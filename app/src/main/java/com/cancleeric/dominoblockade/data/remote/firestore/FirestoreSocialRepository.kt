@file:Suppress("TooManyFunctions", "LongMethod", "MaxLineLength")

package com.cancleeric.dominoblockade.data.remote.firestore

import android.net.Uri
import com.cancleeric.dominoblockade.domain.model.ChallengeInvitation
import com.cancleeric.dominoblockade.domain.model.ChallengeStatus
import com.cancleeric.dominoblockade.domain.model.Friend
import com.cancleeric.dominoblockade.domain.model.FriendRequest
import com.cancleeric.dominoblockade.domain.model.FriendRequestStatus
import com.cancleeric.dominoblockade.domain.repository.SocialRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.tasks.await
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

private const val COLLECTION_USERS = "users"
private const val COLLECTION_FRIEND_REQUESTS = "friend_requests"
private const val COLLECTION_FRIENDSHIPS = "friendships"
private const val COLLECTION_CHALLENGES = "challenges"
private const val COLLECTION_PUSH_QUEUE = "push_queue"
private const val PATH_PRESENCE = "presence"
private const val FIELD_USERNAME = "username"
private const val FIELD_UPDATED_AT = "updatedAt"
private const val FIELD_FCM_TOKEN = "fcmToken"
private const val FIELD_PAIR_KEY = "pairKey"
private const val FIELD_FROM_UID = "fromUid"
private const val FIELD_FROM_USERNAME = "fromUsername"
private const val FIELD_TO_UID = "toUid"
private const val FIELD_STATUS = "status"
private const val FIELD_CREATED_AT = "createdAt"
private const val FIELD_USERS = "users"
private const val FIELD_CHALLENGER_UID = "challengerUid"
private const val FIELD_CHALLENGER_NAME = "challengerName"
private const val FIELD_OPPONENT_UID = "opponentUid"
private const val FIELD_GAME_MODE = "gameMode"
private const val FIELD_ROOM_ID = "roomId"
private const val PRESENCE_ONLINE = "online"
private const val PRESENCE_LAST_SEEN = "lastSeen"
private const val SEARCH_LIMIT = 25L
private const val CHALLENGE_ROOM_CODE_LENGTH = 6
private const val CHALLENGE_ROOM_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
private val secureRandom = SecureRandom()

@Singleton
class FirestoreSocialRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val realtimeDatabase: FirebaseDatabase
) : SocialRepository {

    override suspend fun ensureSignedIn(): String = requireUid()

    override suspend fun ensureUserProfile(username: String) {
        val uid = requireUid()
        val normalized = username.trim().ifEmpty { "Player-${uid.shortId()}" }
        val ref = firestore.collection(COLLECTION_USERS).document(uid)
        val existing = ref.get().await()
        val current = existing.getString(FIELD_USERNAME).orEmpty()
        if (current.isBlank() || current != normalized) {
            ref.set(
                mapOf(
                    FIELD_USERNAME to normalized,
                    FIELD_UPDATED_AT to System.currentTimeMillis()
                ),
                com.google.firebase.firestore.SetOptions.merge()
            ).await()
        }
    }

    override fun observeMyUsername(): Flow<String> = flow {
        val uid = requireUid()
        emitAll(
            callbackFlow {
                val registration = firestore.collection(COLLECTION_USERS)
                    .document(uid)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            close(error)
                            return@addSnapshotListener
                        }
                        val username = snapshot?.getString(FIELD_USERNAME).orEmpty()
                        trySend(if (username.isBlank()) "Player-${uid.shortId()}" else username)
                    }
                awaitClose { registration.remove() }
            }
        )
    }

    override fun observeMyQrValue(): Flow<String> = flow {
        val uid = requireUid()
        emit("domino-blockade://friend/add?uid=$uid")
    }

    override fun searchPlayers(query: String): Flow<List<Friend>> = flow {
        val uid = requireUid()
        val normalized = query.trim()
        if (normalized.isEmpty()) {
            emit(emptyList())
            return@flow
        }
        emitAll(
            callbackFlow {
                val upperBound = "$normalized\uf8ff"
                val registration = firestore.collection(COLLECTION_USERS)
                    .orderBy(FIELD_USERNAME)
                    .whereGreaterThanOrEqualTo(FIELD_USERNAME, normalized)
                    .whereLessThanOrEqualTo(FIELD_USERNAME, upperBound)
                    .limit(SEARCH_LIMIT)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            close(error)
                            return@addSnapshotListener
                        }
                        val players = snapshot?.documents.orEmpty()
                            .mapNotNull { doc ->
                                val playerUid = doc.id
                                if (playerUid == uid) return@mapNotNull null
                                val name = doc.getString(FIELD_USERNAME).orEmpty()
                                if (name.isBlank()) return@mapNotNull null
                                Friend(uid = playerUid, username = name, isOnline = false)
                            }
                        trySend(players)
                    }
                awaitClose { registration.remove() }
            }
        )
    }

    override fun observeFriends(): Flow<List<Friend>> = flow {
        val uid = requireUid()
        val friendIdsFlow = observeFriendIds(uid)
        val friendProfilesFlow = friendIdsFlow.mapLatest { friendIds ->
            fetchUsersByIds(friendIds)
        }
        emitAll(
            combine(friendProfilesFlow, observePresenceMap()) { profiles, presence ->
                profiles.map { friend ->
                    friend.copy(isOnline = presence[friend.uid] == true)
                }.sortedBy { it.username.lowercase() }
            }
        )
    }

    override fun observeIncomingFriendRequests(): Flow<List<FriendRequest>> = flow {
        val uid = requireUid()
        emitAll(
            callbackFlow {
                val registration = firestore.collection(COLLECTION_FRIEND_REQUESTS)
                    .whereEqualTo(FIELD_TO_UID, uid)
                    .whereEqualTo(FIELD_STATUS, FriendRequestStatus.PENDING.name)
                    .orderBy(FIELD_CREATED_AT, Query.Direction.DESCENDING)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            close(error)
                            return@addSnapshotListener
                        }
                        val requests = snapshot?.documents.orEmpty().mapNotNull { it.toFriendRequest() }
                        trySend(requests)
                    }
                awaitClose { registration.remove() }
            }
        )
    }

    override suspend fun sendFriendRequest(targetUid: String) {
        val fromUid = requireUid()
        if (fromUid == targetUid) return
        val fromDoc = firestore.collection(COLLECTION_USERS).document(fromUid).get().await()
        val fromUsername = fromDoc.getString(FIELD_USERNAME).orEmpty().ifBlank { "Player-${fromUid.shortId()}" }
        val pairKey = pairKey(fromUid, targetUid)
        val existing = firestore.collection(COLLECTION_FRIEND_REQUESTS)
            .whereEqualTo(FIELD_PAIR_KEY, pairKey)
            .whereEqualTo(FIELD_STATUS, FriendRequestStatus.PENDING.name)
            .limit(1)
            .get()
            .await()
        if (!existing.isEmpty) return
        val request = mapOf(
            FIELD_FROM_UID to fromUid,
            FIELD_FROM_USERNAME to fromUsername,
            FIELD_TO_UID to targetUid,
            FIELD_STATUS to FriendRequestStatus.PENDING.name,
            FIELD_PAIR_KEY to pairKey,
            FIELD_CREATED_AT to System.currentTimeMillis()
        )
        firestore.collection(COLLECTION_FRIEND_REQUESTS).add(request).await()
    }

    override suspend fun addFriendByQr(targetUid: String) {
        val fromUid = requireUid()
        if (fromUid == targetUid) return
        firestore.collection(COLLECTION_FRIENDSHIPS)
            .document(pairKey(fromUid, targetUid))
            .set(
                mapOf(
                    FIELD_USERS to listOf(fromUid, targetUid).sorted(),
                    FIELD_CREATED_AT to System.currentTimeMillis()
                )
            )
            .await()
    }

    override suspend fun respondToFriendRequest(requestId: String, accept: Boolean) {
        val requestRef = firestore.collection(COLLECTION_FRIEND_REQUESTS).document(requestId)
        val snapshot = requestRef.get().await()
        val fromUid = snapshot.getString(FIELD_FROM_UID).orEmpty()
        val toUid = snapshot.getString(FIELD_TO_UID).orEmpty()
        if (fromUid.isBlank() || toUid.isBlank()) return
        val status = if (accept) FriendRequestStatus.ACCEPTED else FriendRequestStatus.REJECTED
        requestRef.update(FIELD_STATUS, status.name).await()
        if (accept) {
            val friendshipRef = firestore.collection(COLLECTION_FRIENDSHIPS).document(pairKey(fromUid, toUid))
            friendshipRef.set(
                mapOf(
                    FIELD_USERS to listOf(fromUid, toUid).sorted(),
                    FIELD_CREATED_AT to System.currentTimeMillis()
                )
            ).await()
        }
    }

    override fun observePendingChallenges(): Flow<List<ChallengeInvitation>> = flow {
        val uid = requireUid()
        emitAll(
            callbackFlow {
                val registration = firestore.collection(COLLECTION_CHALLENGES)
                    .whereEqualTo(FIELD_OPPONENT_UID, uid)
                    .whereEqualTo(FIELD_STATUS, ChallengeStatus.PENDING.name)
                    .orderBy(FIELD_CREATED_AT, Query.Direction.DESCENDING)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            close(error)
                            return@addSnapshotListener
                        }
                        val challenges = snapshot?.documents.orEmpty().mapNotNull { it.toChallenge() }
                        trySend(challenges)
                    }
                awaitClose { registration.remove() }
            }
        )
    }

    override suspend fun sendChallenge(friendUid: String, friendName: String, gameMode: String) {
        val uid = requireUid()
        val challengerName = firestore.collection(COLLECTION_USERS).document(uid)
            .get()
            .await()
            .getString(FIELD_USERNAME)
            .orEmpty()
            .ifBlank { "Player-${uid.shortId()}" }
        val challengeRef = firestore.collection(COLLECTION_CHALLENGES).document()
        val roomId = generateChallengeRoomCode()
        val acceptDeepLink = challengeDeepLink("accept", challengeRef.id, roomId)
        val declineDeepLink = challengeDeepLink("decline", challengeRef.id)
        val challengeData = mapOf(
            FIELD_CHALLENGER_UID to uid,
            FIELD_CHALLENGER_NAME to challengerName,
            FIELD_OPPONENT_UID to friendUid,
            FIELD_GAME_MODE to gameMode,
            FIELD_ROOM_ID to roomId,
            FIELD_STATUS to ChallengeStatus.PENDING.name,
            FIELD_CREATED_AT to System.currentTimeMillis(),
            "acceptDeepLink" to acceptDeepLink,
            "declineDeepLink" to declineDeepLink
        )
        challengeRef.set(challengeData).await()
        firestore.collection(COLLECTION_PUSH_QUEUE).document(challengeRef.id).set(
            mapOf(
                "type" to "challenge_invitation",
                "toUid" to friendUid,
                "title" to "Challenge from $challengerName",
                "body" to "$challengerName invited you to a $gameMode match",
                "challengerName" to challengerName,
                "opponentName" to friendName,
                "gameMode" to gameMode,
                "challengeId" to challengeRef.id,
                "roomId" to roomId,
                "acceptDeepLink" to acceptDeepLink,
                "declineDeepLink" to declineDeepLink,
                FIELD_CREATED_AT to System.currentTimeMillis()
            )
        ).await()
    }

    override suspend fun respondToChallenge(challengeId: String, accept: Boolean): String? {
        val ref = firestore.collection(COLLECTION_CHALLENGES).document(challengeId)
        val snapshot = ref.get().await()
        val roomId = snapshot.getString(FIELD_ROOM_ID).orEmpty()
        val status = if (accept) ChallengeStatus.ACCEPTED else ChallengeStatus.DECLINED
        ref.update(FIELD_STATUS, status.name).await()
        return if (accept) roomId else null
    }

    override suspend fun saveFcmToken(token: String) {
        if (token.isBlank()) return
        val uid = requireUid()
        firestore.collection(COLLECTION_USERS).document(uid).set(
            mapOf(
                FIELD_FCM_TOKEN to token,
                FIELD_UPDATED_AT to System.currentTimeMillis()
            ),
            com.google.firebase.firestore.SetOptions.merge()
        ).await()
    }

    override suspend fun updatePresence(isOnline: Boolean) {
        val uid = requireUid()
        val ref = realtimeDatabase.getReference(PATH_PRESENCE).child(uid)
        val value = mapOf(
            PRESENCE_ONLINE to isOnline,
            PRESENCE_LAST_SEEN to ServerValue.TIMESTAMP
        )
        ref.updateChildren(value).await()
        if (isOnline) {
            ref.onDisconnect().updateChildren(
                mapOf(
                    PRESENCE_ONLINE to false,
                    PRESENCE_LAST_SEEN to ServerValue.TIMESTAMP
                )
            )
        } else {
            ref.onDisconnect().cancel()
        }
    }

    private fun observeFriendIds(uid: String): Flow<List<String>> = callbackFlow {
        val registration = firestore.collection(COLLECTION_FRIENDSHIPS)
            .whereArrayContains(FIELD_USERS, uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val friendIds = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    val users = doc.get(FIELD_USERS) as? List<*>
                    users?.mapNotNull { it as? String }?.firstOrNull { it != uid }
                }
                trySend(friendIds.distinct())
            }
        awaitClose { registration.remove() }
    }

    private fun observePresenceMap(): Flow<Map<String, Boolean>> = callbackFlow {
        val ref = realtimeDatabase.getReference(PATH_PRESENCE)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val map = buildMap {
                    snapshot.children.forEach { child ->
                        put(child.key.orEmpty(), child.child(PRESENCE_ONLINE).value as? Boolean == true)
                    }
                }
                trySend(map)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    private suspend fun fetchUsersByIds(friendIds: List<String>): List<Friend> {
        if (friendIds.isEmpty()) return emptyList()
        // Firestore whereIn supports up to 10 values per query, so we query in batches.
        return friendIds.chunked(10).flatMap { batch ->
            val query = firestore.collection(COLLECTION_USERS)
                .whereIn(FieldPath.documentId(), batch)
                .get()
                .await()
            query.documents.mapNotNull { doc ->
                val uid = doc.id
                val username = doc.getString(FIELD_USERNAME).orEmpty()
                if (username.isBlank()) null else Friend(uid = uid, username = username, isOnline = false)
            }
        }
    }

    private suspend fun requireUid(): String {
        val current = auth.currentUser
        if (current != null) return current.uid
        val result = auth.signInAnonymously().await()
        return result.user?.uid ?: error("Anonymous sign-in failed")
    }

    private fun pairKey(userA: String, userB: String): String = listOf(userA, userB).sorted().joinToString("_")

    private fun String.shortId(): String = if (length <= 6) this else substring(0, 6)

    private fun challengeDeepLink(action: String, challengeId: String, roomId: String? = null): String {
        val builder = Uri.Builder()
            .scheme("domino-blockade")
            .authority("challenge")
            .appendPath(action)
            .appendQueryParameter("challengeId", challengeId)
        if (!roomId.isNullOrBlank()) {
            builder.appendQueryParameter("roomId", roomId)
        }
        return builder.build().toString()
    }

    private fun generateChallengeRoomCode(): String = buildString(CHALLENGE_ROOM_CODE_LENGTH) {
        repeat(CHALLENGE_ROOM_CODE_LENGTH) {
            append(CHALLENGE_ROOM_CHARS[secureRandom.nextInt(CHALLENGE_ROOM_CHARS.length)])
        }
    }

    private fun DocumentSnapshot.toFriendRequest(): FriendRequest? {
        val fromUid = getString(FIELD_FROM_UID).orEmpty()
        val fromUsername = getString(FIELD_FROM_USERNAME).orEmpty()
        val toUid = getString(FIELD_TO_UID).orEmpty()
        val status = runCatching {
            FriendRequestStatus.valueOf(getString(FIELD_STATUS).orEmpty())
        }.getOrDefault(FriendRequestStatus.PENDING)
        if (fromUid.isBlank() || toUid.isBlank() || fromUsername.isBlank()) return null
        return FriendRequest(
            id = id,
            fromUid = fromUid,
            fromUsername = fromUsername,
            toUid = toUid,
            status = status,
            createdAt = getLong(FIELD_CREATED_AT) ?: 0L
        )
    }

    private fun DocumentSnapshot.toChallenge(): ChallengeInvitation? {
        val challengerUid = getString(FIELD_CHALLENGER_UID).orEmpty()
        val challengerName = getString(FIELD_CHALLENGER_NAME).orEmpty()
        val opponentUid = getString(FIELD_OPPONENT_UID).orEmpty()
        if (challengerUid.isBlank() || challengerName.isBlank() || opponentUid.isBlank()) return null
        val status = runCatching {
            ChallengeStatus.valueOf(getString(FIELD_STATUS).orEmpty())
        }.getOrDefault(ChallengeStatus.PENDING)
        return ChallengeInvitation(
            id = id,
            challengerUid = challengerUid,
            challengerName = challengerName,
            opponentUid = opponentUid,
            gameMode = getString(FIELD_GAME_MODE).orEmpty().ifBlank { "Classic" },
            roomId = getString(FIELD_ROOM_ID).orEmpty(),
            status = status,
            createdAt = getLong(FIELD_CREATED_AT) ?: 0L
        )
    }
}

package com.cancleeric.dominoblockade.data.remote.realtime

import com.cancleeric.dominoblockade.domain.model.GameState
import com.cancleeric.dominoblockade.domain.model.OnlineRoom
import com.cancleeric.dominoblockade.domain.model.OnlineRoomStatus
import com.cancleeric.dominoblockade.domain.repository.OnlineGameRepository
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val PATH_ROOMS = "rooms"
private const val KEY_GUEST_ID = "guestId"
private const val KEY_GUEST_NAME = "guestName"
private const val KEY_STATUS = "status"
private const val KEY_IS_RANKED = "isRanked"
private const val KEY_GAME_STATE = "gameState"
private const val KEY_DISCONNECTED_PLAYER_ID = "disconnectedPlayerId"
private const val PATH_RANKED_QUEUE = "rankedQueue"
private const val PATH_RANKED_ASSIGNMENTS = "rankedAssignments"
private const val KEY_PLAYER_ID = "playerId"
private const val KEY_PLAYER_NAME = "playerName"
private const val KEY_CREATED_AT = "createdAt"
private const val KEY_ROOM_ID = "roomId"
private const val KEY_PLAYER_INDEX = "playerIndex"
private const val KEY_CLAIMED_BY = "claimedBy"
private const val KEY_SPECTATORS = "spectators"
private const val KEY_ALLOW_SPECTATORS = "allowSpectators"
private const val ROOM_CODE_LENGTH = 6
private const val ROOM_CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

@Singleton
class FirebaseRealtimeGameRepository @Inject constructor(
    private val database: FirebaseDatabase
) : OnlineGameRepository {

    private val roomsRef get() = database.getReference(PATH_ROOMS)

    override suspend fun createRoom(hostId: String, hostName: String): String {
        val roomCode = generateRoomCode()
        val data = mapOf(
            "hostId" to hostId,
            "hostName" to hostName,
            KEY_STATUS to OnlineRoomStatus.WAITING.name,
            KEY_IS_RANKED to false
        )
        roomsRef.child(roomCode).setValue(data).await()
        return roomCode
    }

    override suspend fun joinRoom(roomId: String, guestId: String, guestName: String): Boolean {
        val updates: Map<String, Any> = mapOf(
            KEY_GUEST_ID to guestId,
            KEY_GUEST_NAME to guestName,
            KEY_STATUS to OnlineRoomStatus.PLAYING.name
        )
        roomsRef.child(roomId).updateChildren(updates).await()
        return true
    }

    override fun observeRoom(roomId: String): Flow<OnlineRoom> = callbackFlow {
        val ref = roomsRef.child(roomId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val room = snapshotToOnlineRoom(snapshot) ?: return
                trySend(room)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    override suspend fun updateGameState(roomId: String, gameState: GameState) {
        val data = gameStateToMap(gameState)
        roomsRef.child(roomId).child(KEY_GAME_STATE).setValue(data).await()
    }

    override suspend fun leaveRoom(roomId: String) {
        roomsRef.child(roomId).child(KEY_STATUS).setValue(OnlineRoomStatus.FINISHED.name).await()
    }

    override suspend fun registerPresence(roomId: String, playerId: String) {
        val ref = roomsRef.child(roomId).child(KEY_DISCONNECTED_PLAYER_ID)
        ref.setValue(null).await()
        ref.onDisconnect().setValue(playerId).await()
    }

    override suspend fun joinRankedQueue(playerId: String, playerName: String) {
        val queueRef = database.getReference(PATH_RANKED_QUEUE)
        val assignmentRef = database.getReference(PATH_RANKED_ASSIGNMENTS)
        val waitingSnapshot = queueRef.orderByChild(KEY_CREATED_AT).limitToFirst(1).get().await()
        val waiting = waitingSnapshot.children.firstOrNull()
        val waitingPlayerId = waiting?.child(KEY_PLAYER_ID)?.getValue(String::class.java)
        val waitingPlayerName = waiting?.child(KEY_PLAYER_NAME)?.getValue(String::class.java)

        if (!waitingPlayerId.isNullOrBlank() && waitingPlayerId != playerId && !waitingPlayerName.isNullOrBlank()) {
            val claimed = claimWaitingPlayer(queueRef, waitingPlayerId, playerId)
            if (claimed) {
                val roomId = generateRoomCode()
                val roomData = mapOf(
                    "hostId" to waitingPlayerId,
                    "hostName" to waitingPlayerName,
                    KEY_GUEST_ID to playerId,
                    KEY_GUEST_NAME to playerName,
                    KEY_STATUS to OnlineRoomStatus.PLAYING.name,
                    KEY_IS_RANKED to true
                )
                roomsRef.child(roomId).setValue(roomData).await()
                assignmentRef.child(waitingPlayerId).setValue(
                    mapOf(KEY_ROOM_ID to roomId, KEY_PLAYER_INDEX to 0)
                ).await()
                assignmentRef.child(playerId).setValue(
                    mapOf(KEY_ROOM_ID to roomId, KEY_PLAYER_INDEX to 1)
                ).await()
                queueRef.child(waitingPlayerId).removeValue().await()
                return
            }
        }

        queueRef.child(playerId).setValue(
            mapOf(
                KEY_PLAYER_ID to playerId,
                KEY_PLAYER_NAME to playerName,
                KEY_CREATED_AT to System.currentTimeMillis(),
                KEY_CLAIMED_BY to null
            )
        ).await()
    }

    override fun observeRankedAssignment(playerId: String): Flow<Pair<String, Int>?> = callbackFlow {
        val ref = database.getReference(PATH_RANKED_ASSIGNMENTS).child(playerId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val roomId = snapshot.child(KEY_ROOM_ID).getValue(String::class.java)
                val index = snapshot.child(KEY_PLAYER_INDEX).getValue(Long::class.java)?.toInt()
                if (roomId.isNullOrBlank() || index == null) {
                    trySend(null)
                    return
                }
                trySend(roomId to index)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    override suspend fun leaveRankedQueue(playerId: String) {
        database.getReference(PATH_RANKED_QUEUE).child(playerId).removeValue().await()
        database.getReference(PATH_RANKED_ASSIGNMENTS).child(playerId).removeValue().await()
    }

    override suspend fun joinAsSpectator(
        roomId: String,
        spectatorId: String,
        spectatorName: String
    ): Boolean {
        val allowedSnapshot = roomsRef.child(roomId).child(KEY_ALLOW_SPECTATORS).get().await()
        val allowed = allowedSnapshot.getValue(Boolean::class.java) != false
        if (!allowed) return false
        roomsRef.child(roomId).child(KEY_SPECTATORS).child(spectatorId).setValue(spectatorName).await()
        return true
    }

    override suspend fun leaveAsSpectator(roomId: String, spectatorId: String) {
        roomsRef.child(roomId).child(KEY_SPECTATORS).child(spectatorId).removeValue().await()
    }

    override suspend fun setSpectatorPermission(roomId: String, allowed: Boolean) {
        roomsRef.child(roomId).child(KEY_ALLOW_SPECTATORS).setValue(allowed).await()
    }

    private suspend fun claimWaitingPlayer(
        queueRef: com.google.firebase.database.DatabaseReference,
        waitingPlayerId: String,
        claimerId: String
    ): Boolean = suspendCancellableCoroutine { continuation ->
        queueRef.child(waitingPlayerId).runTransaction(
            object : Transaction.Handler {
                override fun doTransaction(currentData: MutableData): Transaction.Result {
                    val value = currentData.value as? Map<*, *> ?: return Transaction.abort()
                    val playerId = value[KEY_PLAYER_ID] as? String ?: return Transaction.abort()
                    val alreadyClaimedBy = value[KEY_CLAIMED_BY] as? String
                    if (playerId != waitingPlayerId || !alreadyClaimedBy.isNullOrBlank()) {
                        return Transaction.abort()
                    }
                    currentData.value = value.toMutableMap().apply { put(KEY_CLAIMED_BY, claimerId) }
                    return Transaction.success(currentData)
                }

                override fun onComplete(
                    error: DatabaseError?,
                    committed: Boolean,
                    currentData: DataSnapshot?
                ) {
                    if (error != null) {
                        if (continuation.isActive) {
                            continuation.resumeWith(Result.failure(error.toException()))
                        }
                        return
                    }
                    if (continuation.isActive) {
                        continuation.resumeWith(Result.success(committed))
                    }
                }
            }
        )
    }

    private fun generateRoomCode(): String =
        (1..ROOM_CODE_LENGTH).map { ROOM_CODE_CHARS.random() }.joinToString("")
}

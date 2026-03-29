package com.cancleeric.dominoblockade.data.remote.firestore

import com.cancleeric.dominoblockade.data.model.LeaderboardEntry
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Firestore implementation of [LeaderboardRepository].
 *
 * Data is stored at leaderboard/domino_blockade/{userId} — the same path
 * used by iOS LifeSnap — enabling a cross-platform global leaderboard.
 */
class FirestoreLeaderboardRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : LeaderboardRepository {

    private val collection
        get() = firestore
            .collection(COLLECTION_LEADERBOARD)
            .document(DOCUMENT_DOMINO_BLOCKADE)
            .collection(COLLECTION_PLAYERS)

    override fun getTopPlayers(limit: Int): Flow<List<LeaderboardEntry>> = callbackFlow {
        val registration = collection
            .orderBy(FIELD_HIGH_SCORE, Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val entries = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(LeaderboardEntry::class.java)?.copy(userId = doc.id)
                } ?: emptyList()
                trySend(entries)
            }
        awaitClose { registration.remove() }
    }

    override suspend fun submitScore(entry: LeaderboardEntry) {
        val data = mapOf(
            FIELD_DISPLAY_NAME to entry.displayName,
            FIELD_HIGH_SCORE to entry.highScore,
            FIELD_TOTAL_WINS to entry.totalWins,
            FIELD_PLATFORM to entry.platform,
            FIELD_LAST_UPDATED to com.google.firebase.Timestamp.now()
        )
        collection.document(entry.userId).set(data).await()
    }

    override fun getPlayerRank(userId: String): Flow<Int?> =
        getTopPlayers(limit = MAX_RANK_QUERY_SIZE).map { entries ->
            val index = entries.indexOfFirst { it.userId == userId }
            if (index >= 0) index + 1 else null
        }

    companion object {
        private const val COLLECTION_LEADERBOARD = "leaderboard"
        private const val DOCUMENT_DOMINO_BLOCKADE = "domino_blockade"
        private const val COLLECTION_PLAYERS = "players"
        private const val FIELD_DISPLAY_NAME = "displayName"
        private const val FIELD_HIGH_SCORE = "highScore"
        private const val FIELD_TOTAL_WINS = "totalWins"
        private const val FIELD_PLATFORM = "platform"
        private const val FIELD_LAST_UPDATED = "lastUpdated"
        private const val MAX_RANK_QUERY_SIZE = 500
    }
}

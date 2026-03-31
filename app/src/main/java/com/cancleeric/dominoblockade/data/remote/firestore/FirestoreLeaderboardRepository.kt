package com.cancleeric.dominoblockade.data.remote.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val COLLECTION_LEADERBOARD = "leaderboard"
private const val DOCUMENT_DOMINO_BLOCKADE = "domino_blockade"
private const val COLLECTION_PLAYERS = "players"
private const val RANK_QUERY_LIMIT = 1000L

@Singleton
class FirestoreLeaderboardRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : LeaderboardRepository {

    private val collectionRef
        get() = firestore
            .collection(COLLECTION_LEADERBOARD)
            .document(DOCUMENT_DOMINO_BLOCKADE)
            .collection(COLLECTION_PLAYERS)

    override fun getTopPlayers(limit: Int): Flow<List<LeaderboardEntry>> = callbackFlow {
        val registration = collectionRef
            .orderBy(LeaderboardEntry.FIELD_HIGH_SCORE, Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val entries = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject<LeaderboardEntry>()?.copy(userId = doc.id)
                }.orEmpty()
                trySend(entries)
            }
        awaitClose { registration.remove() }
    }

    override suspend fun submitScore(entry: LeaderboardEntry) {
        val data = mapOf(
            LeaderboardEntry.FIELD_DISPLAY_NAME to entry.displayName,
            LeaderboardEntry.FIELD_HIGH_SCORE to entry.highScore,
            LeaderboardEntry.FIELD_TOTAL_WINS to entry.totalWins,
            LeaderboardEntry.FIELD_PLATFORM to entry.platform,
            LeaderboardEntry.FIELD_LAST_UPDATED to entry.lastUpdated
        )
        collectionRef.document(entry.userId).set(data).await()
    }

    override fun getPlayerRank(userId: String): Flow<Int?> = flow {
        val snapshot = collectionRef
            .orderBy(LeaderboardEntry.FIELD_HIGH_SCORE, Query.Direction.DESCENDING)
            .limit(RANK_QUERY_LIMIT)
            .get()
            .await()
        val index = snapshot.documents.indexOfFirst { it.id == userId }
        emit(if (index >= 0) index + 1 else null)
    }
}

package com.cancleeric.dominoblockade.data.remote.firestore

import com.cancleeric.dominoblockade.data.crashlytics.CrashlyticsHelper
import com.cancleeric.dominoblockade.domain.model.LeaderboardEntry
import com.cancleeric.dominoblockade.domain.repository.LeaderboardRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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
private const val FIELD_DISPLAY_NAME = "displayName"
private const val FIELD_HIGH_SCORE = "highScore"
private const val FIELD_TOTAL_WINS = "totalWins"
private const val FIELD_PLATFORM = "platform"
private const val FIELD_LAST_UPDATED = "lastUpdated"

@Singleton
class FirestoreLeaderboardRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val crashlyticsHelper: CrashlyticsHelper
) : LeaderboardRepository {

    private val collectionRef
        get() = firestore
            .collection(COLLECTION_LEADERBOARD)
            .document(DOCUMENT_DOMINO_BLOCKADE)
            .collection(COLLECTION_PLAYERS)

    override fun getTopPlayers(limit: Int): Flow<List<LeaderboardEntry>> = callbackFlow {
        val registration = collectionRef
            .orderBy(FIELD_HIGH_SCORE, Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    crashlyticsHelper.logException(error)
                    close(error)
                    return@addSnapshotListener
                }
                val entries = snapshot?.documents?.map { doc ->
                    doc.toFirestoreLeaderboardEntry()
                }.orEmpty()
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
            FIELD_LAST_UPDATED to entry.lastUpdated
        )
        try {
            collectionRef.document(entry.userId).set(data).await()
        } catch (e: Exception) {
            crashlyticsHelper.logException(e)
            throw e
        }
    }

    override fun getPlayerRank(userId: String): Flow<Int?> = flow {
        try {
            val snapshot = collectionRef
                .orderBy(FIELD_HIGH_SCORE, Query.Direction.DESCENDING)
                .limit(RANK_QUERY_LIMIT)
                .get()
                .await()
            val index = snapshot.documents.indexOfFirst { it.id == userId }
            emit(if (index >= 0) index + 1 else null)
        } catch (e: Exception) {
            crashlyticsHelper.logException(e)
            throw e
        }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toFirestoreLeaderboardEntry(): LeaderboardEntry {
        return LeaderboardEntry(
            userId = id,
            displayName = getString(FIELD_DISPLAY_NAME) ?: "",
            highScore = getLong(FIELD_HIGH_SCORE)?.toInt() ?: 0,
            totalWins = getLong(FIELD_TOTAL_WINS)?.toInt() ?: 0,
            platform = getString(FIELD_PLATFORM) ?: LeaderboardEntry.PLATFORM_ANDROID,
            lastUpdated = getLong(FIELD_LAST_UPDATED) ?: 0L
        )
    }
}

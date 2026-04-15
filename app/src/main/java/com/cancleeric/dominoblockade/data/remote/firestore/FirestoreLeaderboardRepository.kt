package com.cancleeric.dominoblockade.data.remote.firestore

import com.cancleeric.dominoblockade.domain.model.LeaderboardEntry
import com.cancleeric.dominoblockade.domain.repository.LeaderboardSegment
import com.cancleeric.dominoblockade.domain.repository.LeaderboardRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.time.YearMonth
import java.time.ZoneOffset
import kotlin.math.pow
import kotlin.math.roundToInt
import javax.inject.Inject
import javax.inject.Singleton

private const val COLLECTION_LEADERBOARD = "leaderboard"
private const val COLLECTION_LEADERBOARD_ALL_TIME = "leaderboard_all_time"
private const val COLLECTION_LEADERBOARD_HISTORY = "leaderboard_history"
private const val COLLECTION_FRIENDSHIPS = "friendships"
private const val COLLECTION_LEADERBOARD_META = "leaderboard_meta"
private const val COLLECTION_RANKED_MATCH_RESULTS = "ranked_match_results"
private const val DOCUMENT_SEASON_STATE = "season_state"
private const val COLLECTION_HISTORY_PLAYERS = "players"
private const val FIELD_USERS = "users"
private const val FIELD_CURRENT_SEASON = "currentSeason"
private const val FIELD_UPDATED_AT = "updatedAt"
private const val FIELD_DISPLAY_NAME = "displayName"
private const val FIELD_ELO = "elo"
private const val FIELD_WINS = "wins"
private const val FIELD_LOSSES = "losses"
private const val FIELD_SEASON = "season"
private const val K_FACTOR = 32.0
private const val RANK_QUERY_LIMIT = 10_000L
private const val QUERY_IN_LIMIT = 10
private const val BATCH_LIMIT = 400

@Singleton
class FirestoreLeaderboardRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : LeaderboardRepository {

    private val leaderboardRef
        get() = firestore.collection(COLLECTION_LEADERBOARD)

    private val allTimeRef
        get() = firestore.collection(COLLECTION_LEADERBOARD_ALL_TIME)

    override fun getTopPlayers(
        limit: Int,
        segment: LeaderboardSegment,
        currentUserId: String?
    ): Flow<List<LeaderboardEntry>> = flow {
        resetSeasonIfNeeded()
        val topPlayers = when (segment) {
            LeaderboardSegment.CURRENT_SEASON -> querySeasonLeaderboard(limit)
            LeaderboardSegment.ALL_TIME -> queryAllTimeLeaderboard(limit)
            LeaderboardSegment.FRIENDS_ONLY -> queryFriendsLeaderboard(limit, currentUserId)
        }
        emit(topPlayers)
    }

    override fun getPlayerRank(
        userId: String,
        segment: LeaderboardSegment,
        currentUserId: String?
    ): Flow<Int?> = flow {
        resetSeasonIfNeeded()
        val rank = when (segment) {
            LeaderboardSegment.CURRENT_SEASON -> queryRankInCollection(
                leaderboardRef
                    .whereEqualTo(FIELD_SEASON, currentSeason())
                    .orderBy(FIELD_ELO, Query.Direction.DESCENDING),
                userId
            )
            LeaderboardSegment.ALL_TIME -> queryRankInCollection(
                allTimeRef.orderBy(FIELD_ELO, Query.Direction.DESCENDING),
                userId
            )
            LeaderboardSegment.FRIENDS_ONLY -> {
                val friendIds = getFriendIds(currentUserId).toMutableSet()
                friendIds.add(userId)
                val entries = fetchEntriesByIds(leaderboardRef, friendIds.toList())
                    .filter { it.season == currentSeason() }
                    .sortedByDescending { it.elo }
                entries.indexOfFirst { it.userId == userId }.takeIf { it >= 0 }?.plus(1)
            }
        }
        emit(rank)
    }

    override suspend fun ensurePlayerEntry(userId: String, displayName: String) {
        resetSeasonIfNeeded()
        val season = currentSeason()
        val playerRef = leaderboardRef.document(userId)
        val snapshot = playerRef.get().await()
        val existingSeason = snapshot.getString(FIELD_SEASON)
        if (!snapshot.exists() || existingSeason != season) {
            playerRef.set(
                mapOf(
                    FIELD_DISPLAY_NAME to displayName,
                    FIELD_ELO to LeaderboardEntry.DEFAULT_ELO,
                    FIELD_WINS to 0,
                    FIELD_LOSSES to 0,
                    FIELD_SEASON to season
                ),
                SetOptions.merge()
            ).await()
        } else if ((snapshot.getString(FIELD_DISPLAY_NAME) ?: "") != displayName) {
            playerRef.update(FIELD_DISPLAY_NAME, displayName).await()
        }
        val allTimeSnapshot = allTimeRef.document(userId).get().await()
        if (!allTimeSnapshot.exists()) {
            allTimeRef.document(userId).set(
                mapOf(
                    FIELD_DISPLAY_NAME to displayName,
                    FIELD_ELO to LeaderboardEntry.DEFAULT_ELO,
                    FIELD_WINS to 0,
                    FIELD_LOSSES to 0,
                    FIELD_SEASON to "all-time"
                ),
                SetOptions.merge()
            ).await()
        }
    }

    override suspend fun updateRankedMatchResult(
        matchId: String,
        winnerId: String,
        winnerDisplayName: String,
        loserId: String,
        loserDisplayName: String
    ) {
        resetSeasonIfNeeded()
        ensurePlayerEntry(winnerId, winnerDisplayName)
        ensurePlayerEntry(loserId, loserDisplayName)
        val season = currentSeason()

        firestore.runTransaction { transaction ->
            val winnerSeasonRef = leaderboardRef.document(winnerId)
            val loserSeasonRef = leaderboardRef.document(loserId)
            val winnerAllTimeRef = allTimeRef.document(winnerId)
            val loserAllTimeRef = allTimeRef.document(loserId)
            val matchResultRef = firestore.collection(COLLECTION_RANKED_MATCH_RESULTS).document(matchId)

            val existingMatch = transaction.get(matchResultRef)
            if (existingMatch.exists()) return@runTransaction null

            val winnerSeasonSnap = transaction.get(winnerSeasonRef)
            val loserSeasonSnap = transaction.get(loserSeasonRef)

            val winnerElo = winnerSeasonSnap.getLong(FIELD_ELO)?.toInt() ?: LeaderboardEntry.DEFAULT_ELO
            val loserElo = loserSeasonSnap.getLong(FIELD_ELO)?.toInt() ?: LeaderboardEntry.DEFAULT_ELO

            val expectedWinner = 1.0 / (1.0 + 10.0.pow((loserElo - winnerElo) / 400.0))
            val expectedLoser = 1.0 - expectedWinner
            val winnerNewElo = (winnerElo + K_FACTOR * (1.0 - expectedWinner)).roundToInt().coerceAtLeast(0)
            val loserNewElo = (loserElo - K_FACTOR * expectedLoser).roundToInt().coerceAtLeast(0)

            val winnerWins = (winnerSeasonSnap.getLong(FIELD_WINS) ?: 0L) + 1L
            val loserLosses = (loserSeasonSnap.getLong(FIELD_LOSSES) ?: 0L) + 1L

            transaction.set(
                winnerSeasonRef,
                mapOf(
                    FIELD_DISPLAY_NAME to winnerDisplayName,
                    FIELD_ELO to winnerNewElo,
                    FIELD_WINS to winnerWins,
                    FIELD_SEASON to season
                ),
                SetOptions.merge()
            )
            transaction.set(
                loserSeasonRef,
                mapOf(
                    FIELD_DISPLAY_NAME to loserDisplayName,
                    FIELD_ELO to loserNewElo,
                    FIELD_LOSSES to loserLosses,
                    FIELD_SEASON to season
                ),
                SetOptions.merge()
            )

            val winnerAllTimeSnap = transaction.get(winnerAllTimeRef)
            val loserAllTimeSnap = transaction.get(loserAllTimeRef)

            val winnerAllTimeWins = (winnerAllTimeSnap.getLong(FIELD_WINS) ?: 0L) + 1L
            val loserAllTimeLosses = (loserAllTimeSnap.getLong(FIELD_LOSSES) ?: 0L) + 1L
            val winnerAllTimeElo = winnerAllTimeSnap.getLong(FIELD_ELO)?.toInt() ?: LeaderboardEntry.DEFAULT_ELO
            val loserAllTimeElo = loserAllTimeSnap.getLong(FIELD_ELO)?.toInt() ?: LeaderboardEntry.DEFAULT_ELO
            val expectedWinnerAllTime = 1.0 / (1.0 + 10.0.pow((loserAllTimeElo - winnerAllTimeElo) / 400.0))
            val expectedLoserAllTime = 1.0 - expectedWinnerAllTime
            val winnerAllTimeNewElo =
                (winnerAllTimeElo + K_FACTOR * (1.0 - expectedWinnerAllTime)).roundToInt().coerceAtLeast(0)
            val loserAllTimeNewElo =
                (loserAllTimeElo - K_FACTOR * expectedLoserAllTime).roundToInt().coerceAtLeast(0)

            transaction.set(
                winnerAllTimeRef,
                mapOf(
                    FIELD_DISPLAY_NAME to winnerDisplayName,
                    FIELD_ELO to winnerAllTimeNewElo,
                    FIELD_WINS to winnerAllTimeWins,
                    FIELD_SEASON to "all-time"
                ),
                SetOptions.merge()
            )
            transaction.set(
                loserAllTimeRef,
                mapOf(
                    FIELD_DISPLAY_NAME to loserDisplayName,
                    FIELD_ELO to loserAllTimeNewElo,
                    FIELD_LOSSES to loserAllTimeLosses,
                    FIELD_SEASON to "all-time"
                ),
                SetOptions.merge()
            )

            transaction.set(
                matchResultRef,
                mapOf(
                    "winnerId" to winnerId,
                    "loserId" to loserId,
                    FIELD_UPDATED_AT to System.currentTimeMillis()
                ),
                SetOptions.merge()
            )
            null
        }.await()
    }

    override suspend fun resetSeasonIfNeeded() {
        val season = currentSeason()
        val seasonMetaRef = firestore.collection(COLLECTION_LEADERBOARD_META).document(DOCUMENT_SEASON_STATE)
        val meta = seasonMetaRef.get().await()
        val current = meta.getString(FIELD_CURRENT_SEASON)
        if (current == season) return

        if (!current.isNullOrBlank()) {
            val previousSeasonSnapshot = leaderboardRef
                .whereEqualTo(FIELD_SEASON, current)
                .get()
                .await()
            previousSeasonSnapshot.documents.chunked(BATCH_LIMIT).forEach { chunk ->
                val batch = firestore.batch()
                chunk.forEach { doc ->
                    val archiveRef = firestore.collection(COLLECTION_LEADERBOARD_HISTORY)
                        .document(current)
                        .collection(COLLECTION_HISTORY_PLAYERS)
                        .document(doc.id)
                    val displayName = doc.getString(FIELD_DISPLAY_NAME).orEmpty()
                    batch.set(
                        archiveRef,
                        mapOf(
                            "userId" to doc.id,
                            FIELD_DISPLAY_NAME to displayName,
                            FIELD_ELO to (doc.getLong(FIELD_ELO) ?: LeaderboardEntry.DEFAULT_ELO.toLong()),
                            FIELD_WINS to (doc.getLong(FIELD_WINS) ?: 0L),
                            FIELD_LOSSES to (doc.getLong(FIELD_LOSSES) ?: 0L),
                            FIELD_SEASON to current
                        ),
                        SetOptions.merge()
                    )
                    batch.set(
                        leaderboardRef.document(doc.id),
                        mapOf(
                            FIELD_DISPLAY_NAME to displayName,
                            FIELD_ELO to LeaderboardEntry.DEFAULT_ELO,
                            FIELD_WINS to 0,
                            FIELD_LOSSES to 0,
                            FIELD_SEASON to season
                        ),
                        SetOptions.merge()
                    )
                }
                batch.commit().await()
            }
        }

        seasonMetaRef.set(
            mapOf(
                FIELD_CURRENT_SEASON to season,
                FIELD_UPDATED_AT to System.currentTimeMillis()
            ),
            SetOptions.merge()
        ).await()
    }

    private suspend fun querySeasonLeaderboard(limit: Int): List<LeaderboardEntry> {
        val snapshot = leaderboardRef
            .whereEqualTo(FIELD_SEASON, currentSeason())
            .orderBy(FIELD_ELO, Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .await()
        return snapshot.documents.map { it.toLeaderboardEntry() }
    }

    private suspend fun queryAllTimeLeaderboard(limit: Int): List<LeaderboardEntry> {
        val snapshot = allTimeRef
            .orderBy(FIELD_ELO, Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .await()
        return snapshot.documents.map { it.toLeaderboardEntry() }
    }

    private suspend fun queryFriendsLeaderboard(limit: Int, currentUserId: String?): List<LeaderboardEntry> {
        val ids = getFriendIds(currentUserId).toMutableSet()
        currentUserId?.let { ids.add(it) }
        return fetchEntriesByIds(leaderboardRef, ids.toList())
            .filter { it.season == currentSeason() }
            .sortedByDescending { it.elo }
            .take(limit)
    }

    private suspend fun queryRankInCollection(query: Query, userId: String): Int? {
        val snapshot = query.limit(RANK_QUERY_LIMIT).get().await()
        val index = snapshot.documents.indexOfFirst { it.id == userId }
        return if (index >= 0) index + 1 else null
    }

    private suspend fun getFriendIds(currentUserId: String?): List<String> {
        if (currentUserId.isNullOrBlank()) return emptyList()
        val snapshot = firestore.collection(COLLECTION_FRIENDSHIPS)
            .whereArrayContains(FIELD_USERS, currentUserId)
            .get()
            .await()
        return snapshot.documents.mapNotNull { doc ->
            val users = doc.get(FIELD_USERS) as? List<*>
            users?.filterIsInstance<String>()?.firstOrNull { it != currentUserId }
        }
    }

    private suspend fun fetchEntriesByIds(
        collection: com.google.firebase.firestore.CollectionReference,
        ids: List<String>
    ): List<LeaderboardEntry> {
        if (ids.isEmpty()) return emptyList()
        return ids.distinct().chunked(QUERY_IN_LIMIT).flatMap { batch ->
            val snapshot = collection.whereIn(com.google.firebase.firestore.FieldPath.documentId(), batch).get().await()
            snapshot.documents.map { it.toLeaderboardEntry() }
        }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toLeaderboardEntry(): LeaderboardEntry {
        return LeaderboardEntry(
            userId = id,
            displayName = getString(FIELD_DISPLAY_NAME).orEmpty(),
            elo = getLong(FIELD_ELO)?.toInt() ?: LeaderboardEntry.DEFAULT_ELO,
            wins = getLong(FIELD_WINS)?.toInt() ?: 0,
            losses = getLong(FIELD_LOSSES)?.toInt() ?: 0,
            season = getString(FIELD_SEASON).orEmpty()
        )
    }

    /**
     * Returns the current leaderboard season in UTC as YYYY-MM (for example: 2026-04).
     */
    private fun currentSeason(): String = YearMonth.now(ZoneOffset.UTC).toString()
}

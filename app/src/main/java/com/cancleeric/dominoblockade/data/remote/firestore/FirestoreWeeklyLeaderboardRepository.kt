package com.cancleeric.dominoblockade.data.remote.firestore

import com.cancleeric.dominoblockade.domain.model.WeeklyLeaderboardEntry
import com.cancleeric.dominoblockade.domain.repository.WeeklyLeaderboardRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.time.DayOfWeek
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.IsoFields
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import javax.inject.Singleton

private const val COLLECTION_WEEKLY_LEADERBOARD = "weekly_leaderboard"
private const val COLLECTION_SCORES = "scores"
private const val COLLECTION_USER_PROFILES = "user_profiles"
private const val FIELD_WEEKLY_BADGES = "weeklyBadges"
private const val FIELD_DISPLAY_NAME = "displayName"
private const val FIELD_SCORE = "score"
private const val FIELD_WINS = "wins"
private const val FIELD_LOSSES = "losses"
private const val FIELD_CURRENT_STREAK = "currentStreak"
private const val FIELD_HAS_BADGE = "hasBadge"
private const val FIELD_WEEK_ID = "weekId"
private const val TOP_BADGE_RANK = 3
private const val RANK_QUERY_LIMIT = 10_000L

@Singleton
class FirestoreWeeklyLeaderboardRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : WeeklyLeaderboardRepository {

    override fun getCurrentWeekId(): String {
        val now = ZonedDateTime.now(ZoneOffset.UTC)
        val year = now.get(IsoFields.WEEK_BASED_YEAR)
        val week = now.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
        return "$year-W${week.toString().padStart(2, '0')}"
    }

    override fun getMillisUntilWeekReset(): Long {
        val now = ZonedDateTime.now(ZoneOffset.UTC)
        val nextMonday = now.with(TemporalAdjusters.next(DayOfWeek.MONDAY))
            .withHour(0).withMinute(0).withSecond(0).withNano(0)
        return nextMonday.toInstant().toEpochMilli() - now.toInstant().toEpochMilli()
    }

    override fun getTopPlayers(weekId: String, limit: Int): Flow<List<WeeklyLeaderboardEntry>> = flow {
        val snapshot = scoresRef(weekId)
            .orderBy(FIELD_SCORE, Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .await()
        emit(snapshot.documents.map { it.toEntry(weekId) })
    }

    override fun getPlayerEntry(weekId: String, userId: String): Flow<WeeklyLeaderboardEntry?> = flow {
        val snapshot = scoresRef(weekId).document(userId).get().await()
        emit(if (snapshot.exists()) snapshot.toEntry(weekId) else null)
    }

    override fun getPlayerRank(weekId: String, userId: String): Flow<Int?> = flow {
        val snapshot = scoresRef(weekId)
            .orderBy(FIELD_SCORE, Query.Direction.DESCENDING)
            .limit(RANK_QUERY_LIMIT)
            .get()
            .await()
        val index = snapshot.documents.indexOfFirst { it.id == userId }
        emit(if (index >= 0) index + 1 else null)
    }

    override suspend fun ensurePlayerEntry(weekId: String, userId: String, displayName: String) {
        val ref = scoresRef(weekId).document(userId)
        val snapshot = ref.get().await()
        if (!snapshot.exists()) {
            ref.set(
                mapOf(
                    FIELD_WEEK_ID to weekId,
                    FIELD_DISPLAY_NAME to displayName,
                    FIELD_SCORE to 0,
                    FIELD_WINS to 0,
                    FIELD_LOSSES to 0,
                    FIELD_CURRENT_STREAK to 0,
                    FIELD_HAS_BADGE to false
                ),
                SetOptions.merge()
            ).await()
        } else if ((snapshot.getString(FIELD_DISPLAY_NAME) ?: "") != displayName) {
            ref.update(FIELD_DISPLAY_NAME, displayName).await()
        }
    }

    override suspend fun updateMatchResult(
        weekId: String,
        winnerId: String,
        winnerDisplayName: String,
        loserId: String,
        loserDisplayName: String
    ) {
        ensurePlayerEntry(weekId, winnerId, winnerDisplayName)
        ensurePlayerEntry(weekId, loserId, loserDisplayName)

        firestore.runTransaction { transaction ->
            val winnerRef = scoresRef(weekId).document(winnerId)
            val loserRef = scoresRef(weekId).document(loserId)

            val winnerSnap = transaction.get(winnerRef)
            val loserSnap = transaction.get(loserRef)

            val winnerWins = (winnerSnap.getLong(FIELD_WINS) ?: 0L) + 1L
            val winnerStreak = (winnerSnap.getLong(FIELD_CURRENT_STREAK) ?: 0L) + 1L
            val streakBonus = (winnerStreak - 1) * WeeklyLeaderboardEntry.POINTS_STREAK_BONUS
            val winnerScoreGain = WeeklyLeaderboardEntry.POINTS_WIN + streakBonus
            val winnerScore = (winnerSnap.getLong(FIELD_SCORE) ?: 0L) + winnerScoreGain

            val loserLosses = (loserSnap.getLong(FIELD_LOSSES) ?: 0L) + 1L
            val loserScore = (loserSnap.getLong(FIELD_SCORE) ?: 0L) + WeeklyLeaderboardEntry.POINTS_LOSS

            transaction.set(
                winnerRef,
                mapOf(
                    FIELD_DISPLAY_NAME to winnerDisplayName,
                    FIELD_SCORE to winnerScore,
                    FIELD_WINS to winnerWins,
                    FIELD_CURRENT_STREAK to winnerStreak
                ),
                SetOptions.merge()
            )
            transaction.set(
                loserRef,
                mapOf(
                    FIELD_DISPLAY_NAME to loserDisplayName,
                    FIELD_SCORE to loserScore,
                    FIELD_LOSSES to loserLosses,
                    FIELD_CURRENT_STREAK to 0L
                ),
                SetOptions.merge()
            )
            null
        }.await()
    }

    override suspend fun grantBadgeToTopFinishers(weekId: String) {
        val snapshot = scoresRef(weekId)
            .orderBy(FIELD_SCORE, Query.Direction.DESCENDING)
            .limit(TOP_BADGE_RANK.toLong())
            .get()
            .await()

        val batch = firestore.batch()
        snapshot.documents.forEach { doc ->
            val userId = doc.id
            batch.set(
                scoresRef(weekId).document(userId),
                mapOf(FIELD_HAS_BADGE to true),
                SetOptions.merge()
            )
            val profileRef = firestore.collection(COLLECTION_USER_PROFILES).document(userId)
            val profileSnap = profileRef.get().await()
            @Suppress("UNCHECKED_CAST")
            val existingBadges = (profileSnap.get(FIELD_WEEKLY_BADGES) as? List<String>).orEmpty()
            if (!existingBadges.contains(weekId)) {
                batch.set(
                    profileRef,
                    mapOf(FIELD_WEEKLY_BADGES to existingBadges + weekId),
                    SetOptions.merge()
                )
            }
        }
        batch.commit().await()
    }

    private fun scoresRef(weekId: String) =
        firestore.collection(COLLECTION_WEEKLY_LEADERBOARD)
            .document(weekId)
            .collection(COLLECTION_SCORES)

    private fun com.google.firebase.firestore.DocumentSnapshot.toEntry(weekId: String): WeeklyLeaderboardEntry =
        WeeklyLeaderboardEntry(
            weekId = weekId,
            userId = id,
            displayName = getString(FIELD_DISPLAY_NAME).orEmpty(),
            score = getLong(FIELD_SCORE)?.toInt() ?: 0,
            wins = getLong(FIELD_WINS)?.toInt() ?: 0,
            losses = getLong(FIELD_LOSSES)?.toInt() ?: 0,
            currentStreak = getLong(FIELD_CURRENT_STREAK)?.toInt() ?: 0,
            hasBadge = getBoolean(FIELD_HAS_BADGE) ?: false
        )
}

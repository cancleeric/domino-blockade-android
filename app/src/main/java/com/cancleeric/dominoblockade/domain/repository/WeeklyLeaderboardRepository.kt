package com.cancleeric.dominoblockade.domain.repository

import com.cancleeric.dominoblockade.domain.model.WeeklyLeaderboardEntry
import kotlinx.coroutines.flow.Flow

interface WeeklyLeaderboardRepository {
    fun getCurrentWeekId(): String
    fun getMillisUntilWeekReset(): Long
    fun getTopPlayers(weekId: String, limit: Int): Flow<List<WeeklyLeaderboardEntry>>
    fun getPlayerEntry(weekId: String, userId: String): Flow<WeeklyLeaderboardEntry?>
    fun getPlayerRank(weekId: String, userId: String): Flow<Int?>
    suspend fun ensurePlayerEntry(weekId: String, userId: String, displayName: String)
    suspend fun updateMatchResult(
        weekId: String,
        winnerId: String,
        winnerDisplayName: String,
        loserId: String,
        loserDisplayName: String
    )
    suspend fun grantBadgeToTopFinishers(weekId: String)
}

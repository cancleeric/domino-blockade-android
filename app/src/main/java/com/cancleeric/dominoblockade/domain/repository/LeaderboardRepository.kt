package com.cancleeric.dominoblockade.domain.repository

import com.cancleeric.dominoblockade.domain.model.LeaderboardEntry
import kotlinx.coroutines.flow.Flow

enum class LeaderboardSegment {
    CURRENT_SEASON,
    ALL_TIME,
    FRIENDS_ONLY
}

interface LeaderboardRepository {
    fun getTopPlayers(limit: Int, segment: LeaderboardSegment, currentUserId: String?): Flow<List<LeaderboardEntry>>
    fun getPlayerRank(userId: String, segment: LeaderboardSegment, currentUserId: String?): Flow<Int?>
    suspend fun ensurePlayerEntry(userId: String, displayName: String)
    suspend fun updateRankedMatchResult(
        matchId: String,
        winnerId: String,
        winnerDisplayName: String,
        loserId: String,
        loserDisplayName: String
    )
    suspend fun resetSeasonIfNeeded()
}

package com.cancleeric.dominoblockade.domain.repository

import com.cancleeric.dominoblockade.domain.model.LeaderboardEntry
import kotlinx.coroutines.flow.Flow

interface LeaderboardRepository {
    fun getTopPlayers(limit: Int): Flow<List<LeaderboardEntry>>
    suspend fun submitScore(entry: LeaderboardEntry)
    fun getPlayerRank(userId: String): Flow<Int?>
}

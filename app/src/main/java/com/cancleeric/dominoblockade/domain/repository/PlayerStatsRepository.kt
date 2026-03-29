package com.cancleeric.dominoblockade.domain.repository

import com.cancleeric.dominoblockade.data.local.entity.PlayerStatsEntity
import kotlinx.coroutines.flow.Flow

interface PlayerStatsRepository {
    suspend fun upsertStats(stats: PlayerStatsEntity)
    suspend fun getStatsByName(name: String): PlayerStatsEntity?
    fun getAllStats(): Flow<List<PlayerStatsEntity>>
}

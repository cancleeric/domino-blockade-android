package com.cancleeric.dominoblockade.domain.repository

import com.cancleeric.dominoblockade.data.local.entity.PlayerStatsEntity
import kotlinx.coroutines.flow.Flow

interface PlayerStatsRepository {
    suspend fun upsert(stats: PlayerStatsEntity)
    suspend fun getByName(name: String): PlayerStatsEntity?
    fun getAll(): Flow<List<PlayerStatsEntity>>
}

package com.cancleeric.dominoblockade.domain.repository

import com.cancleeric.dominoblockade.data.local.entity.GameRecordEntity
import kotlinx.coroutines.flow.Flow

interface GameRecordRepository {
    suspend fun insert(record: GameRecordEntity)
    fun getAll(): Flow<List<GameRecordEntity>>
    fun getRecent(limit: Int): Flow<List<GameRecordEntity>>
    fun getById(id: Long): Flow<GameRecordEntity?>
}

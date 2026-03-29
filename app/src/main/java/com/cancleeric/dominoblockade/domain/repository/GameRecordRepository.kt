package com.cancleeric.dominoblockade.domain.repository

import com.cancleeric.dominoblockade.data.local.entity.GameRecordEntity
import kotlinx.coroutines.flow.Flow

interface GameRecordRepository {
    suspend fun saveRecord(record: GameRecordEntity)
    fun getAllRecords(): Flow<List<GameRecordEntity>>
    fun getRecentRecords(limit: Int): Flow<List<GameRecordEntity>>
}

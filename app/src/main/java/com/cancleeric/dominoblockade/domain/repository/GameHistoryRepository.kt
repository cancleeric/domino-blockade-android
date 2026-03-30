package com.cancleeric.dominoblockade.domain.repository

import com.cancleeric.dominoblockade.data.local.GameRecord
import com.cancleeric.dominoblockade.domain.model.GameStats
import kotlinx.coroutines.flow.Flow

interface GameHistoryRepository {
    fun getAllRecords(): Flow<List<GameRecord>>
    fun getRecordById(id: Long): Flow<GameRecord?>
    fun getStats(): Flow<GameStats>
    suspend fun saveRecord(record: GameRecord)
    suspend fun clearAllRecords()
}

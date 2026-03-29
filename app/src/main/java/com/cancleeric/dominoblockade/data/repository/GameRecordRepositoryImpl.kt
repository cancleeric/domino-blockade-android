package com.cancleeric.dominoblockade.data.repository

import com.cancleeric.dominoblockade.data.local.dao.GameRecordDao
import com.cancleeric.dominoblockade.data.local.entity.GameRecordEntity
import com.cancleeric.dominoblockade.domain.repository.GameRecordRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GameRecordRepositoryImpl @Inject constructor(
    private val dao: GameRecordDao
) : GameRecordRepository {

    override suspend fun saveRecord(record: GameRecordEntity) {
        dao.insert(record)
    }

    override fun getAllRecords(): Flow<List<GameRecordEntity>> {
        return dao.getAll()
    }

    override fun getRecentRecords(limit: Int): Flow<List<GameRecordEntity>> {
        return dao.getRecent(limit)
    }
}

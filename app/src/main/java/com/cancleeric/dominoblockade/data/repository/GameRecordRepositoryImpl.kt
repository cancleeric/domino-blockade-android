package com.cancleeric.dominoblockade.data.repository

import com.cancleeric.dominoblockade.data.local.dao.GameRecordDao
import com.cancleeric.dominoblockade.data.local.entity.GameRecordEntity
import com.cancleeric.dominoblockade.domain.repository.GameRecordRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRecordRepositoryImpl @Inject constructor(
    private val dao: GameRecordDao
) : GameRecordRepository {

    override suspend fun insert(record: GameRecordEntity) = dao.insert(record)

    override fun getAll(): Flow<List<GameRecordEntity>> = dao.getAll()

    override fun getRecent(limit: Int): Flow<List<GameRecordEntity>> = dao.getRecent(limit)
}

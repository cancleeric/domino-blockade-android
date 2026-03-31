package com.cancleeric.dominoblockade.data.repository

import com.cancleeric.dominoblockade.data.local.dao.PlayerStatsDao
import com.cancleeric.dominoblockade.data.local.entity.PlayerStatsEntity
import com.cancleeric.dominoblockade.domain.repository.PlayerStatsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerStatsRepositoryImpl @Inject constructor(
    private val dao: PlayerStatsDao
) : PlayerStatsRepository {

    override suspend fun upsert(stats: PlayerStatsEntity) = dao.upsert(stats)

    override suspend fun getByName(name: String): PlayerStatsEntity? = dao.getByName(name)

    override fun getAll(): Flow<List<PlayerStatsEntity>> = dao.getAll()
}

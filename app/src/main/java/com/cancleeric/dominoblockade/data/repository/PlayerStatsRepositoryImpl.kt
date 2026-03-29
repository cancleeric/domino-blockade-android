package com.cancleeric.dominoblockade.data.repository

import com.cancleeric.dominoblockade.data.local.dao.PlayerStatsDao
import com.cancleeric.dominoblockade.data.local.entity.PlayerStatsEntity
import com.cancleeric.dominoblockade.domain.repository.PlayerStatsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PlayerStatsRepositoryImpl @Inject constructor(
    private val dao: PlayerStatsDao
) : PlayerStatsRepository {

    override suspend fun upsertStats(stats: PlayerStatsEntity) {
        dao.upsert(stats)
    }

    override suspend fun getStatsByName(name: String): PlayerStatsEntity? {
        return dao.getByName(name)
    }

    override fun getAllStats(): Flow<List<PlayerStatsEntity>> {
        return dao.getAll()
    }
}

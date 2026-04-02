package com.cancleeric.dominoblockade.data.repository

import com.cancleeric.dominoblockade.data.local.dao.AchievementDao
import com.cancleeric.dominoblockade.data.local.entity.AchievementEntity
import com.cancleeric.dominoblockade.domain.model.Achievement
import com.cancleeric.dominoblockade.domain.model.AchievementType
import com.cancleeric.dominoblockade.domain.repository.AchievementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AchievementRepositoryImpl @Inject constructor(
    private val dao: AchievementDao
) : AchievementRepository {

    override fun getAll(): Flow<List<Achievement>> = dao.getAll().map { entities ->
        AchievementType.entries.map { type ->
            val entity = entities.firstOrNull { it.type == type.name }
            Achievement(type = type, isUnlocked = entity?.isUnlocked ?: false, unlockedAt = entity?.unlockedAt)
        }
    }

    override suspend fun getAllUnlocked(): List<Achievement> =
        dao.getAllUnlocked().map { entity ->
            Achievement(
                type = AchievementType.valueOf(entity.type),
                isUnlocked = true,
                unlockedAt = entity.unlockedAt
            )
        }

    override suspend fun unlock(type: AchievementType) {
        dao.upsert(AchievementEntity(type = type.name, isUnlocked = true, unlockedAt = System.currentTimeMillis()))
    }

    override suspend fun isUnlocked(type: AchievementType): Boolean =
        dao.getByType(type.name)?.isUnlocked == true
}

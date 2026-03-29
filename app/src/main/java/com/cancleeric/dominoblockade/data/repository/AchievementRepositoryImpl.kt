package com.cancleeric.dominoblockade.data.repository

import com.cancleeric.dominoblockade.data.local.dao.AchievementDao
import com.cancleeric.dominoblockade.data.local.entity.AchievementEntity
import com.cancleeric.dominoblockade.domain.model.Achievement
import com.cancleeric.dominoblockade.domain.model.AchievementType
import com.cancleeric.dominoblockade.domain.repository.AchievementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AchievementRepositoryImpl @Inject constructor(
    private val dao: AchievementDao
) : AchievementRepository {

    override fun getAllAchievements(): Flow<List<Achievement>> =
        dao.getAll().map { entities ->
            val entityMap = entities.associateBy { it.type }
            AchievementType.entries.map { type ->
                val entity = entityMap[type.name]
                Achievement(
                    type = type,
                    isUnlocked = entity?.isUnlocked ?: false,
                    unlockedAt = entity?.unlockedAt
                )
            }
        }

    override fun getUnlockedAchievements(): Flow<List<Achievement>> =
        dao.getUnlocked().map { entities ->
            entities.mapNotNull { entity ->
                val type = AchievementType.entries.find { it.name == entity.type } ?: return@mapNotNull null
                Achievement(type = type, isUnlocked = true, unlockedAt = entity.unlockedAt)
            }
        }

    override suspend fun unlockAchievement(type: AchievementType) {
        val existing = dao.getByType(type.name)
        if (existing?.isUnlocked == true) return
        dao.upsert(
            AchievementEntity(
                type = type.name,
                isUnlocked = true,
                unlockedAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun isUnlocked(type: AchievementType): Boolean =
        dao.getByType(type.name)?.isUnlocked ?: false

    override suspend fun initializeAchievements() {
        val toSeed = AchievementType.entries.mapNotNull { type ->
            if (dao.getByType(type.name) == null) {
                AchievementEntity(type = type.name, isUnlocked = false)
            } else null
        }
        if (toSeed.isNotEmpty()) dao.upsertAll(toSeed)
    }
}

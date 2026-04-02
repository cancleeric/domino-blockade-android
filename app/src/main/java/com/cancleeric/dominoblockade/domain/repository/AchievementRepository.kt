package com.cancleeric.dominoblockade.domain.repository

import com.cancleeric.dominoblockade.domain.model.Achievement
import com.cancleeric.dominoblockade.domain.model.AchievementType
import kotlinx.coroutines.flow.Flow

interface AchievementRepository {
    fun getAll(): Flow<List<Achievement>>
    suspend fun getAllUnlocked(): List<Achievement>
    suspend fun unlock(type: AchievementType)
    suspend fun isUnlocked(type: AchievementType): Boolean
}

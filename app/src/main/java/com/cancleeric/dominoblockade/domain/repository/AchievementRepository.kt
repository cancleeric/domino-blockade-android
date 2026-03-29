package com.cancleeric.dominoblockade.domain.repository

import com.cancleeric.dominoblockade.domain.model.Achievement
import com.cancleeric.dominoblockade.domain.model.AchievementType
import kotlinx.coroutines.flow.Flow

interface AchievementRepository {
    /** Emits the full list of achievements (locked and unlocked) whenever it changes. */
    fun getAllAchievements(): Flow<List<Achievement>>

    /** Emits only unlocked achievements, ordered by unlock time descending. */
    fun getUnlockedAchievements(): Flow<List<Achievement>>

    /**
     * Unlocks the given achievement and persists the state.
     * If the achievement is already unlocked this is a no-op.
     */
    suspend fun unlockAchievement(type: AchievementType)

    /** Returns true if the achievement has already been unlocked. */
    suspend fun isUnlocked(type: AchievementType): Boolean

    /**
     * Seeds the repository with all known achievement types in a locked state
     * if they are not yet present in the database.
     */
    suspend fun initializeAchievements()
}

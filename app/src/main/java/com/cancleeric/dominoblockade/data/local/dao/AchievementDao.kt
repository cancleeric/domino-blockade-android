package com.cancleeric.dominoblockade.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cancleeric.dominoblockade.data.local.entity.AchievementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: AchievementEntity)

    @Query("SELECT * FROM achievements ORDER BY type ASC")
    fun getAll(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements WHERE type = :type LIMIT 1")
    suspend fun getByType(type: String): AchievementEntity?

    @Query("SELECT * FROM achievements WHERE isUnlocked = 1")
    suspend fun getAllUnlocked(): List<AchievementEntity>
}

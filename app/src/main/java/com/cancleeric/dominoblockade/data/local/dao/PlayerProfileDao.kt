package com.cancleeric.dominoblockade.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cancleeric.dominoblockade.data.local.entity.PlayerProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PlayerProfileEntity)

    @Query("SELECT * FROM player_profiles WHERE id = 1 LIMIT 1")
    fun getProfile(): Flow<PlayerProfileEntity?>
}

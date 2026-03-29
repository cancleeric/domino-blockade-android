package com.cancleeric.dominoblockade.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cancleeric.dominoblockade.data.local.entity.PlayerStatsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerStatsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(stats: PlayerStatsEntity)

    @Query("SELECT * FROM player_stats WHERE playerName = :name")
    suspend fun getByName(name: String): PlayerStatsEntity?

    @Query("SELECT * FROM player_stats ORDER BY wins DESC")
    fun getAll(): Flow<List<PlayerStatsEntity>>
}

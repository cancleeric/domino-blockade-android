package com.cancleeric.dominoblockade.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.cancleeric.dominoblockade.data.local.entity.GameRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameRecordDao {
    @Insert
    suspend fun insert(record: GameRecordEntity)

    @Query("SELECT * FROM game_records ORDER BY timestamp DESC")
    fun getAll(): Flow<List<GameRecordEntity>>

    @Query("SELECT * FROM game_records ORDER BY timestamp DESC LIMIT :limit")
    fun getRecent(limit: Int): Flow<List<GameRecordEntity>>
}

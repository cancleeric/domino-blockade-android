package com.cancleeric.dominoblockade.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameRecordDao {
    @Query("SELECT * FROM game_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<GameRecord>>

    @Query("SELECT * FROM game_records WHERE id = :id")
    fun getRecordById(id: Long): Flow<GameRecord?>

    @Query("SELECT * FROM game_records WHERE difficulty = :difficulty ORDER BY timestamp DESC")
    fun getRecordsByDifficulty(difficulty: String): Flow<List<GameRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: GameRecord)

    @Query("DELETE FROM game_records")
    suspend fun deleteAllRecords()
}

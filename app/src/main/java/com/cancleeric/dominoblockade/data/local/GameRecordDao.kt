package com.cancleeric.dominoblockade.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface GameRecordDao {
    @Insert
    suspend fun insert(record: GameRecord)

    @Query("SELECT COUNT(*) FROM game_records")
    suspend fun getTotalGames(): Int

    @Query("SELECT COUNT(*) FROM game_records WHERE winnerName != ''")
    suspend fun getTotalWins(): Int

    @Query("SELECT * FROM game_records ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestRecord(): GameRecord?
}

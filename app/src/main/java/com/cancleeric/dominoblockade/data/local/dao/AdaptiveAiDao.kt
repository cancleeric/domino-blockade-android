package com.cancleeric.dominoblockade.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cancleeric.dominoblockade.data.local.entity.AdaptiveAiGameEntity
import com.cancleeric.dominoblockade.data.local.entity.AdaptiveAiStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AdaptiveAiDao {
    @Insert
    suspend fun insertGame(game: AdaptiveAiGameEntity)

    @Query("SELECT * FROM adaptive_ai_games WHERE gameMode IN (:modes) ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentGames(modes: List<String>, limit: Int): List<AdaptiveAiGameEntity>

    @Query("SELECT * FROM adaptive_ai_state WHERE id = 1")
    fun observeState(): Flow<AdaptiveAiStateEntity?>

    @Query("SELECT * FROM adaptive_ai_state WHERE id = 1")
    suspend fun getState(): AdaptiveAiStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertState(state: AdaptiveAiStateEntity)
}

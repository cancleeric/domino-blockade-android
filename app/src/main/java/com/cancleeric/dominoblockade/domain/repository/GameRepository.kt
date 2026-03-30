package com.cancleeric.dominoblockade.domain.repository

import com.cancleeric.dominoblockade.domain.model.GameState
import kotlinx.coroutines.flow.Flow

interface GameRepository {
    fun getGameState(): Flow<GameState?>
    suspend fun saveGameState(gameState: GameState)
    suspend fun clearGameState()
}

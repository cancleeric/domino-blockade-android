package com.cancleeric.dominoblockade.data.repository

import com.cancleeric.dominoblockade.domain.model.GameState
import com.cancleeric.dominoblockade.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepositoryImpl @Inject constructor() : GameRepository {
    private val _gameState = MutableStateFlow<GameState?>(null)

    override fun getGameState(): Flow<GameState?> = _gameState

    override suspend fun saveGameState(gameState: GameState) {
        _gameState.value = gameState
    }

    override suspend fun clearGameState() {
        _gameState.value = null
    }
}

package com.cancleeric.dominoblockade.domain.repository

import com.cancleeric.dominoblockade.data.local.entity.AdaptiveAiGameEntity
import com.cancleeric.dominoblockade.domain.model.GameMode
import kotlinx.coroutines.flow.Flow

interface AdaptiveAiRepository {
    val currentLevel: Flow<Int>
    suspend fun getCurrentLevel(): Int
    suspend fun setCurrentLevel(level: Int)
    suspend fun insertGameResult(gameMode: GameMode, playerWon: Boolean)
    suspend fun getRecentGames(gameModes: List<GameMode>, limit: Int): List<AdaptiveAiGameEntity>
}

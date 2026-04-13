package com.cancleeric.dominoblockade.domain.usecase

import com.cancleeric.dominoblockade.domain.model.GameMode
import com.cancleeric.dominoblockade.domain.repository.AdaptiveAiRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

private const val WIN_RATE_INCREASE_THRESHOLD = 0.6f
private const val WIN_RATE_DECREASE_THRESHOLD = 0.4f
private const val RECENT_GAMES_WINDOW = 10
private const val LEVEL_STEP = 5

@Singleton
class AdaptiveAiManager @Inject constructor(
    private val repository: AdaptiveAiRepository
) {
    val currentLevel: Flow<Int> = repository.currentLevel

    suspend fun recordGameResult(gameMode: GameMode, playerWon: Boolean) {
        repository.insertGameResult(gameMode = gameMode, playerWon = playerWon)
        val recentGames = repository.getRecentGames(
            gameModes = listOf(GameMode.QUICK_MATCH, GameMode.TOURNAMENT),
            limit = RECENT_GAMES_WINDOW
        )
        if (recentGames.isEmpty()) return

        val winRate = recentGames.count { it.playerWon }.toFloat() / recentGames.size
        val currentLevel = repository.getCurrentLevel()
        val adjustedLevel = when {
            winRate > WIN_RATE_INCREASE_THRESHOLD -> (currentLevel + LEVEL_STEP).coerceAtMost(100)
            winRate < WIN_RATE_DECREASE_THRESHOLD -> (currentLevel - LEVEL_STEP).coerceAtLeast(0)
            else -> currentLevel
        }

        if (adjustedLevel != currentLevel) {
            repository.setCurrentLevel(adjustedLevel)
        }
    }
}

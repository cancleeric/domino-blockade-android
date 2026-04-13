package com.cancleeric.dominoblockade.domain.usecase

import com.cancleeric.dominoblockade.data.local.entity.AdaptiveAiGameEntity
import com.cancleeric.dominoblockade.domain.model.GameMode
import com.cancleeric.dominoblockade.domain.repository.AdaptiveAiRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AdaptiveAiManagerTest {

    private class FakeAdaptiveAiRepository(
        initialLevel: Int = 50,
        initialGames: List<AdaptiveAiGameEntity> = emptyList()
    ) : AdaptiveAiRepository {
        private val levelFlow = MutableStateFlow(initialLevel)
        private val games = initialGames.toMutableList()

        override val currentLevel: Flow<Int> = levelFlow

        override suspend fun getCurrentLevel(): Int = levelFlow.value

        override suspend fun setCurrentLevel(level: Int) {
            levelFlow.value = level
        }

        override suspend fun insertGameResult(gameMode: GameMode, playerWon: Boolean) {
            games.add(
                0,
                AdaptiveAiGameEntity(
                    gameMode = gameMode.value,
                    playerWon = playerWon
                )
            )
        }

        override suspend fun getRecentGames(gameModes: List<GameMode>, limit: Int): List<AdaptiveAiGameEntity> =
            games.filter { game -> gameModes.any { it.value == game.gameMode } }.take(limit)
    }

    @Test
    fun `recordGameResult increases AI level when recent win rate is above 60 percent`() = runTest {
        val repo = FakeAdaptiveAiRepository(
            initialGames = List(6) { AdaptiveAiGameEntity(gameMode = GameMode.QUICK_MATCH.value, playerWon = true) } +
                List(4) { AdaptiveAiGameEntity(gameMode = GameMode.QUICK_MATCH.value, playerWon = false) }
        )
        val manager = AdaptiveAiManager(repo)

        manager.recordGameResult(gameMode = GameMode.QUICK_MATCH, playerWon = true)

        assertEquals(55, repo.getCurrentLevel())
    }

    @Test
    fun `recordGameResult decreases AI level when recent win rate is below 40 percent`() = runTest {
        val repo = FakeAdaptiveAiRepository(
            initialGames = List(7) { AdaptiveAiGameEntity(gameMode = GameMode.TOURNAMENT.value, playerWon = false) } +
                List(3) { AdaptiveAiGameEntity(gameMode = GameMode.TOURNAMENT.value, playerWon = true) }
        )
        val manager = AdaptiveAiManager(repo)

        manager.recordGameResult(gameMode = GameMode.TOURNAMENT, playerWon = false)

        assertEquals(45, repo.getCurrentLevel())
    }

    @Test
    fun `recordGameResult keeps AI level unchanged when recent win rate is between 40 and 60 percent`() = runTest {
        val repo = FakeAdaptiveAiRepository(
            initialGames = List(5) { AdaptiveAiGameEntity(gameMode = GameMode.QUICK_MATCH.value, playerWon = true) } +
                List(5) { AdaptiveAiGameEntity(gameMode = GameMode.TOURNAMENT.value, playerWon = false) }
        )
        val manager = AdaptiveAiManager(repo)

        manager.recordGameResult(gameMode = GameMode.QUICK_MATCH, playerWon = false)

        assertEquals(50, repo.getCurrentLevel())
    }
}

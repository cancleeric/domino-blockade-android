package com.cancleeric.dominoblockade.data.repository

import com.cancleeric.dominoblockade.data.local.dao.AdaptiveAiDao
import com.cancleeric.dominoblockade.data.local.entity.AdaptiveAiGameEntity
import com.cancleeric.dominoblockade.data.local.entity.AdaptiveAiStateEntity
import com.cancleeric.dominoblockade.domain.model.GameMode
import com.cancleeric.dominoblockade.domain.repository.AdaptiveAiRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private const val DEFAULT_AI_LEVEL = 50
private const val STATE_ID = 1

@Singleton
class AdaptiveAiRepositoryImpl @Inject constructor(
    private val dao: AdaptiveAiDao
) : AdaptiveAiRepository {

    override val currentLevel: Flow<Int> = dao.observeState()
        .map { state -> state?.currentLevel ?: DEFAULT_AI_LEVEL }

    override suspend fun getCurrentLevel(): Int =
        dao.getState()?.currentLevel ?: DEFAULT_AI_LEVEL

    override suspend fun setCurrentLevel(level: Int) {
        dao.upsertState(
            AdaptiveAiStateEntity(
                id = STATE_ID,
                currentLevel = level.coerceIn(0, 100)
            )
        )
    }

    override suspend fun insertGameResult(gameMode: GameMode, playerWon: Boolean) {
        dao.insertGame(
            AdaptiveAiGameEntity(
                gameMode = gameMode.value,
                playerWon = playerWon
            )
        )
    }

    override suspend fun getRecentGames(gameModes: List<GameMode>, limit: Int): List<AdaptiveAiGameEntity> =
        dao.getRecentGames(modes = gameModes.map { it.value }, limit = limit)
}

package com.cancleeric.dominoblockade.ai

import com.cancleeric.dominoblockade.domain.model.GameState
import com.cancleeric.dominoblockade.domain.model.Player

private const val EASY_MAX_LEVEL = 33
private const val MEDIUM_MAX_LEVEL = 66

class AdaptiveAiStrategy(
    private val level: Int,
    private val easyAi: AiStrategy = EasyAi(),
    private val mediumAi: AiStrategy = MediumAi(),
    private val hardAi: AiStrategy = HardAi()
) : AiStrategy {
    override fun chooseMove(state: GameState, player: Player, validMoves: List<ValidMove>): ValidMove? {
        val clampedLevel = level.coerceIn(0, 100)
        val strategy = when {
            clampedLevel <= EASY_MAX_LEVEL -> easyAi
            clampedLevel <= MEDIUM_MAX_LEVEL -> mediumAi
            else -> hardAi
        }
        return strategy.chooseMove(state, player, validMoves)
    }
}

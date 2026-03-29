package com.cancleeric.dominoblockade.ai

import com.cancleeric.dominoblockade.domain.model.GameState
import com.cancleeric.dominoblockade.domain.model.Player
import com.cancleeric.dominoblockade.domain.usecase.ValidMove
import kotlinx.coroutines.delay

/**
 * Wraps an [AiStrategy] with a configurable thinking delay to simulate natural
 * AI response time in the UI.
 */
class AiPlayer(
    private val strategy: AiStrategy,
    private val thinkingDelayMs: Long = 1000
) {
    suspend fun makeMove(state: GameState, player: Player, validMoves: List<ValidMove>): ValidMove? {
        delay(thinkingDelayMs)
        return strategy.chooseMove(state, player, validMoves)
    }
}

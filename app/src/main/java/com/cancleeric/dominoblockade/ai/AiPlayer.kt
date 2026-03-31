package com.cancleeric.dominoblockade.ai

import com.cancleeric.dominoblockade.domain.model.GameState
import com.cancleeric.dominoblockade.domain.model.Player
import kotlinx.coroutines.delay

/**
 * Wraps an [AiStrategy] with a configurable thinking delay to simulate a realistic opponent.
 *
 * @property strategy The underlying AI strategy used to choose a move.
 * @property thinkingDelayMs How long (in milliseconds) to pause before returning the chosen move.
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

package com.cancleeric.dominoblockade.ai

import com.cancleeric.dominoblockade.domain.model.GameState
import com.cancleeric.dominoblockade.domain.model.Player
import com.cancleeric.dominoblockade.domain.usecase.ValidMove

/**
 * Easy AI strategy: selects a move at random from the list of valid moves.
 * Provides no strategic logic and serves as the entry-level difficulty for new players.
 */
class EasyAi : AiStrategy {
    override fun chooseMove(state: GameState, player: Player, validMoves: List<ValidMove>): ValidMove? {
        return validMoves.randomOrNull()
    }
}

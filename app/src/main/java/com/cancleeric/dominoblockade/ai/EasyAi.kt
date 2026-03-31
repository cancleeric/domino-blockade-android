package com.cancleeric.dominoblockade.ai

import com.cancleeric.dominoblockade.domain.model.GameState
import com.cancleeric.dominoblockade.domain.model.Player

/** Easy AI: selects a random valid move, or returns null when no moves are available. */
class EasyAi : AiStrategy {
    override fun chooseMove(state: GameState, player: Player, validMoves: List<ValidMove>): ValidMove? =
        validMoves.randomOrNull()
}

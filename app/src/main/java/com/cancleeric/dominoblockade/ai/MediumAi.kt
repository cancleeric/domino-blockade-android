package com.cancleeric.dominoblockade.ai

import com.cancleeric.dominoblockade.domain.model.GameState
import com.cancleeric.dominoblockade.domain.model.Player

/**
 * Medium AI strategy:
 * 1. Prefers double tiles (to shed them early since they connect on one specific value only).
 * 2. Among doubles (or all moves when no doubles), prefers the tile with the highest pip count.
 * 3. Returns null when no valid moves exist (signals the game loop to draw from the boneyard).
 */
class MediumAi : AiStrategy {
    override fun chooseMove(state: GameState, player: Player, validMoves: List<ValidMove>): ValidMove? {
        if (validMoves.isEmpty()) return null
        val doubles = validMoves.filter { it.domino.isDouble }
        val candidates = if (doubles.isNotEmpty()) doubles else validMoves
        return candidates.maxByOrNull { it.domino.total }
    }
}

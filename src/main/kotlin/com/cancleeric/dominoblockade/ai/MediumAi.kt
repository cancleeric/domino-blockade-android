package com.cancleeric.dominoblockade.ai

import com.cancleeric.dominoblockade.domain.model.GameState
import com.cancleeric.dominoblockade.domain.model.Player
import com.cancleeric.dominoblockade.domain.usecase.ValidMove

/**
 * Medium AI strategy:
 * 1. Prioritises doubles (harder to play later since they only match one value)
 * 2. Among eligible moves, plays the domino with the highest total pip count
 * 3. Returns null when the list is empty (caller should draw)
 */
class MediumAi : AiStrategy {
    override fun chooseMove(state: GameState, player: Player, validMoves: List<ValidMove>): ValidMove? {
        if (validMoves.isEmpty()) return null
        val doubles = validMoves.filter { it.domino.isDouble }
        val candidates = if (doubles.isNotEmpty()) doubles else validMoves
        return candidates.maxByOrNull { it.domino.totalPips }
    }
}

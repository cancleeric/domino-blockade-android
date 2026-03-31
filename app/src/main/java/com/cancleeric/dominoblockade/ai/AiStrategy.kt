package com.cancleeric.dominoblockade.ai

import com.cancleeric.dominoblockade.domain.model.GameState
import com.cancleeric.dominoblockade.domain.model.Player

interface AiStrategy {
    fun chooseMove(state: GameState, player: Player, validMoves: List<ValidMove>): ValidMove?
}

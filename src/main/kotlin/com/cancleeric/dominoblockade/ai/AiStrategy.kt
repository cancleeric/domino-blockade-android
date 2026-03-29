package com.cancleeric.dominoblockade.ai

import com.cancleeric.dominoblockade.domain.model.GameState
import com.cancleeric.dominoblockade.domain.model.Player
import com.cancleeric.dominoblockade.domain.usecase.ValidMove

interface AiStrategy {
    fun chooseMove(state: GameState, player: Player, validMoves: List<ValidMove>): ValidMove?
}

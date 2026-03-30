package com.cancleeric.dominoblockade.ai

import com.cancleeric.dominoblockade.domain.model.Domino
import com.cancleeric.dominoblockade.domain.model.GameState

interface AiStrategy {
    fun selectMove(hand: List<Domino>, gameState: GameState): Domino?
}

package com.cancleeric.dominoblockade.domain.usecase

import com.cancleeric.dominoblockade.domain.model.GameState

/**
 * Detects whether the game has reached a blockade (deadlock) condition.
 *
 * A blockade occurs when:
 * 1. The boneyard is empty (no more tiles to draw), AND
 * 2. No player has any legal move.
 */
class BlockadeDetector {

    private val validateMoveUseCase = ValidateMoveUseCase()

    fun isBlocked(state: GameState): Boolean {
        if (state.boneyard.isNotEmpty()) return false
        return state.players.all { player ->
            validateMoveUseCase.getValidMoves(state, player).isEmpty()
        }
    }
}

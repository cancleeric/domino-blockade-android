package com.cancleeric.dominoblockade.domain.usecase

import com.cancleeric.dominoblockade.domain.model.Domino
import com.cancleeric.dominoblockade.domain.model.GameState
import com.cancleeric.dominoblockade.domain.model.Player

/**
 * Validates domino moves against the current board state and enumerates all legal moves
 * for a given player.
 *
 * Board orientation convention:
 * - [GameState.board].first().left  = exposed left-end value
 * - [GameState.board].last().right  = exposed right-end value
 *
 * When placing on LEFT: the domino's right side must connect to the current left end.
 * When placing on RIGHT: the domino's left side must connect to the current right end.
 */
class ValidateMoveUseCase {

    /**
     * Returns true if [domino] can legally be played onto [end] of the board in [state].
     */
    fun validate(state: GameState, domino: Domino, end: BoardEnd): Boolean {
        if (state.board.isEmpty()) return true
        return when (end) {
            BoardEnd.LEFT -> {
                val leftEnd = state.leftEnd!!
                domino.left == leftEnd || domino.right == leftEnd
            }
            BoardEnd.RIGHT -> {
                val rightEnd = state.rightEnd!!
                domino.left == rightEnd || domino.right == rightEnd
            }
        }
    }

    /**
     * Returns all legal [ValidMove]s available to [player] given the current [state].
     *
     * When the board is empty every tile is valid and is listed as a LEFT placement
     * without flipping (the first tile establishes the board orientation).
     */
    fun getValidMoves(state: GameState, player: Player): List<ValidMove> {
        if (state.board.isEmpty()) {
            return player.hand.map { domino -> ValidMove(domino, BoardEnd.LEFT, false) }
        }

        val leftEnd = state.leftEnd!!
        val rightEnd = state.rightEnd!!
        val moves = mutableListOf<ValidMove>()

        for (domino in player.hand) {
            // LEFT end: domino.right must connect to leftEnd after optional flip
            when {
                domino.right == leftEnd -> moves.add(ValidMove(domino, BoardEnd.LEFT, false))
                domino.left == leftEnd -> moves.add(ValidMove(domino, BoardEnd.LEFT, true))
            }
            // RIGHT end: domino.left must connect to rightEnd after optional flip
            when {
                domino.left == rightEnd -> moves.add(ValidMove(domino, BoardEnd.RIGHT, false))
                domino.right == rightEnd -> moves.add(ValidMove(domino, BoardEnd.RIGHT, true))
            }
        }
        return moves
    }
}

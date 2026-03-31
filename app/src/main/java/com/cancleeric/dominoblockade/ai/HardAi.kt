package com.cancleeric.dominoblockade.ai

import com.cancleeric.dominoblockade.domain.model.Domino
import com.cancleeric.dominoblockade.domain.model.GameState
import com.cancleeric.dominoblockade.domain.model.Player

/**
 * Hard AI strategy:
 * - Card counting: derives the set of tiles each opponent might still hold by comparing the full
 *   domino set against the tiles visible on the board and in the AI player's own hand.
 * - Blocking: prefers moves that expose a board end value that opponents are least likely to match.
 * - High-pip tie-breaker: among equally good blocking moves, sheds the highest-pip tile first.
 * - Returns null when no valid moves exist.
 */
class HardAi : AiStrategy {

    override fun chooseMove(state: GameState, player: Player, validMoves: List<ValidMove>): ValidMove? {
        if (validMoves.isEmpty()) return null
        val opponentPossible = getOpponentPossibleDominoes(state, player)
        return selectBestMove(validMoves, opponentPossible)
    }

    private fun getOpponentPossibleDominoes(state: GameState, player: Player): Set<Domino> {
        val played = state.board.map { it.normalized() }.toSet()
        val inHand = player.hand.map { it.normalized() }.toSet()
        return generateFullSet() - played - inHand
    }

    private fun selectBestMove(validMoves: List<ValidMove>, opponentPossible: Set<Domino>): ValidMove =
        validMoves.sortedWith(
            compareBy<ValidMove> { move ->
                opponentPossible.count { it.left == move.newExposedEnd || it.right == move.newExposedEnd }
            }.thenByDescending { it.domino.total }
        ).first()

    private fun generateFullSet(): Set<Domino> {
        val result = mutableSetOf<Domino>()
        for (i in 0..6) {
            for (j in i..6) {
                result.add(Domino(i, j))
            }
        }
        return result
    }

    private fun Domino.normalized(): Domino = if (left <= right) this else Domino(right, left)
}

package com.cancleeric.dominoblockade.ai

import com.cancleeric.dominoblockade.domain.model.DominoSet
import com.cancleeric.dominoblockade.domain.model.GameState
import com.cancleeric.dominoblockade.domain.model.Player
import com.cancleeric.dominoblockade.domain.usecase.BoardEnd
import com.cancleeric.dominoblockade.domain.usecase.ValidMove

/**
 * Hard AI strategy:
 * - Tracks every domino placed on the board (card counting)
 * - Infers which values opponents are likely to be short on
 * - Favours moves that expose an end value opponents probably cannot match (blocking)
 * - Among equally-blocking moves, sheds the highest-pip domino
 */
class HardAi : AiStrategy {

    private val trackedBoardIds = mutableSetOf<Int>()

    override fun chooseMove(state: GameState, player: Player, validMoves: List<ValidMove>): ValidMove? {
        if (validMoves.isEmpty()) return null

        // Update card-count knowledge from the current board
        state.board.forEach { trackedBoardIds.add(it.id) }

        // Determine dominoes whose location is unknown (not in our hand and not on the board)
        val knownIds = player.hand.map { it.id }.toSet() + trackedBoardIds
        val unknownDominoes = DominoSet.createFullSet().filter { it.id !in knownIds }

        // For each pip value, count how many unknown dominoes contain it.
        // Fewer unknown dominoes with that value → opponents are less likely to hold it
        // → placing that value at an open end is a better blocking move.
        val unknownCountByValue: Map<Int, Int> = (0..6).associateWith { v ->
            unknownDominoes.count { it.hasValue(v) }
        }

        return validMoves.maxByOrNull { move ->
            val newEndValue = newEndValue(move)
            // Blocking score: fewer unknowns for this value means better block
            val blockingScore = (7 - (unknownCountByValue[newEndValue] ?: 7)) * 100
            // Secondary criterion: shed high-pip dominoes
            val pipScore = move.domino.totalPips
            blockingScore + pipScore
        }
    }

    /** Returns the pip value that will be exposed at the board end after the move is played. */
    private fun newEndValue(move: ValidMove): Int = when {
        move.end == BoardEnd.LEFT && !move.needsFlip -> move.domino.left
        move.end == BoardEnd.LEFT && move.needsFlip -> move.domino.right
        move.end == BoardEnd.RIGHT && !move.needsFlip -> move.domino.right
        else -> move.domino.left // RIGHT + needsFlip
    }

    /** Exposes tracked board state for testing purposes. */
    fun trackedBoardSize(): Int = trackedBoardIds.size
}

package com.cancleeric.dominoblockade.ai

import com.cancleeric.dominoblockade.domain.model.Domino

/**
 * Represents a legal move: a specific domino played onto a specific board end,
 * with a flag indicating whether the domino must be flipped before placement.
 *
 * Orientation convention:
 * - LEFT placement: the domino's right side connects to the current left end of the board.
 *   If [needsFlip] is true the domino is placed with its left side connecting.
 * - RIGHT placement: the domino's left side connects to the current right end of the board.
 *   If [needsFlip] is true the domino is placed with its right side connecting.
 *
 * [newExposedEnd] is the pip value that will be exposed after the move.
 */
data class ValidMove(
    val domino: Domino,
    val end: BoardEnd,
    val needsFlip: Boolean
) {
    val newExposedEnd: Int
        get() = when {
            end == BoardEnd.LEFT && !needsFlip -> domino.left
            end == BoardEnd.LEFT -> domino.right
            end == BoardEnd.RIGHT && !needsFlip -> domino.right
            else -> domino.left
        }
}

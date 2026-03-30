package com.cancleeric.dominoblockade.domain.usecase

import com.cancleeric.dominoblockade.domain.model.Domino

/**
 * Represents a legal move: a specific domino played onto a specific board end,
 * with a flag indicating whether the domino must be flipped before placement.
 *
 * Orientation convention:
 * - LEFT placement: the domino's right side connects to the current left end of the board.
 *   If [needsFlip] is true the domino is flipped so that its original left side connects.
 * - RIGHT placement: the domino's left side connects to the current right end of the board.
 *   If [needsFlip] is true the domino is flipped so that its original right side connects.
 */
data class ValidMove(
    val domino: Domino,
    val end: BoardEnd,
    val needsFlip: Boolean
)

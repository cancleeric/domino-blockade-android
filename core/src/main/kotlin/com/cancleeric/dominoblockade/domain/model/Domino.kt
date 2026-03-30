package com.cancleeric.dominoblockade.domain.model

/**
 * Represents a single domino tile with left and right pip values (0–6).
 */
data class Domino(val left: Int, val right: Int) {
    /** Total pip count on this tile. */
    val pips: Int get() = left + right

    /** True if both sides have the same value. */
    val isDouble: Boolean get() = left == right

    /** Returns this domino with left and right values swapped. */
    fun flipped(): Domino = Domino(right, left)
}

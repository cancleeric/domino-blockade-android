package com.cancleeric.dominoblockade.domain.model

/**
 * Represents a single domino tile with two pip values.
 */
data class Domino(
    val left: Int,
    val right: Int
) {
    val total: Int get() = left + right

    fun isDouble(): Boolean = left == right

    fun canConnectTo(value: Int): Boolean = left == value || right == value

    fun flipped(): Domino = Domino(right, left)

    override fun toString(): String = "[$left|$right]"
}

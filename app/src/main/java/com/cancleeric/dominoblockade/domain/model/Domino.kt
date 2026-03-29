package com.cancleeric.dominoblockade.domain.model

data class Domino(
    val left: Int,
    val right: Int
) {
    val isDouble: Boolean get() = left == right
    val totalPips: Int get() = left + right

    fun flipped(): Domino = Domino(right, left)

    fun canConnectTo(value: Int): Boolean = left == value || right == value

    fun orientedFor(connectionValue: Int): Domino {
        return if (left == connectionValue) this else flipped()
    }
}

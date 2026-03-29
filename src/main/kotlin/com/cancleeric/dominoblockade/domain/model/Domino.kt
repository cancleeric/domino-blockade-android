package com.cancleeric.dominoblockade.domain.model

data class Domino(
    val left: Int,
    val right: Int,
    val id: Int
) {
    val isDouble: Boolean get() = left == right
    val totalPips: Int get() = left + right
    fun hasValue(value: Int): Boolean = left == value || right == value
    fun flip(): Domino = copy(left = right, right = left)
}

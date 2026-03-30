package com.cancleeric.dominoblockade.domain.model

data class Domino(
    val left: Int,
    val right: Int
) {
    val total: Int get() = left + right
    val isDouble: Boolean get() = left == right
}

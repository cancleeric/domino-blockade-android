package com.cancleeric.dominoblockade.domain.model

data class Player(
    val id: Int,
    val name: String,
    val hand: List<Domino> = emptyList(),
    val isAi: Boolean = false,
    val score: Int = 0
) {
    val handPipCount: Int get() = hand.sumOf { it.totalPips }
}

package com.cancleeric.dominoblockade.domain.model

/**
 * Represents a player in the game.
 */
data class Player(
    val id: Int,
    val name: String,
    val hand: List<Domino> = emptyList()
) {
    val handScore: Int get() = hand.sumOf { it.total }
    val handSize: Int get() = hand.size
    val hasEmptyHand: Boolean get() = hand.isEmpty()
}

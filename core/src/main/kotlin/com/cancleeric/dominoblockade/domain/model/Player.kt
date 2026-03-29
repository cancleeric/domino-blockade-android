package com.cancleeric.dominoblockade.domain.model

/**
 * Represents a player in the game.
 *
 * @property id Unique identifier for the player.
 * @property name Display name.
 * @property hand The domino tiles currently in the player's hand.
 */
data class Player(
    val id: String,
    val name: String,
    val hand: List<Domino> = emptyList()
)

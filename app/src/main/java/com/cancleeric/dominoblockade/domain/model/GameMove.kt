package com.cancleeric.dominoblockade.domain.model

/**
 * Represents a single domino placement during a game.
 * Used for recording move history that can be replayed.
 *
 * @param playerId   Unique identifier of the player who made the move.
 * @param playerName Display name of the player who made the move.
 * @param dominoLeft Left pip value of the placed domino (before any flip).
 * @param dominoRight Right pip value of the placed domino (before any flip).
 * @param boardEnd   Where the domino was placed: "INITIAL" (first tile), "LEFT", or "RIGHT".
 * @param timestamp  Unix epoch milliseconds when the move was recorded.
 */
data class GameMove(
    val playerId: String,
    val playerName: String,
    val dominoLeft: Int,
    val dominoRight: Int,
    val boardEnd: String,
    val timestamp: Long = System.currentTimeMillis()
)

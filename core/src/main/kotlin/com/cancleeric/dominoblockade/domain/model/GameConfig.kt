package com.cancleeric.dominoblockade.domain.model

/**
 * Configuration required to start a new game.
 *
 * @property players List of players (2–4 players supported).
 * @property randomSeed Optional seed for reproducible shuffles (useful for testing).
 */
data class GameConfig(
    val players: List<Player>,
    val randomSeed: Long? = null
) {
    init {
        require(players.size in 2..4) { "Player count must be between 2 and 4, got ${players.size}" }
    }
}

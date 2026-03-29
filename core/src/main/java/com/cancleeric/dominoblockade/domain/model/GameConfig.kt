package com.cancleeric.dominoblockade.domain.model

/**
 * Configuration for a multiplayer game.
 * Supports 2–4 players with automatic hand size adjustment.
 */
data class GameConfig(
    val playerCount: Int,
    val playerNames: List<String>
) {
    init {
        require(playerCount in 2..4) { "Player count must be between 2 and 4" }
        require(playerNames.size == playerCount) {
            "Number of player names (${playerNames.size}) must match player count ($playerCount)"
        }
    }

    /** Initial hand size: 7 for 2 players, 5 for 3–4 players. */
    val initialHandSize: Int get() = if (playerCount == 2) 7 else 5

    companion object {
        fun twoPlayer(p1: String = "Player 1", p2: String = "Player 2") =
            GameConfig(2, listOf(p1, p2))

        fun threePlayer(
            p1: String = "Player 1",
            p2: String = "Player 2",
            p3: String = "Player 3"
        ) = GameConfig(3, listOf(p1, p2, p3))

        fun fourPlayer(
            p1: String = "Player 1",
            p2: String = "Player 2",
            p3: String = "Player 3",
            p4: String = "Player 4"
        ) = GameConfig(4, listOf(p1, p2, p3, p4))
    }
}

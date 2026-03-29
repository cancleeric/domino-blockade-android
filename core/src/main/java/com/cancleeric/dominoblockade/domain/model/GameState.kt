package com.cancleeric.dominoblockade.domain.model

/**
 * The current phase of the game.
 */
sealed class GamePhase {
    /** Game has not started yet. */
    object Idle : GamePhase()

    /** A player is taking their turn. */
    data class PlayerTurn(val playerIndex: Int) : GamePhase()

    /**
     * Showing the "Pass & Play" handoff screen.
     * The previous player's hand is hidden; the next player must confirm
     * before seeing their hand.
     */
    data class PassAndPlay(
        val previousPlayerIndex: Int,
        val nextPlayerIndex: Int
    ) : GamePhase()

    /** The game is over. */
    data class GameOver(
        val winnerIndex: Int?,          // null = draw / blockade with tie
        val scores: Map<Int, Int>       // playerIndex → remaining hand score
    ) : GamePhase()
}

/**
 * Complete game state snapshot.
 */
data class GameState(
    val players: List<Player>,
    val board: List<Domino> = emptyList(),
    val drawPile: List<Domino> = emptyList(),
    val currentPlayerIndex: Int = 0,
    val phase: GamePhase = GamePhase.Idle,
    /** Left-most open end value on the board (null if board is empty). */
    val boardLeftEnd: Int? = null,
    /** Right-most open end value on the board (null if board is empty). */
    val boardRightEnd: Int? = null
) {
    val currentPlayer: Player get() = players[currentPlayerIndex]

    val isGameOver: Boolean get() = phase is GamePhase.GameOver

    val isBlockade: Boolean
        get() {
            if (drawPile.isNotEmpty()) return false
            return players.none { player ->
                if (board.isEmpty()) return@none true
                player.hand.any { domino ->
                    domino.canConnectTo(boardLeftEnd!!) || domino.canConnectTo(boardRightEnd!!)
                }
            }
        }
}

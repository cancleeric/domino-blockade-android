package com.cancleeric.dominoblockade.domain.model

/**
 * Immutable snapshot of the entire game state at a given point in time.
 *
 * The board is stored as an ordered list of dominoes where:
 * - [board].first().left  = the exposed left end value
 * - [board].last().right  = the exposed right end value
 *
 * @property players All players with their current hands.
 * @property board Domino tiles placed on the table in order.
 * @property boneyard Remaining tiles available to draw.
 * @property currentPlayerIndex Index into [players] indicating whose turn it is.
 * @property isGameOver True once the game has ended.
 * @property winner The winning player, or null if the game is still in progress.
 */
data class GameState(
    val players: List<Player>,
    val board: List<Domino>,
    val boneyard: List<Domino>,
    val currentPlayerIndex: Int,
    val isGameOver: Boolean = false,
    val winner: Player? = null
) {
    /** The player whose turn it currently is. */
    val currentPlayer: Player get() = players[currentPlayerIndex]

    /** The exposed pip value on the left end of the board, or null if the board is empty. */
    val leftEnd: Int? get() = board.firstOrNull()?.left

    /** The exposed pip value on the right end of the board, or null if the board is empty. */
    val rightEnd: Int? get() = board.lastOrNull()?.right
}

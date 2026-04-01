package com.cancleeric.dominoblockade.domain.model

/**
 * Computes the resulting [GameState] after placing [domino] on the board for the current player,
 * returning null if the domino cannot be legally placed at either end.
 *
 * The domino is automatically oriented so its connecting side faces the chosen end.
 * The current player's hand is updated by removing [domino].
 */
fun GameState.computePlacement(domino: Domino): GameState? {
    val updatedPlayers = players.toMutableList().also {
        it[currentPlayerIndex] = currentPlayer.copy(hand = currentPlayer.hand - domino)
    }
    val base = copy(players = updatedPlayers)
    return when {
        board.isEmpty() ->
            base.copy(board = listOf(domino), leftEnd = domino.left, rightEnd = domino.right)
        domino.left == rightEnd ->
            base.copy(board = board + domino, rightEnd = domino.right)
        domino.right == rightEnd ->
            base.copy(board = board + Domino(domino.right, domino.left), rightEnd = domino.left)
        domino.right == leftEnd ->
            base.copy(board = listOf(domino) + board, leftEnd = domino.left)
        domino.left == leftEnd ->
            base.copy(
                board = listOf(Domino(domino.right, domino.left)) + board,
                leftEnd = domino.right
            )
        else -> null
    }
}

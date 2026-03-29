package com.cancleeric.dominoblockade.domain.model

data class GameState(
    val players: List<Player>,
    val board: List<PlacedDomino>,
    val drawPile: List<Domino>,
    val currentPlayerIndex: Int,
    val phase: GamePhase,
    val winnerIndex: Int? = null,
    val turnNumber: Int = 0
) {
    val currentPlayer: Player get() = players[currentPlayerIndex]
    val leftEnd: Int? get() = board.firstOrNull()?.domino?.left
    val rightEnd: Int? get() = board.lastOrNull()?.domino?.right

    fun validMovesFor(player: Player): List<Pair<Domino, BoardEnd>> {
        if (board.isEmpty()) {
            return player.hand.map { it to BoardEnd.LEFT }
        }
        val moves = mutableListOf<Pair<Domino, BoardEnd>>()
        val left = leftEnd ?: return emptyList()
        val right = rightEnd ?: return emptyList()
        for (domino in player.hand) {
            if (domino.canConnectTo(left)) moves.add(domino to BoardEnd.LEFT)
            if (domino.canConnectTo(right)) {
                if (left != right || !domino.canConnectTo(left)) {
                    moves.add(domino to BoardEnd.RIGHT)
                } else if (!moves.any { it.first == domino && it.second == BoardEnd.RIGHT }) {
                    moves.add(domino to BoardEnd.RIGHT)
                }
            }
        }
        return moves
    }
}

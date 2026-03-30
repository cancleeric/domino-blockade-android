package com.cancleeric.dominoblockade.domain.model

data class GameState(
    val players: List<Player>,
    val board: List<Domino> = emptyList(),
    val boneyard: List<Domino> = emptyList(),
    val currentPlayerIndex: Int = 0,
    val isGameOver: Boolean = false,
    val winner: Player? = null,
    val leftEnd: Int? = null,
    val rightEnd: Int? = null,
    val isBlocked: Boolean = false
) {
    val currentPlayer: Player get() = players[currentPlayerIndex]

    fun canPlace(domino: Domino): Boolean {
        if (board.isEmpty()) return true
        val left = leftEnd ?: return true
        val right = rightEnd ?: return true
        return domino.left == left || domino.right == left ||
            domino.left == right || domino.right == right
    }
}

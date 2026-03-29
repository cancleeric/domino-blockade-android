package com.cancleeric.dominoblockade.domain.model

data class GameState(
    val players: List<Player>,
    val board: List<Domino> = emptyList(),
    val boneyard: List<Domino> = emptyList(),
    val currentPlayerIndex: Int = 0,
    val isGameOver: Boolean = false,
    val winner: Player? = null
) {
    val currentPlayer: Player get() = players[currentPlayerIndex]
}

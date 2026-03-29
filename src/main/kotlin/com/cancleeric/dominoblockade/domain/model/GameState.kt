package com.cancleeric.dominoblockade.domain.model

data class GameState(
    val players: List<Player>,
    val board: List<Domino> = emptyList(),
    val drawPile: List<Domino> = emptyList(),
    val currentPlayerIndex: Int = 0,
    val leftEnd: Int? = null,
    val rightEnd: Int? = null,
    val status: GameStatus = GameStatus.WAITING
)

enum class GameStatus {
    WAITING,
    PLAYING,
    BLOCKED,
    FINISHED
}

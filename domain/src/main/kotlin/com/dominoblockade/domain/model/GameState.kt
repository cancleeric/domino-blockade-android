package com.dominoblockade.domain.model

data class GameState(
    val players: List<Player>,
    val board: List<Domino> = emptyList(),    // 場上已出骨牌鏈
    val drawPile: List<Domino> = emptyList(), // 剩餘牌堆
    val currentPlayerIndex: Int = 0,
    val leftEnd: Int? = null,    // 骨牌鏈左端點數
    val rightEnd: Int? = null,   // 骨牌鏈右端點數
    val status: GameStatus = GameStatus.WAITING
)

enum class GameStatus {
    WAITING,    // 等待開始
    PLAYING,    // 進行中
    BLOCKED,    // 封鎖
    FINISHED    // 結束
}

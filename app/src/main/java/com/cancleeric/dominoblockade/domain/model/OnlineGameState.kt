package com.cancleeric.dominoblockade.domain.model

enum class OnlineGameStatus {
    WAITING,
    IN_PROGRESS,
    BLOCKED,
    FINISHED
}

/**
 * Game state synced via Firebase Realtime Database for online multiplayer.
 * Separate from the local [GameState] to avoid coupling online/offline flows.
 */
data class OnlineGameState(
    val roomId: String,
    val boardLeftEnd: Int = -1,
    val boardRightEnd: Int = -1,
    val boardSize: Int = 0,
    val playerHandCounts: Map<String, Int> = emptyMap(),
    val currentPlayerId: String = "",
    val drawPileCount: Int = 0,
    val status: OnlineGameStatus = OnlineGameStatus.WAITING,
    val winnerId: String? = null,
    val lastMoveTimestamp: Long = 0L,
    val version: Long = 0L
)

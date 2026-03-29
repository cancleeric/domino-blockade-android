package com.cancleeric.dominoblockade.domain.model

data class GameConfig(
    val playerCount: Int = 2,
    val dominoesPerPlayer: Int = 7,
    val aiDifficulty: AiDifficulty = AiDifficulty.MEDIUM
)

/** Difficulty level used to select the AI strategy for a game. */
enum class AiDifficulty {
    /** Random move selection — no strategic logic. */
    EASY,
    /** Prioritises doubles and high-pip dominoes to minimise points at game end. */
    MEDIUM,
    /** Uses card counting and blocking strategy to maximise win probability. */
    HARD
}

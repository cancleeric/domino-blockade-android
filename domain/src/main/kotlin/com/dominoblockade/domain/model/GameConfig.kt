package com.dominoblockade.domain.model

data class GameConfig(
    val playerCount: Int = 2,           // 2-4 人
    val dominoesPerPlayer: Int = 7,     // 2人=7張, 3-4人=5張
    val aiDifficulty: AiDifficulty = AiDifficulty.MEDIUM
)

enum class AiDifficulty { EASY, MEDIUM, HARD }

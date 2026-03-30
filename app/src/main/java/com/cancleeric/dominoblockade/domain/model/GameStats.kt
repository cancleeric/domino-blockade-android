package com.cancleeric.dominoblockade.domain.model

data class GameStats(
    val totalGames: Int = 0,
    val totalWins: Int = 0,
    val winRate: Float = 0f,
    val bestScore: Int = 0,
    val consecutiveWins: Int = 0,
    val easyStats: DifficultyStats = DifficultyStats(),
    val mediumStats: DifficultyStats = DifficultyStats(),
    val hardStats: DifficultyStats = DifficultyStats()
)

data class DifficultyStats(
    val totalGames: Int = 0,
    val totalWins: Int = 0,
    val winRate: Float = 0f,
    val bestScore: Int = 0
)

package com.cancleeric.dominoblockade.domain.model

data class GameResult(
    val isWin: Boolean,
    val isBlocked: Boolean,
    val totalWins: Int,
    val totalGames: Int
)

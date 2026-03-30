package com.cancleeric.dominoblockade.domain.model

data class OnlinePlayer(
    val uid: String,
    val displayName: String,
    val eloRating: Int = 1000,
    val wins: Int = 0,
    val losses: Int = 0,
    val totalGames: Int = 0
) {
    val winRate: Float
        get() = if (totalGames == 0) 0f else wins.toFloat() / totalGames
}

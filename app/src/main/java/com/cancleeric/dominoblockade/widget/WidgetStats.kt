package com.cancleeric.dominoblockade.widget

data class WidgetStats(
    val totalGames: Int = 0,
    val totalWins: Int = 0
) {
    val winRate: Float
        get() = if (totalGames > 0) totalWins.toFloat() / totalGames else 0f

    val winRatePercent: Int
        get() = (winRate * 100).toInt()
}

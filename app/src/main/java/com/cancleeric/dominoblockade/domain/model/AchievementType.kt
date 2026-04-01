package com.cancleeric.dominoblockade.domain.model

enum class AchievementType(val title: String, val description: String, val badge: String) {
    FIRST_WIN("First Victory", "Win your first game", "🏆"),
    WIN_STREAK_3("Hat Trick", "Win 3 games", "⭐"),
    WIN_STREAK_10("Champion", "Win 10 games", "🌟"),
    BLOCKADE_PLAYED("Blockaded!", "Experience a blocked game", "🛡️"),
    VETERAN("Veteran", "Play 20 games", "🎖️");

    fun isUnlockedBy(result: GameResult): Boolean = when (this) {
        FIRST_WIN -> result.isWin && result.totalWins >= 1
        WIN_STREAK_3 -> result.isWin && result.totalWins >= 3
        WIN_STREAK_10 -> result.isWin && result.totalWins >= 10
        BLOCKADE_PLAYED -> result.isBlocked
        VETERAN -> result.totalGames >= 20
    }
}

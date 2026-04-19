package com.cancleeric.dominoblockade.domain.model

data class WeeklyLeaderboardEntry(
    val weekId: String = "",
    val userId: String = "",
    val displayName: String = "",
    val score: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val currentStreak: Int = 0,
    val hasBadge: Boolean = false
) {
    companion object {
        const val FIELD_WEEK_ID = "weekId"
        const val FIELD_USER_ID = "userId"
        const val FIELD_DISPLAY_NAME = "displayName"
        const val FIELD_SCORE = "score"
        const val FIELD_WINS = "wins"
        const val FIELD_LOSSES = "losses"
        const val FIELD_CURRENT_STREAK = "currentStreak"
        const val FIELD_HAS_BADGE = "hasBadge"
        const val POINTS_WIN = 10
        const val POINTS_LOSS = 1
        const val POINTS_STREAK_BONUS = 5
    }
}

package com.cancleeric.dominoblockade.data.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject
import javax.inject.Singleton

private const val EVENT_GAME_START = "game_start"
private const val EVENT_GAME_END = "game_end"
private const val EVENT_GAME_BLOCKED = "game_blocked"

private const val PARAM_PLAYER_COUNT = "player_count"
private const val PARAM_AI_DIFFICULTY = "ai_difficulty"
private const val PARAM_WINNER = "winner"
private const val PARAM_IS_BLOCKED = "is_blocked"
private const val PARAM_DURATION_SECONDS = "duration_seconds"
private const val PARAM_WIN_RATE = "win_rate"

@Singleton
class AnalyticsHelper @Inject constructor(
    private val analytics: FirebaseAnalytics
) {
    fun logGameStart(playerCount: Int, aiDifficulty: String? = null) {
        val bundle = Bundle().apply {
            putInt(PARAM_PLAYER_COUNT, playerCount)
            aiDifficulty?.let { putString(PARAM_AI_DIFFICULTY, it) }
        }
        analytics.logEvent(EVENT_GAME_START, bundle)
    }

    fun logGameEnd(winner: String, isBlocked: Boolean, durationSeconds: Long, winRate: Float) {
        val bundle = Bundle().apply {
            putString(PARAM_WINNER, winner)
            putBoolean(PARAM_IS_BLOCKED, isBlocked)
            putLong(PARAM_DURATION_SECONDS, durationSeconds)
            putFloat(PARAM_WIN_RATE, winRate)
        }
        analytics.logEvent(EVENT_GAME_END, bundle)
    }

    fun logGameBlocked() {
        analytics.logEvent(EVENT_GAME_BLOCKED, null)
    }
}

package com.cancleeric.dominoblockade.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject

/**
 * Firebase Analytics implementation of [AnalyticsService].
 *
 * Events tracked:
 *  - game_start
 *  - game_end
 *  - game_blocked
 *  - ai_difficulty_selected
 */
class FirebaseAnalyticsService @Inject constructor(
    private val analytics: FirebaseAnalytics
) : AnalyticsService {

    override fun logGameStart(aiDifficulty: String) {
        val bundle = Bundle().apply {
            putString(PARAM_AI_DIFFICULTY, aiDifficulty)
        }
        analytics.logEvent(EVENT_GAME_START, bundle)
    }

    override fun logGameEnd(durationSeconds: Long, playerWon: Boolean, finalScore: Int) {
        val bundle = Bundle().apply {
            putLong(PARAM_DURATION_SECONDS, durationSeconds)
            putBoolean(PARAM_PLAYER_WON, playerWon)
            putInt(PARAM_FINAL_SCORE, finalScore)
        }
        analytics.logEvent(EVENT_GAME_END, bundle)
    }

    override fun logGameBlocked() {
        analytics.logEvent(EVENT_GAME_BLOCKED, null)
    }

    override fun logAiDifficultySelected(difficulty: String) {
        val bundle = Bundle().apply {
            putString(PARAM_AI_DIFFICULTY, difficulty)
        }
        analytics.logEvent(EVENT_AI_DIFFICULTY_SELECTED, bundle)
    }

    override fun setUserId(userId: String) {
        analytics.setUserId(userId)
    }

    companion object {
        private const val EVENT_GAME_START = "game_start"
        private const val EVENT_GAME_END = "game_end"
        private const val EVENT_GAME_BLOCKED = "game_blocked"
        private const val EVENT_AI_DIFFICULTY_SELECTED = "ai_difficulty_selected"
        private const val PARAM_AI_DIFFICULTY = "ai_difficulty"
        private const val PARAM_DURATION_SECONDS = "duration_seconds"
        private const val PARAM_PLAYER_WON = "player_won"
        private const val PARAM_FINAL_SCORE = "final_score"
    }
}

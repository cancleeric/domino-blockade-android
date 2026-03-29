package com.cancleeric.dominoblockade.analytics

import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralises Crashlytics usage throughout the app.
 *
 * Custom keys recorded:
 *  - user_id
 *  - game_mode
 *  - ai_difficulty
 */
@Singleton
class CrashlyticsService @Inject constructor(
    private val crashlytics: FirebaseCrashlytics
) {
    fun setUserId(userId: String) {
        crashlytics.setUserId(userId)
    }

    fun setGameMode(mode: String) {
        crashlytics.setCustomKey(KEY_GAME_MODE, mode)
    }

    fun setAiDifficulty(difficulty: String) {
        crashlytics.setCustomKey(KEY_AI_DIFFICULTY, difficulty)
    }

    fun recordNonFatalError(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }

    companion object {
        private const val KEY_GAME_MODE = "game_mode"
        private const val KEY_AI_DIFFICULTY = "ai_difficulty"
    }
}

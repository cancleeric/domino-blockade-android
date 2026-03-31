package com.cancleeric.dominoblockade.data.crashlytics

import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject
import javax.inject.Singleton

private const val KEY_USER_ID = "user_id"
private const val KEY_GAME_MODE = "game_mode"
private const val KEY_AI_DIFFICULTY = "ai_difficulty"

@Singleton
class CrashlyticsHelper @Inject constructor() {

    private val crashlytics: FirebaseCrashlytics
        get() = FirebaseCrashlytics.getInstance()

    fun setUserId(userId: String) {
        crashlytics.setUserId(userId)
        crashlytics.setCustomKey(KEY_USER_ID, userId)
    }

    fun setGameMode(gameMode: String) {
        crashlytics.setCustomKey(KEY_GAME_MODE, gameMode)
    }

    fun setAiDifficulty(difficulty: String) {
        crashlytics.setCustomKey(KEY_AI_DIFFICULTY, difficulty)
    }

    fun logNonFatal(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }

    fun log(message: String) {
        crashlytics.log(message)
    }
}

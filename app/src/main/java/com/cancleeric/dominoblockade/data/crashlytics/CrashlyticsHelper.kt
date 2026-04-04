package com.cancleeric.dominoblockade.data.crashlytics

import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

private const val KEY_GAME_MODE = "game_mode"
private const val KEY_AI_DIFFICULTY = "ai_difficulty"
private const val HASH_ALGORITHM = "SHA-256"
private const val HASHED_ID_LENGTH = 16

@Singleton
class CrashlyticsHelper @Inject constructor() {

    private val crashlytics: FirebaseCrashlytics
        get() = FirebaseCrashlytics.getInstance()

    fun setUserId(userId: String) {
        crashlytics.setUserId(hashUid(userId))
    }

    fun setGameMode(gameMode: String) {
        crashlytics.setCustomKey(KEY_GAME_MODE, gameMode)
    }

    fun setAiDifficulty(difficulty: String) {
        crashlytics.setCustomKey(KEY_AI_DIFFICULTY, difficulty)
    }

    fun logException(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }

    fun log(message: String) {
        crashlytics.log(message)
    }

    private fun hashUid(uid: String): String {
        val digest = MessageDigest.getInstance(HASH_ALGORITHM)
        val bytes = digest.digest(uid.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }.take(HASHED_ID_LENGTH)
    }
}

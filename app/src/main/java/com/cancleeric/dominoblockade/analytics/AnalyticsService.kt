package com.cancleeric.dominoblockade.analytics

/**
 * Contract for game analytics tracking.
 */
interface AnalyticsService {
    fun logGameStart(aiDifficulty: String)
    fun logGameEnd(durationSeconds: Long, playerWon: Boolean, finalScore: Int)
    fun logGameBlocked()
    fun logAiDifficultySelected(difficulty: String)
    fun setUserId(userId: String)
}

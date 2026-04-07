package com.cancleeric.dominoblockade.domain.analytics

interface AnalyticsTracker {
    fun logTutorialStarted()
    fun logTutorialStepCompleted(step: Int)
    fun logTutorialCompleted()
    fun logGameStart(playerCount: Int, aiDifficulty: String? = null)
    fun logGameEnd(winner: String, isBlocked: Boolean, durationSeconds: Long, winRate: Float)
    fun logGameBlocked()
}

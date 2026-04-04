package com.cancleeric.dominoblockade.data.analytics

interface AnalyticsTracker {
    fun logGameStart(playerCount: Int, mode: String, difficulty: String)
    fun logGameEnd(result: String, durationSeconds: Long, mode: String, turnCount: Int)
    fun logBlockadeTriggered(turnNumber: Int, remainingPipTotal: Int)
    fun logAchievementUnlocked(achievementId: String, achievementName: String)
    fun logDominoPlaced(pipLeft: Int, pipRight: Int, boardEnd: String)
    fun logOnlineMatchFound(waitTimeSeconds: Long)
    fun logTutorialCompleted()
}

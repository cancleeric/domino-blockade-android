package com.cancleeric.dominoblockade.data.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject
import javax.inject.Singleton

private const val EVENT_GAME_START = "game_start"
private const val EVENT_GAME_END = "game_end"
private const val EVENT_BLOCKADE_TRIGGERED = "blockade_triggered"
private const val EVENT_ACHIEVEMENT_UNLOCKED = "achievement_unlocked"
private const val EVENT_DOMINO_PLACED = "domino_placed"
private const val EVENT_ONLINE_MATCH_FOUND = "online_match_found"
private const val EVENT_TUTORIAL_COMPLETED = "tutorial_completed"

private const val PARAM_PLAYER_COUNT = "player_count"
private const val PARAM_MODE = "mode"
private const val PARAM_DIFFICULTY = "difficulty"
private const val PARAM_RESULT = "result"
private const val PARAM_DURATION_SECONDS = "duration_seconds"
private const val PARAM_TURN_COUNT = "turn_count"
private const val PARAM_TURN_NUMBER = "turn_number"
private const val PARAM_REMAINING_PIP_TOTAL = "remaining_pip_total"
private const val PARAM_ACHIEVEMENT_ID = "achievement_id"
private const val PARAM_ACHIEVEMENT_NAME = "achievement_name"
private const val PARAM_PIP_LEFT = "pip_left"
private const val PARAM_PIP_RIGHT = "pip_right"
private const val PARAM_BOARD_END = "board_end"
private const val PARAM_WAIT_TIME_SECONDS = "wait_time_seconds"

@Singleton
class AnalyticsHelper @Inject constructor(
    private val analytics: FirebaseAnalytics
) : AnalyticsTracker {

    override fun logGameStart(playerCount: Int, mode: String, difficulty: String) {
        val bundle = Bundle().apply {
            putInt(PARAM_PLAYER_COUNT, playerCount)
            putString(PARAM_MODE, mode)
            putString(PARAM_DIFFICULTY, difficulty)
        }
        analytics.logEvent(EVENT_GAME_START, bundle)
    }

    override fun logGameEnd(result: String, durationSeconds: Long, mode: String, turnCount: Int) {
        val bundle = Bundle().apply {
            putString(PARAM_RESULT, result)
            putLong(PARAM_DURATION_SECONDS, durationSeconds)
            putString(PARAM_MODE, mode)
            putInt(PARAM_TURN_COUNT, turnCount)
        }
        analytics.logEvent(EVENT_GAME_END, bundle)
    }

    override fun logBlockadeTriggered(turnNumber: Int, remainingPipTotal: Int) {
        val bundle = Bundle().apply {
            putInt(PARAM_TURN_NUMBER, turnNumber)
            putInt(PARAM_REMAINING_PIP_TOTAL, remainingPipTotal)
        }
        analytics.logEvent(EVENT_BLOCKADE_TRIGGERED, bundle)
    }

    override fun logAchievementUnlocked(achievementId: String, achievementName: String) {
        val bundle = Bundle().apply {
            putString(PARAM_ACHIEVEMENT_ID, achievementId)
            putString(PARAM_ACHIEVEMENT_NAME, achievementName)
        }
        analytics.logEvent(EVENT_ACHIEVEMENT_UNLOCKED, bundle)
    }

    override fun logDominoPlaced(pipLeft: Int, pipRight: Int, boardEnd: String) {
        val bundle = Bundle().apply {
            putInt(PARAM_PIP_LEFT, pipLeft)
            putInt(PARAM_PIP_RIGHT, pipRight)
            putString(PARAM_BOARD_END, boardEnd)
        }
        analytics.logEvent(EVENT_DOMINO_PLACED, bundle)
    }

    override fun logOnlineMatchFound(waitTimeSeconds: Long) {
        val bundle = Bundle().apply {
            putLong(PARAM_WAIT_TIME_SECONDS, waitTimeSeconds)
        }
        analytics.logEvent(EVENT_ONLINE_MATCH_FOUND, bundle)
    }

    override fun logTutorialCompleted() {
        analytics.logEvent(EVENT_TUTORIAL_COMPLETED, null)
    }
}

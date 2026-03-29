package com.cancleeric.dominoblockade.domain.usecase

import com.cancleeric.dominoblockade.domain.model.Achievement
import com.cancleeric.dominoblockade.domain.model.AchievementType
import com.cancleeric.dominoblockade.domain.model.GameResult
import com.cancleeric.dominoblockade.domain.repository.AchievementRepository
import javax.inject.Inject

/**
 * Evaluates a completed [GameResult] and unlocks any achievements whose conditions
 * are newly satisfied.
 *
 * Returns the list of achievements that were **just** unlocked by this invocation.
 * Already-unlocked achievements are never returned again.
 */
class CheckAchievementsUseCase @Inject constructor(
    private val repository: AchievementRepository
) {
    suspend operator fun invoke(result: GameResult): List<Achievement> {
        val newlyUnlocked = mutableListOf<Achievement>()

        for (type in AchievementType.entries) {
            if (repository.isUnlocked(type)) continue
            if (isSatisfied(type, result)) {
                repository.unlockAchievement(type)
                newlyUnlocked += Achievement(type = type, isUnlocked = true)
            }
        }

        return newlyUnlocked
    }

    private fun isSatisfied(type: AchievementType, r: GameResult): Boolean = when (type) {
        // Win-count milestones (only applicable when player won)
        AchievementType.FIRST_WIN -> r.isWin && r.totalWins >= 1
        AchievementType.TEN_WINS -> r.isWin && r.totalWins >= 10
        AchievementType.FIFTY_WINS -> r.isWin && r.totalWins >= 50
        AchievementType.HUNDRED_WINS -> r.isWin && r.totalWins >= 100

        // Consecutive-win streaks
        AchievementType.WIN_STREAK_3 -> r.isWin && r.consecutiveWins >= 3
        AchievementType.WIN_STREAK_5 -> r.isWin && r.consecutiveWins >= 5
        AchievementType.WIN_STREAK_10 -> r.isWin && r.consecutiveWins >= 10

        // Special condition wins
        AchievementType.PERFECT_BLOCK -> r.isWin && r.isBlocked
        AchievementType.QUICK_WIN_60 -> r.isWin && r.durationSeconds < 60
        AchievementType.QUICK_WIN_30 -> r.isWin && r.durationSeconds < 30
        AchievementType.ZERO_HAND_WIN -> r.isWin && r.playerRemainingTiles == 0
        AchievementType.LOW_SCORE_WIN -> r.isWin && r.playerRemainingScore <= 5

        // Experience milestones (win not required)
        AchievementType.FIRST_BLOCKADE -> r.isBlocked
    }
}

package com.cancleeric.dominoblockade.domain.usecase

import com.cancleeric.dominoblockade.domain.model.AchievementType
import com.cancleeric.dominoblockade.domain.model.GameResult
import com.cancleeric.dominoblockade.domain.repository.AchievementRepository
import javax.inject.Inject

class CheckAchievementsUseCase @Inject constructor(
    private val achievementRepository: AchievementRepository
) {
    suspend operator fun invoke(result: GameResult): List<AchievementType> {
        val alreadyUnlocked = achievementRepository.getAllUnlocked().map { it.type }.toSet()
        val newlyUnlocked = AchievementType.entries.filter { type ->
            type !in alreadyUnlocked && type.isUnlockedBy(result)
        }
        newlyUnlocked.forEach { achievementRepository.unlock(it) }
        return newlyUnlocked
    }
}

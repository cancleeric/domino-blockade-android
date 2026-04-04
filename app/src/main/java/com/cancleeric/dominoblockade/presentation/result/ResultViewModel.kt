package com.cancleeric.dominoblockade.presentation.result

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cancleeric.dominoblockade.data.analytics.AnalyticsTracker
import com.cancleeric.dominoblockade.data.local.entity.PlayerStatsEntity
import com.cancleeric.dominoblockade.domain.model.AchievementType
import com.cancleeric.dominoblockade.domain.model.GameResult
import com.cancleeric.dominoblockade.domain.repository.PlayerStatsRepository
import com.cancleeric.dominoblockade.domain.usecase.CheckAchievementsUseCase
import com.cancleeric.dominoblockade.presentation.notification.AchievementNotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val GLOBAL_PLAYER_KEY = "global"

@HiltViewModel
class ResultViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val playerStatsRepository: PlayerStatsRepository,
    private val checkAchievementsUseCase: CheckAchievementsUseCase,
    private val notificationHelper: AchievementNotificationHelper,
    private val analyticsTracker: AnalyticsTracker
) : ViewModel() {

    private val _newAchievements = MutableStateFlow<List<AchievementType>>(emptyList())
    val newAchievements: StateFlow<List<AchievementType>> = _newAchievements.asStateFlow()

    init {
        val winnerName = savedStateHandle.get<String>("winnerName").orEmpty()
            .let { if (it == "_") "" else it }
        val isBlocked = savedStateHandle.get<Boolean>("isBlocked") ?: false
        viewModelScope.launch {
            val result = buildGameResult(winnerName, isBlocked)
            val unlocked = checkAchievementsUseCase(result)
            unlocked.forEach { achievement ->
                notificationHelper.showAchievementUnlocked(achievement)
                analyticsTracker.logAchievementUnlocked(achievement.name, achievement.title)
            }
            _newAchievements.value = unlocked
        }
    }

    private suspend fun buildGameResult(winnerName: String, isBlocked: Boolean): GameResult {
        val isWin = winnerName.isNotEmpty()
        val existing = playerStatsRepository.getByName(GLOBAL_PLAYER_KEY)
            ?: PlayerStatsEntity(playerName = GLOBAL_PLAYER_KEY)
        val updated = existing.copy(
            totalGames = existing.totalGames + 1,
            wins = existing.wins + if (isWin) 1 else 0
        )
        playerStatsRepository.upsert(updated)
        return GameResult(
            isWin = isWin,
            isBlocked = isBlocked,
            totalWins = updated.wins,
            totalGames = updated.totalGames
        )
    }
}

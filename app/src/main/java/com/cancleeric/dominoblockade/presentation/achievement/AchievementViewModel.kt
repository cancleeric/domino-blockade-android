package com.cancleeric.dominoblockade.presentation.achievement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cancleeric.dominoblockade.domain.model.Achievement
import com.cancleeric.dominoblockade.domain.model.GameResult
import com.cancleeric.dominoblockade.domain.repository.AchievementRepository
import com.cancleeric.dominoblockade.domain.usecase.CheckAchievementsUseCase
import com.cancleeric.dominoblockade.domain.usecase.GetAchievementsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AchievementViewModel @Inject constructor(
    private val getAchievementsUseCase: GetAchievementsUseCase,
    private val checkAchievementsUseCase: CheckAchievementsUseCase,
    private val achievementRepository: AchievementRepository
) : ViewModel() {

    /** Full list of all achievements (locked + unlocked). */
    val achievements: StateFlow<List<Achievement>> = getAchievementsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    /** Achievements that were newly unlocked after the most recent game. Consumed once by the UI. */
    private val _newlyUnlocked = MutableStateFlow<List<Achievement>>(emptyList())
    val newlyUnlocked: StateFlow<List<Achievement>> = _newlyUnlocked.asStateFlow()

    init {
        viewModelScope.launch {
            achievementRepository.initializeAchievements()
        }
    }

    /**
     * Evaluates [result] against all achievement conditions and updates [newlyUnlocked]
     * with any achievements that were just unlocked.
     */
    fun onGameFinished(result: GameResult) {
        viewModelScope.launch {
            val unlocked = checkAchievementsUseCase(result)
            if (unlocked.isNotEmpty()) {
                _newlyUnlocked.value = unlocked
            }
        }
    }

    /** Call after the UI has displayed the unlock notification to clear the queue. */
    fun onUnlockNotificationsConsumed() {
        _newlyUnlocked.value = emptyList()
    }
}

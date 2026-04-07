package com.cancleeric.dominoblockade.presentation.tutorial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cancleeric.dominoblockade.domain.analytics.AnalyticsTracker
import com.cancleeric.dominoblockade.domain.repository.TutorialRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TOTAL_STEPS = 5

data class TutorialUiState(
    val isVisible: Boolean = false,
    val currentStep: Int = 0,
    val totalSteps: Int = TOTAL_STEPS
)

@HiltViewModel
class TutorialViewModel @Inject constructor(
    private val tutorialRepository: TutorialRepository,
    private val analyticsTracker: AnalyticsTracker
) : ViewModel() {

    private val _uiState = MutableStateFlow(TutorialUiState())
    val uiState: StateFlow<TutorialUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            tutorialRepository.isTutorialCompleted.collect { completed ->
                val wasVisible = _uiState.value.isVisible
                val nowVisible = !completed
                _uiState.value = _uiState.value.copy(isVisible = nowVisible)
                if (nowVisible && !wasVisible) {
                    analyticsTracker.logTutorialStarted()
                }
            }
        }
    }

    fun nextStep() {
        val state = _uiState.value
        if (state.currentStep < state.totalSteps - 1) {
            val nextStep = state.currentStep + 1
            _uiState.value = state.copy(currentStep = nextStep)
            analyticsTracker.logTutorialStepCompleted(nextStep)
        } else {
            completeTutorial()
        }
    }

    fun completeTutorial() {
        _uiState.value = _uiState.value.copy(isVisible = false)
        analyticsTracker.logTutorialCompleted()
        viewModelScope.launch {
            tutorialRepository.markTutorialCompleted()
        }
    }
}

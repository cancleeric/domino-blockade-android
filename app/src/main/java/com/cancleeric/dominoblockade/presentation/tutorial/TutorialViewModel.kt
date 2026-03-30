package com.cancleeric.dominoblockade.presentation.tutorial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cancleeric.dominoblockade.R
import com.cancleeric.dominoblockade.domain.model.HighlightTarget
import com.cancleeric.dominoblockade.domain.model.TutorialStep
import com.cancleeric.dominoblockade.domain.repository.TutorialRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TutorialViewModel @Inject constructor(
    private val tutorialRepository: TutorialRepository
) : ViewModel() {

    private val tutorialSteps = listOf(
        TutorialStep(
            stepIndex = 0,
            titleRes = R.string.tutorial_step1_title,
            messageRes = R.string.tutorial_step1_message,
            highlightTarget = HighlightTarget.NONE
        ),
        TutorialStep(
            stepIndex = 1,
            titleRes = R.string.tutorial_step2_title,
            messageRes = R.string.tutorial_step2_message,
            highlightTarget = HighlightTarget.BOARD
        ),
        TutorialStep(
            stepIndex = 2,
            titleRes = R.string.tutorial_step3_title,
            messageRes = R.string.tutorial_step3_message,
            highlightTarget = HighlightTarget.PLAYER_HAND
        ),
        TutorialStep(
            stepIndex = 3,
            titleRes = R.string.tutorial_step4_title,
            messageRes = R.string.tutorial_step4_message,
            highlightTarget = HighlightTarget.BOARD
        ),
        TutorialStep(
            stepIndex = 4,
            titleRes = R.string.tutorial_step5_title,
            messageRes = R.string.tutorial_step5_message,
            highlightTarget = HighlightTarget.BONEYARD
        ),
        TutorialStep(
            stepIndex = 5,
            titleRes = R.string.tutorial_step6_title,
            messageRes = R.string.tutorial_step6_message,
            highlightTarget = HighlightTarget.SCORE
        )
    )

    private val _currentStepIndex = MutableStateFlow(0)
    val currentStepIndex: StateFlow<Int> = _currentStepIndex.asStateFlow()

    val currentStep: TutorialStep get() = tutorialSteps[_currentStepIndex.value]

    val totalSteps: Int get() = tutorialSteps.size

    fun nextStep() {
        if (_currentStepIndex.value < tutorialSteps.size - 1) {
            _currentStepIndex.value++
        }
    }

    fun previousStep() {
        if (_currentStepIndex.value > 0) {
            _currentStepIndex.value--
        }
    }

    fun completeTutorial() {
        viewModelScope.launch {
            tutorialRepository.markTutorialCompleted()
        }
    }

    fun skipTutorial() {
        completeTutorial()
    }

    fun isLastStep(): Boolean = _currentStepIndex.value == tutorialSteps.size - 1
}

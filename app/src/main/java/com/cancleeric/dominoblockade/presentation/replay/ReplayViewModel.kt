package com.cancleeric.dominoblockade.presentation.replay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cancleeric.dominoblockade.domain.model.ReplayStep
import com.cancleeric.dominoblockade.domain.repository.GameReplayRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReplayViewModel @Inject constructor(
    private val repository: GameReplayRepository
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = true,
        val steps: List<ReplayStep> = emptyList(),
        val currentIndex: Int = 0,
        val winnerName: String = "",
        val isBlocked: Boolean = false,
        val playerCount: Int = 2
    ) {
        val currentStep: ReplayStep? get() = steps.getOrNull(currentIndex)
        val canGoBack: Boolean get() = currentIndex > 0
        val canGoForward: Boolean get() = currentIndex < steps.size - 1
        val totalSteps: Int get() = steps.size
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadReplay()
    }

    fun nextStep() {
        val state = _uiState.value
        if (state.canGoForward) {
            _uiState.value = state.copy(currentIndex = state.currentIndex + 1)
        }
    }

    fun previousStep() {
        val state = _uiState.value
        if (state.canGoBack) {
            _uiState.value = state.copy(currentIndex = state.currentIndex - 1)
        }
    }

    fun goToStep(index: Int) {
        val state = _uiState.value
        if (state.steps.isNotEmpty()) {
            _uiState.value = state.copy(currentIndex = index.coerceIn(0, state.steps.size - 1))
        }
    }

    private fun loadReplay() {
        viewModelScope.launch {
            val result = repository.getLatestReplayWithMoves()
            if (result == null) {
                _uiState.value = UiState(isLoading = false)
                return@launch
            }
            val (replay, moves) = result
            val steps = moves.map { move ->
                ReplayStep(
                    moveIndex = move.moveIndex,
                    playerName = move.playerName,
                    moveType = move.moveType,
                    dominoLeft = move.dominoLeft,
                    dominoRight = move.dominoRight,
                    board = ReplayStep.deserializeBoard(move.boardState),
                    boneyardSize = move.boneyardSize
                )
            }
            _uiState.value = UiState(
                isLoading = false,
                steps = steps,
                currentIndex = 0,
                winnerName = replay.winnerName,
                isBlocked = replay.isBlocked,
                playerCount = replay.playerCount
            )
        }
    }
}

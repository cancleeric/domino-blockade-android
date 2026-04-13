package com.cancleeric.dominoblockade.presentation.tournament

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cancleeric.dominoblockade.domain.model.Tournament
import com.cancleeric.dominoblockade.domain.model.GameMode
import com.cancleeric.dominoblockade.domain.repository.TournamentRepository
import com.cancleeric.dominoblockade.domain.usecase.AdaptiveAiManager
import com.cancleeric.dominoblockade.domain.usecase.AdvanceTournamentUseCase
import com.cancleeric.dominoblockade.domain.usecase.CreateTournamentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class TournamentUiState {
    object Setup : TournamentUiState()
    object Loading : TournamentUiState()
    data class BracketView(val tournament: Tournament) : TournamentUiState()
    data class Error(val message: String) : TournamentUiState()
}

@HiltViewModel
class TournamentViewModel @Inject constructor(
    private val createTournamentUseCase: CreateTournamentUseCase,
    private val advanceTournamentUseCase: AdvanceTournamentUseCase,
    private val tournamentRepository: TournamentRepository,
    private val adaptiveAiManager: AdaptiveAiManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<TournamentUiState>(TournamentUiState.Setup)
    val uiState: StateFlow<TournamentUiState> = _uiState.asStateFlow()

    private val _tournament = MutableStateFlow<Tournament?>(null)
    val tournament: StateFlow<Tournament?> = _tournament.asStateFlow()

    fun loadActiveTournament() {
        viewModelScope.launch {
            tournamentRepository.getActiveTournament().collect { tournament ->
                _tournament.value = tournament
                if (tournament != null) {
                    _uiState.value = TournamentUiState.BracketView(tournament)
                }
            }
        }
    }

    fun createTournament(playerCount: Int, playerNames: List<String>) {
        _uiState.value = TournamentUiState.Loading
        viewModelScope.launch {
            runCatching { createTournamentUseCase(playerCount, playerNames) }
                .onSuccess { tournament ->
                    _tournament.value = tournament
                    _uiState.value = TournamentUiState.BracketView(tournament)
                }
                .onFailure { error ->
                    _uiState.value = TournamentUiState.Error(error.message ?: "Unknown error")
                }
        }
    }

    fun recordMatchWinner(tournamentId: String, roundIndex: Int, matchIndex: Int, winnerId: String) {
        viewModelScope.launch {
            val selectedWinnerName = _tournament.value
                ?.rounds
                ?.getOrNull(roundIndex)
                ?.getOrNull(matchIndex)
                ?.let { match ->
                    when (winnerId) {
                        match.player1?.playerId -> match.player1?.playerName
                        match.player2?.playerId -> match.player2?.playerName
                        else -> null
                    }
                }.orEmpty()
            runCatching { advanceTournamentUseCase(tournamentId, roundIndex, matchIndex, winnerId) }
                .onSuccess {
                    adaptiveAiManager.recordGameResult(
                        gameMode = GameMode.TOURNAMENT,
                        playerWon = !selectedWinnerName.startsWith(prefix = "AI", ignoreCase = true)
                    )
                }
                .onFailure { error ->
                    _uiState.value = TournamentUiState.Error(error.message ?: "Unknown error")
                }
        }
    }

    fun deleteTournament(tournamentId: String) {
        viewModelScope.launch {
            tournamentRepository.deleteTournament(tournamentId)
            _uiState.value = TournamentUiState.Setup
            _tournament.value = null
        }
    }
}

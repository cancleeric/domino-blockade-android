package com.cancleeric.dominoblockade.presentation.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cancleeric.dominoblockade.data.remote.auth.AuthService
import com.cancleeric.dominoblockade.data.remote.auth.User
import com.cancleeric.dominoblockade.data.remote.firestore.LeaderboardEntry
import com.cancleeric.dominoblockade.data.remote.firestore.LeaderboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TOP_PLAYERS_LIMIT = 50
private const val ERROR_SIGN_IN = "Failed to sign in. Please try again."
private const val ERROR_SIGN_OUT = "Failed to sign out. Please try again."

data class LeaderboardUiState(
    val entries: List<LeaderboardEntry> = emptyList(),
    val currentUser: User? = null,
    val playerRank: Int? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val leaderboardRepository: LeaderboardRepository,
    private val authService: AuthService
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaderboardUiState())
    val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()

    init {
        observeCurrentUser()
        loadLeaderboard()
    }

    private fun observeCurrentUser() {
        authService.getCurrentUser()
            .onEach { user ->
                _uiState.value = _uiState.value.copy(currentUser = user)
                user?.uid?.let { loadPlayerRank(it) }
            }
            .launchIn(viewModelScope)
    }

    private fun loadLeaderboard() {
        leaderboardRepository.getTopPlayers(TOP_PLAYERS_LIMIT)
            .onEach { entries ->
                _uiState.value = _uiState.value.copy(
                    entries = entries,
                    isLoading = false
                )
            }
            .launchIn(viewModelScope)
    }

    private fun loadPlayerRank(userId: String) {
        leaderboardRepository.getPlayerRank(userId)
            .onEach { rank ->
                _uiState.value = _uiState.value.copy(playerRank = rank)
            }
            .launchIn(viewModelScope)
    }

    fun signInAnonymously() {
        viewModelScope.launch {
            runCatching { authService.signInAnonymously() }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        error = e.message ?: ERROR_SIGN_IN
                    )
                }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            runCatching { authService.signOut() }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        error = e.message ?: ERROR_SIGN_OUT
                    )
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

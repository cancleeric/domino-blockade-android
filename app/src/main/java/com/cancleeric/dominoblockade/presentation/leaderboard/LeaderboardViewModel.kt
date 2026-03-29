package com.cancleeric.dominoblockade.presentation.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cancleeric.dominoblockade.data.model.LeaderboardEntry
import com.cancleeric.dominoblockade.data.model.User
import com.cancleeric.dominoblockade.data.remote.auth.AuthService
import com.cancleeric.dominoblockade.data.remote.firestore.LeaderboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LeaderboardUiState(
    val entries: List<LeaderboardEntry> = emptyList(),
    val currentUser: User? = null,
    val currentUserRank: Int? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val leaderboardRepository: LeaderboardRepository,
    private val authService: AuthService
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaderboardUiState())
    val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()

    init {
        observeLeaderboard()
    }

    private fun observeLeaderboard() {
        authService.getCurrentUser()
            .onEach { user ->
                _uiState.value = _uiState.value.copy(currentUser = user)
            }
            .launchIn(viewModelScope)

        combine(
            leaderboardRepository.getTopPlayers(limit = 20),
            authService.getCurrentUser()
        ) { entries, user ->
            val rank = user?.let {
                val index = entries.indexOfFirst { e -> e.userId == user.uid }
                if (index >= 0) index + 1 else null
            }
            _uiState.value = _uiState.value.copy(
                entries = entries,
                currentUser = user,
                currentUserRank = rank,
                isLoading = false,
                errorMessage = null
            )
        }.launchIn(viewModelScope)
    }

    fun signInAnonymously() {
        viewModelScope.launch {
            runCatching { authService.signInAnonymously() }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message ?: "Failed to sign in"
                    )
                }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            runCatching { authService.signOut() }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message ?: "Failed to sign out"
                    )
                }
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

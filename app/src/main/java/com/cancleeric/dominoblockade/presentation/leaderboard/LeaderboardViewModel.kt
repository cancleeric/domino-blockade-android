package com.cancleeric.dominoblockade.presentation.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cancleeric.dominoblockade.data.remote.auth.AuthService
import com.cancleeric.dominoblockade.data.remote.auth.User
import com.cancleeric.dominoblockade.domain.model.LeaderboardEntry
import com.cancleeric.dominoblockade.domain.repository.LeaderboardSegment
import com.cancleeric.dominoblockade.domain.repository.LeaderboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
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
    val selectedSegment: LeaderboardSegment = LeaderboardSegment.CURRENT_SEASON,
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
        observeLeaderboard()
    }

    private fun observeCurrentUser() {
        authService.getCurrentUser()
            .onEach { user ->
                _uiState.value = _uiState.value.copy(currentUser = user)
                user?.let { currentUser ->
                    viewModelScope.launch {
                        val displayName = currentUser.displayName
                            ?.takeIf { it.isNotBlank() }
                            ?: "Player-${currentUser.uid.take(6)}"
                        leaderboardRepository.ensurePlayerEntry(currentUser.uid, displayName)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeLeaderboard() {
        combine(
            _uiState.mapDistinct { it.selectedSegment },
            _uiState.mapDistinct { it.currentUser?.uid }
        ) { segment, userId -> segment to userId }
            .flatMapLatest { (segment, userId) ->
                leaderboardRepository.getTopPlayers(TOP_PLAYERS_LIMIT, segment, userId)
            }
            .onEach { entries ->
                _uiState.value = _uiState.value.copy(
                    entries = entries,
                    isLoading = false
                )
            }
            .launchIn(viewModelScope)
        combine(
            _uiState.mapDistinct { it.selectedSegment },
            _uiState.mapDistinct { it.currentUser?.uid }
        ) { segment, userId -> segment to userId }
            .flatMapLatest { (segment, userId) ->
                if (userId == null) {
                    flowOf(null)
                } else {
                    leaderboardRepository.getPlayerRank(userId, segment, userId)
                }
            }
            .onEach { rank ->
                _uiState.value = _uiState.value.copy(playerRank = rank, isLoading = false)
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

    fun selectSegment(segment: LeaderboardSegment) {
        if (_uiState.value.selectedSegment == segment) return
        _uiState.value = _uiState.value.copy(selectedSegment = segment, isLoading = true)
    }

    private fun <T> MutableStateFlow<LeaderboardUiState>.mapDistinct(
        selector: (LeaderboardUiState) -> T
    ) = this.asStateFlow().map(selector).distinctUntilChanged()
}

package com.cancleeric.dominoblockade.presentation.weeklyleaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cancleeric.dominoblockade.data.remote.auth.AuthService
import com.cancleeric.dominoblockade.data.remote.auth.User
import com.cancleeric.dominoblockade.domain.model.WeeklyLeaderboardEntry
import com.cancleeric.dominoblockade.domain.repository.WeeklyLeaderboardRepository
import com.cancleeric.dominoblockade.presentation.notification.RankChangeNotifier
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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
private const val COUNTDOWN_INTERVAL_MS = 1_000L

data class WeeklyLeaderboardUiState(
    val weekId: String = "",
    val entries: List<WeeklyLeaderboardEntry> = emptyList(),
    val currentUser: User? = null,
    val playerEntry: WeeklyLeaderboardEntry? = null,
    val playerRank: Int? = null,
    val millisUntilReset: Long = 0L,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class WeeklyLeaderboardViewModel @Inject constructor(
    private val weeklyLeaderboardRepository: WeeklyLeaderboardRepository,
    private val authService: AuthService,
    private val notificationHelper: RankChangeNotifier
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeeklyLeaderboardUiState())
    val uiState: StateFlow<WeeklyLeaderboardUiState> = _uiState.asStateFlow()

    private var previousRank: Int? = null

    init {
        val weekId = weeklyLeaderboardRepository.getCurrentWeekId()
        _uiState.value = _uiState.value.copy(weekId = weekId)
        observeCurrentUser()
        observeLeaderboard()
        startCountdown()
    }

    private fun observeCurrentUser() {
        authService.getCurrentUser()
            .onEach { user ->
                _uiState.value = _uiState.value.copy(currentUser = user)
                user?.let { currentUser ->
                    val displayName = currentUser.displayName
                        ?.takeIf { it.isNotBlank() }
                        ?: "Player-${currentUser.uid.take(6)}"
                    viewModelScope.launch {
                        runCatching {
                            weeklyLeaderboardRepository.ensurePlayerEntry(
                                _uiState.value.weekId,
                                currentUser.uid,
                                displayName
                            )
                        }
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeLeaderboard() {
        _uiState.asStateFlow()
            .map { it.weekId }
            .distinctUntilChanged()
            .flatMapLatest { weekId ->
                weeklyLeaderboardRepository.getTopPlayers(weekId, TOP_PLAYERS_LIMIT)
            }
            .onEach { entries ->
                _uiState.value = _uiState.value.copy(entries = entries, isLoading = false)
            }
            .launchIn(viewModelScope)

        combine(
            _uiState.asStateFlow().map { it.weekId }.distinctUntilChanged(),
            _uiState.asStateFlow().map { it.currentUser?.uid }.distinctUntilChanged()
        ) { weekId, userId -> weekId to userId }
            .flatMapLatest { (weekId, userId) ->
                if (userId == null) flowOf(null) else weeklyLeaderboardRepository.getPlayerRank(weekId, userId)
            }
            .onEach { rank ->
                val previous = previousRank
                _uiState.value = _uiState.value.copy(playerRank = rank, isLoading = false)
                if (rank != null) {
                    notificationHelper.notifyRankChange(rank, previous)
                }
                previousRank = rank
            }
            .launchIn(viewModelScope)

        combine(
            _uiState.asStateFlow().map { it.weekId }.distinctUntilChanged(),
            _uiState.asStateFlow().map { it.currentUser?.uid }.distinctUntilChanged()
        ) { weekId, userId -> weekId to userId }
            .flatMapLatest { (weekId, userId) ->
                if (userId == null) flowOf(null) else weeklyLeaderboardRepository.getPlayerEntry(weekId, userId)
            }
            .onEach { entry ->
                _uiState.value = _uiState.value.copy(playerEntry = entry)
            }
            .launchIn(viewModelScope)
    }

    private fun startCountdown() {
        viewModelScope.launch {
            while (true) {
                _uiState.value = _uiState.value.copy(
                    millisUntilReset = weeklyLeaderboardRepository.getMillisUntilWeekReset()
                )
                delay(COUNTDOWN_INTERVAL_MS)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

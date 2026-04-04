package com.cancleeric.dominoblockade.presentation.lobby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cancleeric.dominoblockade.data.analytics.AnalyticsTracker
import com.cancleeric.dominoblockade.domain.model.OnlineRoom
import com.cancleeric.dominoblockade.domain.model.OnlineRoomStatus
import com.cancleeric.dominoblockade.domain.repository.OnlineGameRepository
import com.cancleeric.dominoblockade.domain.usecase.StartGameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

private const val GUEST_PLAYER_INDEX = 1
private const val HOST_PLAYER_INDEX = 0
private const val MS_PER_SECOND = 1000L

@HiltViewModel
class LobbyViewModel @Inject constructor(
    private val onlineGameRepository: OnlineGameRepository,
    private val startGameUseCase: StartGameUseCase,
    private val analyticsTracker: AnalyticsTracker
) : ViewModel() {

    private val _uiState = MutableStateFlow(LobbyUiState())
    val uiState: StateFlow<LobbyUiState> = _uiState.asStateFlow()

    private val localId = UUID.randomUUID().toString()
    private var gameInitialized = false
    private var matchmakingStartTime = 0L

    fun setPlayerName(name: String) {
        _uiState.value = _uiState.value.copy(playerName = name, error = null)
    }

    fun setRoomCode(code: String) {
        _uiState.value = _uiState.value.copy(roomCode = code.uppercase(), error = null)
    }

    fun createRoom() {
        val name = _uiState.value.playerName.trim()
        if (name.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Enter your name first")
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        matchmakingStartTime = System.currentTimeMillis()
        viewModelScope.launch {
            val result = runCatching { onlineGameRepository.createRoom(localId, name) }
            val roomId = result.getOrNull()
            if (roomId == null) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to create room")
                return@launch
            }
            _uiState.value = _uiState.value.copy(isLoading = false, createdRoomId = roomId)
            observeRoomForUpdates(roomId, HOST_PLAYER_INDEX)
        }
    }

    fun joinRoom() {
        val name = _uiState.value.playerName.trim()
        val code = _uiState.value.roomCode.trim()
        if (name.isEmpty() || code.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Enter your name and room code")
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        matchmakingStartTime = System.currentTimeMillis()
        viewModelScope.launch {
            val result = runCatching { onlineGameRepository.joinRoom(code, localId, name) }
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Room not found")
                return@launch
            }
            _uiState.value = _uiState.value.copy(isLoading = false)
            observeRoomForUpdates(code, GUEST_PLAYER_INDEX)
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetNavigation() {
        _uiState.value = _uiState.value.copy(navigateToGame = null)
    }

    private fun observeRoomForUpdates(roomId: String, localPlayerIndex: Int) {
        viewModelScope.launch {
            onlineGameRepository.observeRoom(roomId).collect { room ->
                _uiState.value = _uiState.value.copy(roomStatus = room.status)
                handleRoomUpdate(room, roomId, localPlayerIndex)
            }
        }
    }

    private fun handleRoomUpdate(room: OnlineRoom, roomId: String, localPlayerIndex: Int) {
        val isPlaying = room.status == OnlineRoomStatus.PLAYING
        val canNavigate = isPlaying && room.gameState != null && _uiState.value.navigateToGame == null
        val shouldInit = isPlaying && room.gameState == null
            && localPlayerIndex == HOST_PLAYER_INDEX && !gameInitialized
        when {
            shouldInit -> {
                gameInitialized = true
                initializeGameAsHost(room, roomId)
            }
            canNavigate -> {
                val waitSeconds = (System.currentTimeMillis() - matchmakingStartTime) / MS_PER_SECOND
                analyticsTracker.logOnlineMatchFound(waitSeconds)
                _uiState.value = _uiState.value.copy(
                    navigateToGame = NavigateToOnlineGame(roomId, localPlayerIndex)
                )
            }
        }
    }

    private fun initializeGameAsHost(room: OnlineRoom, roomId: String) {
        val guestName = room.guestName ?: return
        viewModelScope.launch {
            val gameState = startGameUseCase(listOf(room.hostName, guestName))
            runCatching { onlineGameRepository.updateGameState(roomId, gameState) }
        }
    }
}


package com.cancleeric.dominoblockade.presentation.lobby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cancleeric.dominoblockade.domain.model.OnlineRoom
import com.cancleeric.dominoblockade.domain.model.OnlineRoomStatus
import com.cancleeric.dominoblockade.domain.repository.OnlineGameRepository
import com.cancleeric.dominoblockade.domain.usecase.StartGameUseCase
import kotlinx.coroutines.Job
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

private const val GUEST_PLAYER_INDEX = 1
private const val HOST_PLAYER_INDEX = 0

@HiltViewModel
class LobbyViewModel @Inject constructor(
    private val onlineGameRepository: OnlineGameRepository,
    private val startGameUseCase: StartGameUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LobbyUiState())
    val uiState: StateFlow<LobbyUiState> = _uiState.asStateFlow()

    private val localId = UUID.randomUUID().toString()
    private var gameInitialized = false
    private var rankedAssignmentJob: Job? = null

    fun setPlayerName(name: String) {
        _uiState.value = _uiState.value.copy(playerName = name, error = null)
    }

    fun setRoomCode(code: String) {
        _uiState.value = _uiState.value.copy(roomCode = code.uppercase(), error = null)
    }

    fun setMatchMode(mode: MatchMode) {
        val isQueueing = _uiState.value.isQueueingRanked
        if (isQueueing && mode == MatchMode.CASUAL) {
            cancelRankedQueue()
        }
        _uiState.value = _uiState.value.copy(mode = mode, error = null)
    }

    fun createRoom() {
        if (_uiState.value.mode == MatchMode.RANKED) {
            joinRankedQueue()
            return
        }
        val name = _uiState.value.playerName.trim()
        if (name.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Enter your name first")
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
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
        if (_uiState.value.mode == MatchMode.RANKED) {
            joinRankedQueue()
            return
        }
        val name = _uiState.value.playerName.trim()
        val code = _uiState.value.roomCode.trim()
        if (name.isEmpty() || code.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Enter your name and room code")
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
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

    fun joinAsSpectator() {
        val name = _uiState.value.playerName.trim()
        val code = _uiState.value.roomCode.trim()
        if (name.isEmpty() || code.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Enter your name and room code")
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val result = runCatching { onlineGameRepository.joinAsSpectator(code, localId, name) }
            val success = result.getOrNull()
            if (result.isFailure || success == false) {
                val msg = if (success == false) "Spectators are not allowed in this room"
                else "Room not found"
                _uiState.value = _uiState.value.copy(isLoading = false, error = msg)
                return@launch
            }
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                navigateToSpectator = NavigateToSpectator(code, localId)
            )
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetNavigation() {
        _uiState.value = _uiState.value.copy(navigateToGame = null)
    }

    fun resetSpectatorNavigation() {
        _uiState.value = _uiState.value.copy(navigateToSpectator = null)
    }

    fun toggleSpectatorPermission() {
        val current = _uiState.value.allowSpectators
        val roomId = _uiState.value.createdRoomId ?: return
        _uiState.value = _uiState.value.copy(allowSpectators = !current)
        viewModelScope.launch {
            runCatching { onlineGameRepository.setSpectatorPermission(roomId, !current) }
                .onFailure { _uiState.value = _uiState.value.copy(allowSpectators = current) }
        }
    }

    private fun observeRoomForUpdates(roomId: String, localPlayerIndex: Int) {
        viewModelScope.launch {
            onlineGameRepository.observeRoom(roomId).collect { room ->
                _uiState.value = _uiState.value.copy(
                    roomStatus = room.status,
                    spectators = room.spectators,
                    allowSpectators = room.allowSpectators
                )
                handleRoomUpdate(room, roomId, localPlayerIndex)
            }
        }
    }

    fun cancelRankedQueue() {
        rankedAssignmentJob?.cancel()
        rankedAssignmentJob = null
        viewModelScope.launch {
            runCatching { onlineGameRepository.leaveRankedQueue(localId) }
                .onFailure {
                    _uiState.value = _uiState.value.copy(error = "Failed to leave ranked queue")
                }
            _uiState.value = _uiState.value.copy(isQueueingRanked = false, createdRoomId = null)
        }
    }

    private fun joinRankedQueue() {
        val name = _uiState.value.playerName.trim()
        if (name.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Enter your name first")
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, isQueueingRanked = true, error = null)
        viewModelScope.launch {
            val enqueueResult = runCatching { onlineGameRepository.joinRankedQueue(localId, name) }
            if (enqueueResult.isFailure) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isQueueingRanked = false,
                    error = "Failed to join ranked queue"
                )
                return@launch
            }
            _uiState.value = _uiState.value.copy(isLoading = false)
            observeRankedAssignment()
        }
    }

    private fun observeRankedAssignment() {
        if (rankedAssignmentJob?.isActive == true) return
        rankedAssignmentJob = viewModelScope.launch {
            onlineGameRepository.observeRankedAssignment(localId).collectLatest { assignment ->
                if (assignment == null || _uiState.value.navigateToGame != null) return@collectLatest
                val (roomId, localPlayerIndex) = assignment
                _uiState.value = _uiState.value.copy(
                    isQueueingRanked = false,
                    navigateToGame = NavigateToOnlineGame(roomId, localPlayerIndex, localId)
                )
                runCatching { onlineGameRepository.leaveRankedQueue(localId) }
                    .onFailure {
                        _uiState.value = _uiState.value.copy(error = "Failed to clean ranked queue")
                    }
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
                _uiState.value = _uiState.value.copy(
                    navigateToGame = NavigateToOnlineGame(roomId, localPlayerIndex, localId)
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

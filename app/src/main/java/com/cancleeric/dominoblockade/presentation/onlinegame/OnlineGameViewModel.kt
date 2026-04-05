package com.cancleeric.dominoblockade.presentation.onlinegame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cancleeric.dominoblockade.domain.model.Domino
import com.cancleeric.dominoblockade.domain.model.GameState
import com.cancleeric.dominoblockade.domain.model.OnlineRoomStatus
import com.cancleeric.dominoblockade.domain.repository.OnlineGameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val GRACE_PERIOD_SECONDS = 60
private const val ONE_SECOND_MS = 1_000L

@HiltViewModel
class OnlineGameViewModel @Inject constructor(
    private val onlineGameRepository: OnlineGameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnlineGameUiState())
    val uiState: StateFlow<OnlineGameUiState> = _uiState.asStateFlow()

    private var roomId: String = ""
    private var localPlayerIndex: Int = 0
    private var observeJob: Job? = null
    private var gracePeriodJob: Job? = null

    fun setup(roomId: String, localPlayerIndex: Int) {
        val isNewRoom = this.roomId != roomId
        this.roomId = roomId
        this.localPlayerIndex = localPlayerIndex
        viewModelScope.launch {
            runCatching {
                onlineGameRepository.markPlayerConnected(roomId, localPlayerIndex == 0)
                onlineGameRepository.registerDisconnectHandler(roomId, localPlayerIndex == 0)
            }
        }
        if (isNewRoom) {
            observeJob?.cancel()
            observeRoom()
        }
    }

    fun selectDomino(domino: Domino) {
        val current = _uiState.value.selectedDomino
        _uiState.value = _uiState.value.copy(selectedDomino = if (current == domino) null else domino)
    }

    fun placeDomino() {
        val state = _uiState.value.gameState ?: return
        val domino = _uiState.value.selectedDomino ?: return
        if (!_uiState.value.isMyTurn) return
        handlePlacement(state, domino)
    }

    fun drawFromBoneyard() {
        val state = _uiState.value.gameState ?: return
        if (!_uiState.value.isMyTurn) return
        if (state.boneyard.isEmpty()) {
            advanceTurn(state)
        } else {
            val tile = state.boneyard.first()
            val updatedPlayer = state.currentPlayer.copy(hand = state.currentPlayer.hand + tile)
            val updatedPlayers = state.players.toMutableList()
                .also { it[state.currentPlayerIndex] = updatedPlayer }
            syncToRemote(state.copy(players = updatedPlayers, boneyard = state.boneyard.drop(1)))
        }
    }

    fun leaveRoom() {
        viewModelScope.launch {
            runCatching { onlineGameRepository.leaveRoom(roomId) }
        }
    }

    private fun observeRoom() {
        observeJob = viewModelScope.launch {
            onlineGameRepository.observeRoom(roomId).collect { room ->
                if (room.status == OnlineRoomStatus.FINISHED) {
                    gracePeriodJob?.cancel()
                    _uiState.value = _uiState.value.copy(roomFinished = true)
                    return@collect
                }
                val gameState = room.gameState ?: return@collect
                val opponentIndex = 1 - localPlayerIndex
                val opponent = gameState.players.getOrNull(opponentIndex)
                val opponentDisconnectedAt = if (localPlayerIndex == 0) {
                    room.guestDisconnectedAt
                } else {
                    room.hostDisconnectedAt
                }
                handleOpponentConnection(opponentDisconnectedAt)
                _uiState.value = _uiState.value.copy(
                    gameState = gameState,
                    isMyTurn = gameState.currentPlayerIndex == localPlayerIndex,
                    isGameOver = gameState.isGameOver,
                    winnerName = gameState.winner?.name,
                    isBlocked = gameState.isBlocked,
                    opponentName = opponent?.name ?: "",
                    opponentTileCount = opponent?.hand?.size ?: 0,
                    isLoading = false
                )
            }
        }
    }

    private fun handleOpponentConnection(opponentDisconnectedAt: Long?) {
        if (opponentDisconnectedAt != null) {
            if (!_uiState.value.opponentDisconnected) {
                startGracePeriodCountdown()
            }
        } else {
            if (_uiState.value.opponentDisconnected) {
                gracePeriodJob?.cancel()
                _uiState.value = _uiState.value.copy(
                    opponentDisconnected = false,
                    gracePeriodSeconds = 0
                )
            }
        }
    }

    private fun startGracePeriodCountdown() {
        gracePeriodJob?.cancel()
        _uiState.value = _uiState.value.copy(
            opponentDisconnected = true,
            gracePeriodSeconds = GRACE_PERIOD_SECONDS
        )
        gracePeriodJob = viewModelScope.launch {
            for (remaining in (GRACE_PERIOD_SECONDS - 1) downTo 0) {
                delay(ONE_SECOND_MS)
                _uiState.value = _uiState.value.copy(gracePeriodSeconds = remaining)
                if (remaining == 0) {
                    runCatching { onlineGameRepository.leaveRoom(roomId) }
                    _uiState.value = _uiState.value.copy(roomFinished = true)
                }
            }
        }
    }

    private fun handlePlacement(state: GameState, domino: Domino) {
        val newState = computePlacement(state, domino) ?: return
        val updatedPlayer = newState.players[state.currentPlayerIndex]
        if (updatedPlayer.hand.isEmpty()) {
            syncToRemote(
                newState.copy(isGameOver = true, winner = updatedPlayer)
            )
        } else {
            advanceTurn(newState)
        }
    }

    private fun advanceTurn(state: GameState) {
        val nextIndex = (state.currentPlayerIndex + 1) % state.players.size
        val nextState = state.copy(currentPlayerIndex = nextIndex)
        val blocked = checkBlocked(nextState)
        syncToRemote(nextState.copy(isBlocked = blocked, isGameOver = blocked))
    }

    private fun syncToRemote(state: GameState) {
        _uiState.value = _uiState.value.copy(selectedDomino = null)
        viewModelScope.launch {
            runCatching { onlineGameRepository.updateGameState(roomId, state) }
        }
    }
}

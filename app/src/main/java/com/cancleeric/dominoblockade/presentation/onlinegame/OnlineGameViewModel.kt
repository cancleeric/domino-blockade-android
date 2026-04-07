package com.cancleeric.dominoblockade.presentation.onlinegame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cancleeric.dominoblockade.domain.model.Domino
import com.cancleeric.dominoblockade.domain.model.GameState
import com.cancleeric.dominoblockade.domain.model.OnlineRoom
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
import javax.inject.Named

@HiltViewModel
class OnlineGameViewModel @Inject constructor(
    private val onlineGameRepository: OnlineGameRepository,
    @Named("reconnectTimeout") private val reconnectTimeoutSeconds: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnlineGameUiState())
    val uiState: StateFlow<OnlineGameUiState> = _uiState.asStateFlow()

    private var roomId: String = ""
    private var localPlayerIndex: Int = 0
    private var localPlayerId: String = ""
    private var observeJob: Job? = null
    private var countdownJob: Job? = null

    fun setup(roomId: String, localPlayerIndex: Int, localPlayerId: String = "") {
        if (this.roomId == roomId) return
        this.roomId = roomId
        this.localPlayerIndex = localPlayerIndex
        this.localPlayerId = localPlayerId
        observeJob?.cancel()
        observeRoom()
        if (localPlayerId.isNotEmpty()) {
            viewModelScope.launch {
                runCatching { onlineGameRepository.registerPresence(roomId, localPlayerId) }
            }
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
                    _uiState.value = _uiState.value.copy(roomFinished = true)
                    return@collect
                }
                val gameState = room.gameState ?: return@collect
                val opponentIndex = 1 - localPlayerIndex
                val opponent = gameState.players.getOrNull(opponentIndex)
                handleDisconnectState(room, opponent?.id.orEmpty(), opponent?.name.orEmpty())
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

    private fun handleDisconnectState(room: OnlineRoom, opponentId: String, opponentName: String) {
        val isOpponentDisconnected = room.disconnectedPlayerId != null &&
            room.disconnectedPlayerId == opponentId
        if (isOpponentDisconnected) {
            if (countdownJob?.isActive != true) {
                countdownJob = viewModelScope.launch {
                    var remaining = reconnectTimeoutSeconds
                    _uiState.value = _uiState.value.copy(
                        reconnectionCountdown = remaining,
                        disconnectedOpponentName = opponentName
                    )
                    while (remaining > 0) {
                        delay(1_000L)
                        remaining--
                        if (remaining > 0) {
                            _uiState.value = _uiState.value.copy(reconnectionCountdown = remaining)
                        }
                    }
                    _uiState.value = _uiState.value.copy(
                        roomFinished = true,
                        reconnectionCountdown = null
                    )
                    runCatching { onlineGameRepository.leaveRoom(roomId) }
                }
            }
        } else if (countdownJob?.isActive == true) {
            countdownJob?.cancel()
            countdownJob = null
            _uiState.value = _uiState.value.copy(
                reconnectionCountdown = null,
                disconnectedOpponentName = null
            )
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

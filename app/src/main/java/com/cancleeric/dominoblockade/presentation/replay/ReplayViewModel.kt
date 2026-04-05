package com.cancleeric.dominoblockade.presentation.replay

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cancleeric.dominoblockade.data.local.MoveHistorySerializer
import com.cancleeric.dominoblockade.domain.model.Domino
import com.cancleeric.dominoblockade.domain.model.GameMove
import com.cancleeric.dominoblockade.domain.model.Player
import com.cancleeric.dominoblockade.domain.repository.GameRecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val AUTO_PLAY_DELAY_MS = 1500L

data class ReplayUiState(
    val isLoading: Boolean = true,
    val moves: List<GameMove> = emptyList(),
    val boardStates: List<List<Domino>> = emptyList(),
    val playerHandStates: List<List<Player>> = emptyList(),
    val currentStep: Int = 0,
    val isPlaying: Boolean = false,
    val hasReplayData: Boolean = false
) {
    val totalSteps: Int get() = moves.size
    val currentBoard: List<Domino> get() = boardStates.getOrElse(currentStep) { emptyList() }
    val currentPlayers: List<Player> get() = playerHandStates.getOrElse(currentStep) { emptyList() }
    val currentMove: GameMove? get() = moves.getOrNull(currentStep - 1)
}

@HiltViewModel
class ReplayViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: GameRecordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReplayUiState())
    val uiState: StateFlow<ReplayUiState> = _uiState.asStateFlow()

    private var autoPlayJob: Job? = null

    init {
        val recordId = savedStateHandle.get<Long>("recordId") ?: 0L
        loadRecord(recordId)
    }

    fun stepForward() {
        val state = _uiState.value
        if (state.currentStep < state.totalSteps) {
            _uiState.value = state.copy(currentStep = state.currentStep + 1)
        }
    }

    fun stepBackward() {
        val state = _uiState.value
        if (state.currentStep > 0) {
            _uiState.value = state.copy(currentStep = state.currentStep - 1)
        }
    }

    fun seekTo(step: Int) {
        val state = _uiState.value
        val clamped = step.coerceIn(0, state.totalSteps)
        _uiState.value = state.copy(currentStep = clamped)
    }

    fun togglePlay() {
        val state = _uiState.value
        if (state.isPlaying) {
            autoPlayJob?.cancel()
            _uiState.value = state.copy(isPlaying = false)
        } else {
            _uiState.value = state.copy(isPlaying = true)
            autoPlayJob = viewModelScope.launch {
                while (_uiState.value.currentStep < _uiState.value.totalSteps) {
                    delay(AUTO_PLAY_DELAY_MS)
                    stepForward()
                }
                _uiState.value = _uiState.value.copy(isPlaying = false)
            }
        }
    }

    private fun loadRecord(recordId: Long) {
        viewModelScope.launch {
            val entity = repository.getById(recordId).filterNotNull().first()
            val moves = entity.moveHistory?.let { MoveHistorySerializer.deserializeMoves(it) } ?: emptyList()
            val initialPlayers = entity.initialHandsJson
                ?.let { MoveHistorySerializer.deserializePlayers(it) }
                ?: emptyList()
            if (moves.isEmpty() || initialPlayers.isEmpty()) {
                _uiState.value = _uiState.value.copy(isLoading = false, hasReplayData = false)
                return@launch
            }
            val (boardStates, handStates) = reconstructStates(initialPlayers, moves)
            _uiState.value = ReplayUiState(
                isLoading = false,
                hasReplayData = true,
                moves = moves,
                boardStates = boardStates,
                playerHandStates = handStates,
                currentStep = 0
            )
        }
    }

    private fun reconstructStates(
        initialPlayers: List<Player>,
        moves: List<GameMove>
    ): Pair<List<List<Domino>>, List<List<Player>>> {
        val boardStates = mutableListOf<List<Domino>>()
        val handStates = mutableListOf<List<Player>>()
        var board = emptyList<Domino>()
        var players = initialPlayers
        boardStates.add(board)
        handStates.add(players)
        for (move in moves) {
            board = applyMoveToBoard(board, move)
            players = applyMoveToHands(players, move)
            boardStates.add(board)
            handStates.add(players)
        }
        return boardStates to handStates
    }

    private fun applyMoveToBoard(board: List<Domino>, move: GameMove): List<Domino> {
        val domino = Domino(move.dominoLeft, move.dominoRight)
        return when (move.boardEnd) {
            "INITIAL" -> listOf(domino)
            "RIGHT" -> {
                val rightEnd = board.lastOrNull()?.right
                val oriented = if (rightEnd != null && domino.left != rightEnd) {
                    Domino(domino.right, domino.left)
                } else domino
                board + oriented
            }
            else -> {
                val leftEnd = board.firstOrNull()?.left
                val oriented = if (leftEnd != null && domino.right != leftEnd) {
                    Domino(domino.right, domino.left)
                } else domino
                listOf(oriented) + board
            }
        }
    }

    private fun applyMoveToHands(players: List<Player>, move: GameMove): List<Player> {
        val domino = Domino(move.dominoLeft, move.dominoRight)
        return players.map { player ->
            if (player.id == move.playerId) {
                val removed = player.hand.firstOrNull { it == domino || it == Domino(domino.right, domino.left) }
                player.copy(hand = if (removed != null) player.hand - removed else player.hand)
            } else {
                player
            }
        }
    }
}

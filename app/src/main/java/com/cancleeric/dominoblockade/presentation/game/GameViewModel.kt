package com.cancleeric.dominoblockade.presentation.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cancleeric.dominoblockade.domain.analytics.AnalyticsTracker
import com.cancleeric.dominoblockade.domain.model.Domino
import com.cancleeric.dominoblockade.domain.model.GameState
import com.cancleeric.dominoblockade.domain.model.ReplayStep
import com.cancleeric.dominoblockade.domain.usecase.StartGameUseCase
import com.cancleeric.dominoblockade.presentation.replay.GameReplayRecorder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GameUiState(
    val gameState: GameState? = null,
    val selectedDomino: Domino? = null,
    val isGameOver: Boolean = false,
    val winnerName: String? = null,
    val isBlocked: Boolean = false
)

@HiltViewModel
class GameViewModel @Inject constructor(
    private val startGameUseCase: StartGameUseCase,
    private val analyticsTracker: AnalyticsTracker,
    private val replayRecorder: GameReplayRecorder
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    fun startGame(playerCount: Int) {
        val names = (1..playerCount).map { "Player $it" }
        val gameState = startGameUseCase(names)
        replayRecorder.reset()
        replayRecorder.recordMove(
            playerIndex = gameState.currentPlayerIndex,
            playerName = gameState.currentPlayer.name,
            moveType = ReplayStep.MOVE_DEAL,
            domino = null,
            board = gameState.board,
            boneyardSize = gameState.boneyard.size
        )
        _uiState.value = GameUiState(gameState = gameState)
        analyticsTracker.logGameStart(playerCount)
    }

    fun selectDomino(domino: Domino) {
        val current = _uiState.value.selectedDomino
        _uiState.value = _uiState.value.copy(selectedDomino = if (current == domino) null else domino)
    }

    fun placeDomino() {
        val state = _uiState.value.gameState ?: return
        val domino = _uiState.value.selectedDomino ?: return
        applyPlacement(state, domino)
    }

    fun drawFromBoneyard() {
        val state = _uiState.value.gameState ?: return
        if (state.boneyard.isEmpty()) {
            replayRecorder.recordMove(
                playerIndex = state.currentPlayerIndex,
                playerName = state.currentPlayer.name,
                moveType = ReplayStep.MOVE_SKIP,
                domino = null,
                board = state.board,
                boneyardSize = 0
            )
            advanceGame(state)
        } else {
            val tile = state.boneyard.first()
            val updatedPlayer = state.currentPlayer.copy(hand = state.currentPlayer.hand + tile)
            val updatedPlayers = state.players.toMutableList()
            updatedPlayers[state.currentPlayerIndex] = updatedPlayer
            val newState = state.copy(players = updatedPlayers, boneyard = state.boneyard.drop(1))
            replayRecorder.recordMove(
                playerIndex = state.currentPlayerIndex,
                playerName = state.currentPlayer.name,
                moveType = ReplayStep.MOVE_DRAW,
                domino = null,
                board = newState.board,
                boneyardSize = newState.boneyard.size
            )
            _uiState.value = _uiState.value.copy(gameState = newState, selectedDomino = null)
        }
    }

    private fun applyPlacement(state: GameState, domino: Domino) {
        val newState = computePlacement(state, domino)
        if (newState != null) {
            replayRecorder.recordMove(
                playerIndex = state.currentPlayerIndex,
                playerName = state.currentPlayer.name,
                moveType = ReplayStep.MOVE_PLACE,
                domino = domino,
                board = newState.board,
                boneyardSize = newState.boneyard.size
            )
            val updatedPlayer = newState.players[state.currentPlayerIndex]
            if (updatedPlayer.hand.isEmpty()) {
                analyticsTracker.logGameEnd(
                    winner = updatedPlayer.name,
                    isBlocked = false,
                    durationSeconds = 0L,
                    winRate = 0f
                )
                viewModelScope.launch {
                    replayRecorder.saveReplay(state.players.size, updatedPlayer.name, false)
                }
                _uiState.value = _uiState.value.copy(
                    gameState = newState, selectedDomino = null,
                    isGameOver = true, winnerName = updatedPlayer.name
                )
            } else {
                advanceGame(newState)
            }
        }
    }

    private fun advanceGame(state: GameState) {
        val nextIndex = (state.currentPlayerIndex + 1) % state.players.size
        val nextState = state.copy(currentPlayerIndex = nextIndex)
        val blocked = checkBlocked(nextState)
        if (blocked) {
            analyticsTracker.logGameBlocked()
            viewModelScope.launch {
                replayRecorder.saveReplay(state.players.size, "", true)
            }
        }
        _uiState.value = _uiState.value.copy(
            gameState = nextState, selectedDomino = null,
            isBlocked = blocked, isGameOver = blocked
        )
    }

    private fun checkBlocked(state: GameState): Boolean {
        if (state.boneyard.isNotEmpty()) return false
        return state.players.none { player -> player.hand.any { state.canPlace(it) } }
    }

    private fun computePlacement(state: GameState, domino: Domino): GameState? {
        return if (state.board.isEmpty()) {
            val base = removeFromHand(state, domino)
            base.copy(board = listOf(domino), leftEnd = domino.left, rightEnd = domino.right)
        } else {
            placeAtEnd(state, domino, state.rightEnd, isRight = true)
                ?: placeAtEnd(state, domino, state.leftEnd, isRight = false)
        }
    }

    private fun placeAtEnd(state: GameState, domino: Domino, endValue: Int?, isRight: Boolean): GameState? {
        val oriented = if (endValue != null) orientDomino(domino, endValue, isRight) else null
        return if (oriented != null) {
            val base = removeFromHand(state, domino)
            if (isRight) {
                base.copy(board = base.board + oriented, rightEnd = oriented.right)
            } else {
                base.copy(board = listOf(oriented) + base.board, leftEnd = oriented.left)
            }
        } else {
            null
        }
    }

    private fun orientDomino(domino: Domino, endValue: Int, connectRight: Boolean): Domino? = when {
        connectRight && domino.left == endValue -> domino
        connectRight && domino.right == endValue -> Domino(domino.right, domino.left)
        !connectRight && domino.right == endValue -> domino
        !connectRight && domino.left == endValue -> Domino(domino.right, domino.left)
        else -> null
    }

    private fun removeFromHand(state: GameState, domino: Domino): GameState {
        val updatedHand = state.currentPlayer.hand - domino
        val updatedPlayer = state.currentPlayer.copy(hand = updatedHand)
        val updatedPlayers = state.players.toMutableList()
        updatedPlayers[state.currentPlayerIndex] = updatedPlayer
        return state.copy(players = updatedPlayers)
    }
}

package com.cancleeric.dominoblockade.presentation.game

import androidx.lifecycle.ViewModel
import com.cancleeric.dominoblockade.data.analytics.AnalyticsTracker
import com.cancleeric.dominoblockade.domain.model.Domino
import com.cancleeric.dominoblockade.domain.model.GameState
import com.cancleeric.dominoblockade.domain.usecase.StartGameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

private const val GAME_MODE_SOLO = "solo"
private const val DIFFICULTY_NA = "n_a"
private const val RESULT_WIN = "win"
private const val RESULT_DRAW = "draw"
private const val BOARD_END_RIGHT = "right"
private const val BOARD_END_LEFT = "left"

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
    private val analyticsTracker: AnalyticsTracker
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var gameStartTime = 0L
    private var turnCount = 0

    fun startGame(playerCount: Int) {
        val names = (1..playerCount).map { "Player $it" }
        gameStartTime = System.currentTimeMillis()
        turnCount = 0
        analyticsTracker.logGameStart(playerCount, GAME_MODE_SOLO, DIFFICULTY_NA)
        _uiState.value = GameUiState(gameState = startGameUseCase(names))
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
            advanceGame(state)
        } else {
            val tile = state.boneyard.first()
            val updatedPlayer = state.currentPlayer.copy(hand = state.currentPlayer.hand + tile)
            val updatedPlayers = state.players.toMutableList()
            updatedPlayers[state.currentPlayerIndex] = updatedPlayer
            _uiState.value = _uiState.value.copy(
                gameState = state.copy(players = updatedPlayers, boneyard = state.boneyard.drop(1)),
                selectedDomino = null
            )
        }
    }

    private fun applyPlacement(state: GameState, domino: Domino) {
        val newState = computePlacement(state, domino)
        if (newState != null) {
            val boardEnd = if (state.board.isEmpty() || newState.rightEnd != state.rightEnd) {
                BOARD_END_RIGHT
            } else {
                BOARD_END_LEFT
            }
            analyticsTracker.logDominoPlaced(domino.left, domino.right, boardEnd)
            val updatedPlayer = newState.players[state.currentPlayerIndex]
            if (updatedPlayer.hand.isEmpty()) {
                turnCount++
                val duration = (System.currentTimeMillis() - gameStartTime) / 1000
                analyticsTracker.logGameEnd(RESULT_WIN, duration, GAME_MODE_SOLO, turnCount)
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
        turnCount++
        if (blocked) {
            val remainingPips = nextState.players.sumOf { p -> p.hand.sumOf { it.left + it.right } }
            analyticsTracker.logBlockadeTriggered(turnCount, remainingPips)
            val duration = (System.currentTimeMillis() - gameStartTime) / 1000
            analyticsTracker.logGameEnd(RESULT_DRAW, duration, GAME_MODE_SOLO, turnCount)
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

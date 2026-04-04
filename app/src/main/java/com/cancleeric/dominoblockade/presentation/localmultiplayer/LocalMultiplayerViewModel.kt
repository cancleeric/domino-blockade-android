package com.cancleeric.dominoblockade.presentation.localmultiplayer

import androidx.lifecycle.ViewModel
import com.cancleeric.dominoblockade.data.analytics.AnalyticsTracker
import com.cancleeric.dominoblockade.domain.model.Domino
import com.cancleeric.dominoblockade.domain.model.GameState
import com.cancleeric.dominoblockade.domain.model.computePlacement
import com.cancleeric.dominoblockade.domain.usecase.StartGameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

private const val MIN_PLAYERS = 2
private const val MAX_PLAYERS = 4
private const val DEFAULT_PLAYER_COUNT = 2
private const val GAME_MODE_LOCAL_MULTI = "local_multi"
private const val DIFFICULTY_NA = "n_a"
private const val RESULT_WIN = "win"
private const val RESULT_DRAW = "draw"
private const val BOARD_END_RIGHT = "right"
private const val BOARD_END_LEFT = "left"

data class LocalMultiplayerUiState(
    val playerCount: Int = DEFAULT_PLAYER_COUNT,
    val playerNames: List<String> = List(DEFAULT_PLAYER_COUNT) { "Player ${it + 1}" },
    val gameState: GameState? = null,
    val selectedDomino: Domino? = null,
    val isGameOver: Boolean = false,
    val winnerName: String? = null,
    val isBlocked: Boolean = false,
    val isPassingDevice: Boolean = false
)

@HiltViewModel
class LocalMultiplayerViewModel @Inject constructor(
    private val startGameUseCase: StartGameUseCase,
    private val analyticsTracker: AnalyticsTracker
) : ViewModel() {

    private val _uiState = MutableStateFlow(LocalMultiplayerUiState())
    val uiState: StateFlow<LocalMultiplayerUiState> = _uiState.asStateFlow()

    private var gameStartTime = 0L
    private var turnCount = 0

    fun updatePlayerCount(count: Int) {
        val clamped = count.coerceIn(MIN_PLAYERS, MAX_PLAYERS)
        val names = List(clamped) { _uiState.value.playerNames.getOrElse(it) { "Player ${it + 1}" } }
        _uiState.value = _uiState.value.copy(playerCount = clamped, playerNames = names)
    }

    fun updatePlayerName(index: Int, name: String) {
        val names = _uiState.value.playerNames.toMutableList()
        if (index in names.indices) {
            names[index] = name
            _uiState.value = _uiState.value.copy(playerNames = names)
        }
    }

    fun startGame() {
        val count = _uiState.value.playerCount
        val names = _uiState.value.playerNames.take(count)
            .mapIndexed { i, n -> n.ifBlank { "Player ${i + 1}" } }
        gameStartTime = System.currentTimeMillis()
        turnCount = 0
        analyticsTracker.logGameStart(count, GAME_MODE_LOCAL_MULTI, DIFFICULTY_NA)
        _uiState.value = LocalMultiplayerUiState(
            playerCount = _uiState.value.playerCount,
            playerNames = _uiState.value.playerNames,
            gameState = startGameUseCase(names)
        )
    }

    fun selectDomino(domino: Domino) {
        val current = _uiState.value.selectedDomino
        _uiState.value = _uiState.value.copy(
            selectedDomino = if (current == domino) null else domino
        )
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

    fun confirmDevicePassed() {
        _uiState.value = _uiState.value.copy(isPassingDevice = false)
    }

    private fun applyPlacement(state: GameState, domino: Domino) {
        val newState = state.computePlacement(domino) ?: return
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
            analyticsTracker.logGameEnd(RESULT_WIN, duration, GAME_MODE_LOCAL_MULTI, turnCount)
            _uiState.value = _uiState.value.copy(
                gameState = newState, selectedDomino = null,
                isGameOver = true, winnerName = updatedPlayer.name,
                isPassingDevice = false
            )
        } else {
            advanceGame(newState)
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
            analyticsTracker.logGameEnd(RESULT_DRAW, duration, GAME_MODE_LOCAL_MULTI, turnCount)
        }
        _uiState.value = _uiState.value.copy(
            gameState = nextState, selectedDomino = null,
            isBlocked = blocked, isGameOver = blocked,
            isPassingDevice = !blocked
        )
    }

    private fun checkBlocked(state: GameState): Boolean {
        if (state.boneyard.isNotEmpty()) return false
        return state.players.none { player -> player.hand.any { state.canPlace(it) } }
    }
}

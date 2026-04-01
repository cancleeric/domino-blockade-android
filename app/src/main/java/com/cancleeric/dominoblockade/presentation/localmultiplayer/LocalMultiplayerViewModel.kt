package com.cancleeric.dominoblockade.presentation.localmultiplayer

import androidx.lifecycle.ViewModel
import com.cancleeric.dominoblockade.domain.model.Domino
import com.cancleeric.dominoblockade.domain.model.GameState
import com.cancleeric.dominoblockade.domain.usecase.StartGameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

private const val MIN_PLAYERS = 2
private const val MAX_PLAYERS = 4
private const val DEFAULT_PLAYER_COUNT = 2

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
    private val startGameUseCase: StartGameUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LocalMultiplayerUiState())
    val uiState: StateFlow<LocalMultiplayerUiState> = _uiState.asStateFlow()

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
        val newState = computePlacement(state, domino) ?: return
        val updatedPlayer = newState.players[state.currentPlayerIndex]
        if (updatedPlayer.hand.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                gameState = newState, selectedDomino = null,
                isGameOver = true, winnerName = updatedPlayer.name
            )
        } else {
            advanceGame(newState)
        }
    }

    private fun advanceGame(state: GameState) {
        val nextIndex = (state.currentPlayerIndex + 1) % state.players.size
        val nextState = state.copy(currentPlayerIndex = nextIndex)
        val blocked = checkBlocked(nextState)
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

    private fun computePlacement(state: GameState, domino: Domino): GameState? {
        val updatedPlayers = state.players.toMutableList().also {
            it[state.currentPlayerIndex] = state.currentPlayer.copy(
                hand = state.currentPlayer.hand - domino
            )
        }
        val base = state.copy(players = updatedPlayers)
        return when {
            state.board.isEmpty() ->
                base.copy(board = listOf(domino), leftEnd = domino.left, rightEnd = domino.right)
            domino.left == state.rightEnd ->
                base.copy(board = base.board + domino, rightEnd = domino.right)
            domino.right == state.rightEnd ->
                base.copy(
                    board = base.board + Domino(domino.right, domino.left),
                    rightEnd = domino.left
                )
            domino.right == state.leftEnd ->
                base.copy(board = listOf(domino) + base.board, leftEnd = domino.left)
            domino.left == state.leftEnd ->
                base.copy(
                    board = listOf(Domino(domino.right, domino.left)) + base.board,
                    leftEnd = domino.right
                )
            else -> null
        }
    }
}

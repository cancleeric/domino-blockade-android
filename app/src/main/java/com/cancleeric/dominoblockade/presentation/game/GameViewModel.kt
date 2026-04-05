package com.cancleeric.dominoblockade.presentation.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cancleeric.dominoblockade.data.local.MoveHistorySerializer
import com.cancleeric.dominoblockade.data.local.entity.GameRecordEntity
import com.cancleeric.dominoblockade.domain.model.Domino
import com.cancleeric.dominoblockade.domain.model.GameMove
import com.cancleeric.dominoblockade.domain.model.GameState
import com.cancleeric.dominoblockade.domain.repository.GameRecordRepository
import com.cancleeric.dominoblockade.domain.usecase.StartGameUseCase
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
    private val gameRecordRepository: GameRecordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _moves = mutableListOf<GameMove>()
    private var _initialHandsJson = ""
    private var _gameStartTime = 0L

    fun startGame(playerCount: Int) {
        val names = (1..playerCount).map { "Player $it" }
        val gameState = startGameUseCase(names)
        _moves.clear()
        _initialHandsJson = MoveHistorySerializer.serializePlayers(gameState.players)
        _gameStartTime = System.currentTimeMillis()
        _uiState.value = GameUiState(gameState = gameState)
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
            val boardEnd = when {
                state.board.isEmpty() -> "INITIAL"
                newState.rightEnd != state.rightEnd -> "RIGHT"
                else -> "LEFT"
            }
            _moves.add(
                GameMove(
                    playerId = state.currentPlayer.id,
                    playerName = state.currentPlayer.name,
                    dominoLeft = domino.left,
                    dominoRight = domino.right,
                    boardEnd = boardEnd,
                    timestamp = System.currentTimeMillis()
                )
            )
            val updatedPlayer = newState.players[state.currentPlayerIndex]
            if (updatedPlayer.hand.isEmpty()) {
                val winnerScore = newState.players
                    .filterIndexed { idx, _ -> idx != state.currentPlayerIndex }
                    .sumOf { p -> p.hand.sumOf { it.total } }
                val record = GameRecordEntity(
                    playerCount = newState.players.size,
                    winnerName = updatedPlayer.name,
                    winnerScore = winnerScore,
                    gameMode = "local",
                    aiDifficulty = null,
                    isBlocked = false,
                    durationSeconds = ((System.currentTimeMillis() - _gameStartTime) / 1000).toInt(),
                    moveHistory = MoveHistorySerializer.serializeMoves(_moves.toList()),
                    initialHandsJson = _initialHandsJson
                )
                viewModelScope.launch { gameRecordRepository.insert(record) }
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
            val record = GameRecordEntity(
                playerCount = nextState.players.size,
                winnerName = "",
                winnerScore = 0,
                gameMode = "local",
                aiDifficulty = null,
                isBlocked = true,
                durationSeconds = ((System.currentTimeMillis() - _gameStartTime) / 1000).toInt(),
                moveHistory = MoveHistorySerializer.serializeMoves(_moves.toList()),
                initialHandsJson = _initialHandsJson
            )
            viewModelScope.launch { gameRecordRepository.insert(record) }
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

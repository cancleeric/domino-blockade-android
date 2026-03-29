package com.cancleeric.dominoblockade.presentation.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cancleeric.dominoblockade.ai.AiPlayer
import com.cancleeric.dominoblockade.domain.GameEngine
import com.cancleeric.dominoblockade.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GameUiState(
    val gameState: GameState? = null,
    val selectedDomino: Domino? = null,
    val isAiThinking: Boolean = false,
    val message: String = "",
    val playableDominoes: Set<Domino> = emptySet()
)

@HiltViewModel
class GameViewModel @Inject constructor(
    private val gameEngine: GameEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var aiDifficulty: AiDifficulty = AiDifficulty.EASY
    private var aiPlayer: AiPlayer = AiPlayer(aiDifficulty)

    fun startGame(numPlayers: Int, difficulty: String) {
        aiDifficulty = try {
            AiDifficulty.valueOf(difficulty)
        } catch (e: IllegalArgumentException) {
            AiDifficulty.EASY
        }
        aiPlayer = AiPlayer(aiDifficulty)
        val state = gameEngine.deal(numPlayers)
        val playable = computePlayable(state)
        _uiState.update {
            GameUiState(
                gameState = state,
                message = "${state.currentPlayer.name}'s turn",
                playableDominoes = playable
            )
        }
        if (state.currentPlayer.isAi) {
            triggerAiTurn()
        }
    }

    fun onDominoSelected(domino: Domino) {
        val state = _uiState.value.gameState ?: return
        if (state.currentPlayer.isAi) return
        val currentSelected = _uiState.value.selectedDomino
        _uiState.update {
            it.copy(selectedDomino = if (currentSelected == domino) null else domino)
        }
    }

    fun onEndSelected(end: BoardEnd) {
        val state = _uiState.value.gameState ?: return
        val domino = _uiState.value.selectedDomino ?: return

        val validMoves = state.validMovesFor(state.currentPlayer)
        val move = validMoves.find { it.first == domino && it.second == end } ?: return

        val newState = gameEngine.placeDomino(state, move.first, move.second)
        val playable = computePlayable(newState)
        _uiState.update {
            it.copy(
                gameState = newState,
                selectedDomino = null,
                message = buildMessage(newState),
                playableDominoes = playable
            )
        }

        if (newState.phase == GamePhase.PLAYING && newState.currentPlayer.isAi) {
            triggerAiTurn()
        }
    }

    fun onDrawTile() {
        val state = _uiState.value.gameState ?: return
        if (state.currentPlayer.isAi || state.drawPile.isEmpty()) return

        val newState = gameEngine.drawTile(state)
        val playable = computePlayable(newState)
        _uiState.update {
            it.copy(
                gameState = newState,
                message = "Drew a tile. ${buildMessage(newState)}",
                playableDominoes = playable
            )
        }
    }

    fun onSkipTurn() {
        val state = _uiState.value.gameState ?: return
        if (state.currentPlayer.isAi) return

        val newState = gameEngine.skipTurn(state)
        val playable = computePlayable(newState)
        _uiState.update {
            it.copy(
                gameState = newState,
                selectedDomino = null,
                message = buildMessage(newState),
                playableDominoes = playable
            )
        }

        if (newState.phase == GamePhase.PLAYING && newState.currentPlayer.isAi) {
            triggerAiTurn()
        }
    }

    private fun triggerAiTurn() {
        viewModelScope.launch {
            _uiState.update { it.copy(isAiThinking = true, message = "AI is thinking...") }
            delay(800L)

            var state = _uiState.value.gameState ?: return@launch
            val move = aiPlayer.chooseMove(state)
            state = if (move != null) {
                gameEngine.placeDomino(state, move.domino, move.end)
            } else if (state.drawPile.isNotEmpty()) {
                gameEngine.drawTile(state)
            } else {
                gameEngine.skipTurn(state)
            }

            val playable = computePlayable(state)
            _uiState.update {
                it.copy(
                    gameState = state,
                    isAiThinking = false,
                    selectedDomino = null,
                    message = buildMessage(state),
                    playableDominoes = playable
                )
            }

            if (state.phase == GamePhase.PLAYING && state.currentPlayer.isAi) {
                triggerAiTurn()
            }
        }
    }

    private fun buildMessage(state: GameState): String {
        return when (state.phase) {
            GamePhase.PLAYING -> "${state.currentPlayer.name}'s turn"
            GamePhase.BLOCKED -> "Game blocked! ${state.players.getOrNull(state.winnerIndex ?: -1)?.name ?: "Unknown"} wins!"
            GamePhase.FINISHED -> "${state.players.getOrNull(state.winnerIndex ?: -1)?.name ?: "Unknown"} wins!"
        }
    }

    private fun computePlayable(state: GameState): Set<Domino> {
        return state.validMovesFor(state.currentPlayer).map { it.first }.toSet()
    }

    fun canPlaceAt(end: BoardEnd): Boolean {
        val state = _uiState.value.gameState ?: return false
        val domino = _uiState.value.selectedDomino ?: return false
        return state.validMovesFor(state.currentPlayer).any { it.first == domino && it.second == end }
    }
}

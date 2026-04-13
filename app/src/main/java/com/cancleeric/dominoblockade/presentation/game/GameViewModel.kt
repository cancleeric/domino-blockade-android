package com.cancleeric.dominoblockade.presentation.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cancleeric.dominoblockade.ai.AdaptiveAiStrategy
import com.cancleeric.dominoblockade.ai.BoardEnd
import com.cancleeric.dominoblockade.ai.ValidMove
import com.cancleeric.dominoblockade.domain.analytics.AnalyticsTracker
import com.cancleeric.dominoblockade.domain.model.Domino
import com.cancleeric.dominoblockade.domain.model.GameState
import com.cancleeric.dominoblockade.domain.model.GameMode
import com.cancleeric.dominoblockade.domain.model.ReplayStep
import com.cancleeric.dominoblockade.domain.model.computePlacement
import com.cancleeric.dominoblockade.domain.usecase.StartGameUseCase
import com.cancleeric.dominoblockade.domain.usecase.AdaptiveAiManager
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
    val isBlocked: Boolean = false,
    val currentAiLevel: Int = DEFAULT_AI_LEVEL
)

private const val HUMAN_PLAYER_INDEX = 0
private const val HUMAN_PLAYER_NAME = "Player 1"
private const val DEFAULT_AI_LEVEL = 50

@HiltViewModel
class GameViewModel @Inject constructor(
    private val startGameUseCase: StartGameUseCase,
    private val analyticsTracker: AnalyticsTracker,
    private val replayRecorder: GameReplayRecorder,
    private val adaptiveAiManager: AdaptiveAiManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            adaptiveAiManager.currentLevel.collect { level ->
                _uiState.value = _uiState.value.copy(currentAiLevel = level)
            }
        }
    }

    fun startGame(playerCount: Int) {
        val names = List(playerCount) { index ->
            if (index == HUMAN_PLAYER_INDEX) HUMAN_PLAYER_NAME else "AI ${index}"
        }
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
        _uiState.value = GameUiState(
            gameState = gameState,
            currentAiLevel = _uiState.value.currentAiLevel
        )
        analyticsTracker.logGameStart(playerCount, _uiState.value.currentAiLevel.toString())
        maybePlayAiTurns(gameState)
    }

    fun selectDomino(domino: Domino) {
        if (!isHumanTurn()) return
        val current = _uiState.value.selectedDomino
        _uiState.value = _uiState.value.copy(selectedDomino = if (current == domino) null else domino)
    }

    fun placeDomino() {
        if (!isHumanTurn()) return
        val state = _uiState.value.gameState ?: return
        val domino = _uiState.value.selectedDomino ?: return
        applyPlacement(state, domino)
    }

    fun drawFromBoneyard() {
        if (!isHumanTurn()) return
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
                finishGame(newState, updatedPlayer.name, isBlocked = false)
            } else {
                advanceGame(newState)
            }
        }
    }

    private fun advanceGame(state: GameState) {
        val nextState = state.copy(currentPlayerIndex = (state.currentPlayerIndex + 1) % state.players.size)
        val blocked = checkBlocked(nextState)
        if (blocked) finishGame(nextState, winnerName = "", isBlocked = true)
        else {
            _uiState.value = _uiState.value.copy(
                gameState = nextState,
                selectedDomino = null,
                isBlocked = false,
                isGameOver = false
            )
            maybePlayAiTurns(nextState)
        }
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

    private fun maybePlayAiTurns(state: GameState) {
        if (state.currentPlayerIndex == HUMAN_PLAYER_INDEX || _uiState.value.isGameOver) return
        viewModelScope.launch {
            playAiTurns(state)
        }
    }

    private suspend fun playAiTurns(initialState: GameState) {
        var state = initialState
        while (state.currentPlayerIndex != HUMAN_PLAYER_INDEX && !_uiState.value.isGameOver) {
            val player = state.currentPlayer
            val aiMove = AdaptiveAiStrategy(_uiState.value.currentAiLevel)
                .chooseMove(state, player, buildValidMoves(state))
            val advanced = when {
                aiMove != null -> {
                    val placedState = state.computePlacement(aiMove.domino) ?: state
                    replayRecorder.recordMove(
                        playerIndex = state.currentPlayerIndex,
                        playerName = player.name,
                        moveType = ReplayStep.MOVE_PLACE,
                        domino = aiMove.domino,
                        board = placedState.board,
                        boneyardSize = placedState.boneyard.size
                    )
                    if (placedState.players[state.currentPlayerIndex].hand.isEmpty()) {
                        finishGame(placedState, player.name, isBlocked = false)
                        return
                    }
                    placedState.copy(currentPlayerIndex = (placedState.currentPlayerIndex + 1) % placedState.players.size)
                }
                state.boneyard.isNotEmpty() -> {
                    val tile = state.boneyard.first()
                    val updatedPlayer = player.copy(hand = player.hand + tile)
                    val updatedPlayers = state.players.toMutableList().also {
                        it[state.currentPlayerIndex] = updatedPlayer
                    }
                    val drawnState = state.copy(players = updatedPlayers, boneyard = state.boneyard.drop(1))
                    replayRecorder.recordMove(
                        playerIndex = state.currentPlayerIndex,
                        playerName = player.name,
                        moveType = ReplayStep.MOVE_DRAW,
                        domino = null,
                        board = drawnState.board,
                        boneyardSize = drawnState.boneyard.size
                    )
                    val moveAfterDraw = AdaptiveAiStrategy(_uiState.value.currentAiLevel)
                        .chooseMove(drawnState, updatedPlayer, buildValidMoves(drawnState))
                    if (moveAfterDraw != null) {
                        val placedState = drawnState.computePlacement(moveAfterDraw.domino) ?: drawnState
                        replayRecorder.recordMove(
                            playerIndex = state.currentPlayerIndex,
                            playerName = player.name,
                            moveType = ReplayStep.MOVE_PLACE,
                            domino = moveAfterDraw.domino,
                            board = placedState.board,
                            boneyardSize = placedState.boneyard.size
                        )
                        if (placedState.players[state.currentPlayerIndex].hand.isEmpty()) {
                            finishGame(placedState, player.name, isBlocked = false)
                            return
                        }
                        placedState.copy(
                            currentPlayerIndex = (placedState.currentPlayerIndex + 1) % placedState.players.size
                        )
                    } else {
                        drawnState.copy(currentPlayerIndex = (drawnState.currentPlayerIndex + 1) % drawnState.players.size)
                    }
                }
                else -> {
                    replayRecorder.recordMove(
                        playerIndex = state.currentPlayerIndex,
                        playerName = player.name,
                        moveType = ReplayStep.MOVE_SKIP,
                        domino = null,
                        board = state.board,
                        boneyardSize = 0
                    )
                    state.copy(currentPlayerIndex = (state.currentPlayerIndex + 1) % state.players.size)
                }
            }

            if (checkBlocked(advanced)) {
                finishGame(advanced, winnerName = "", isBlocked = true)
                return
            }
            state = advanced
        }
        _uiState.value = _uiState.value.copy(gameState = state, selectedDomino = null)
    }

    private fun buildValidMoves(state: GameState): List<ValidMove> {
        if (state.board.isEmpty()) {
            return state.currentPlayer.hand.map { domino ->
                ValidMove(domino = domino, end = BoardEnd.RIGHT, needsFlip = false)
            }
        }
        val left = state.leftEnd ?: return emptyList()
        val right = state.rightEnd ?: return emptyList()
        return state.currentPlayer.hand.flatMap { domino ->
            buildList {
                if (domino.left == right) add(ValidMove(domino = domino, end = BoardEnd.RIGHT, needsFlip = false))
                if (domino.right == right) add(ValidMove(domino = domino, end = BoardEnd.RIGHT, needsFlip = true))
                if (domino.right == left) add(ValidMove(domino = domino, end = BoardEnd.LEFT, needsFlip = false))
                if (domino.left == left) add(ValidMove(domino = domino, end = BoardEnd.LEFT, needsFlip = true))
            }
        }.distinct()
    }

    private fun isHumanTurn(): Boolean =
        _uiState.value.gameState?.currentPlayerIndex == HUMAN_PLAYER_INDEX

    private fun finishGame(state: GameState, winnerName: String, isBlocked: Boolean) {
        if (isBlocked) analyticsTracker.logGameBlocked()
        else analyticsTracker.logGameEnd(
            winner = winnerName,
            isBlocked = false,
            durationSeconds = 0L,
            winRate = 0f
        )
        _uiState.value = _uiState.value.copy(
            gameState = state,
            selectedDomino = null,
            isGameOver = true,
            winnerName = winnerName,
            isBlocked = isBlocked
        )
        viewModelScope.launch {
            replayRecorder.saveReplay(state.players.size, winnerName, isBlocked)
            adaptiveAiManager.recordGameResult(
                gameMode = GameMode.QUICK_MATCH,
                playerWon = !isBlocked && winnerName == HUMAN_PLAYER_NAME
            )
        }
    }
}

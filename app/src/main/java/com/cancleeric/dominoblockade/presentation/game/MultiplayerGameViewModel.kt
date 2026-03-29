package com.cancleeric.dominoblockade.presentation.game

import androidx.lifecycle.ViewModel
import com.cancleeric.dominoblockade.domain.engine.BoardSide
import com.cancleeric.dominoblockade.domain.engine.GameEngine
import com.cancleeric.dominoblockade.domain.model.Domino
import com.cancleeric.dominoblockade.domain.model.GameConfig
import com.cancleeric.dominoblockade.domain.model.GamePhase
import com.cancleeric.dominoblockade.domain.model.GameState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * ViewModel for the multiplayer game screen.
 *
 * Supports 2–4 players with Pass & Play mechanics.
 * All game mutations go through [GameEngine] which keeps the ViewModel clean and testable.
 */
@HiltViewModel
class MultiplayerGameViewModel @Inject constructor(
    private val gameEngine: GameEngine
) : ViewModel() {

    private val _gameState = MutableStateFlow<GameState?>(null)
    val gameState: StateFlow<GameState?> = _gameState.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // -------------------------------------------------------------------------
    // Game lifecycle
    // -------------------------------------------------------------------------

    /** Starts a new game with the given [config]. */
    fun startGame(config: GameConfig) {
        _gameState.value = gameEngine.newGame(config)
        _error.value = null
    }

    /** Resets the game back to null state (e.g. after returning to menu). */
    fun resetGame() {
        _gameState.value = null
        _error.value = null
    }

    // -------------------------------------------------------------------------
    // Player actions
    // -------------------------------------------------------------------------

    /**
     * The current player places [domino] on [side] of the board.
     *
     * On success: transitions to PassAndPlay phase (or GameOver if hand is empty).
     * On failure: populates [error] with a message.
     */
    fun playDomino(domino: Domino, side: BoardSide) {
        val state = _gameState.value ?: return
        if (state.phase !is GamePhase.PlayerTurn) return
        try {
            _gameState.update { gameEngine.placeDomino(state, domino, side) }
            _error.value = null
        } catch (e: IllegalArgumentException) {
            _error.value = e.message
        }
    }

    /**
     * The current player draws a tile from the pile.
     *
     * If the pile is empty or a blockade is detected, the game ends automatically.
     */
    fun drawTile() {
        val state = _gameState.value ?: return
        if (state.phase !is GamePhase.PlayerTurn) return
        _gameState.update { gameEngine.drawTile(state) }
    }

    /**
     * Confirms the Pass & Play handoff screen.
     *
     * Call when the next player presses "I'm ready" to reveal their hand.
     */
    fun confirmPassAndPlay() {
        val state = _gameState.value ?: return
        if (state.phase !is GamePhase.PassAndPlay) return
        _gameState.update { gameEngine.confirmPassAndPlay(state) }
    }

    /** Clears the last error message. */
    fun clearError() {
        _error.value = null
    }

    // -------------------------------------------------------------------------
    // Computed helpers
    // -------------------------------------------------------------------------

    /** True if there is a valid placement for any domino in the current player's hand. */
    fun currentPlayerCanPlay(): Boolean {
        val state = _gameState.value ?: return false
        return gameEngine.canCurrentPlayerPlay(state)
    }
}

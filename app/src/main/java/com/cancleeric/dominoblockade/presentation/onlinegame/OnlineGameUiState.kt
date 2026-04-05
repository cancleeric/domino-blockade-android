package com.cancleeric.dominoblockade.presentation.onlinegame

import com.cancleeric.dominoblockade.domain.model.Domino
import com.cancleeric.dominoblockade.domain.model.GameState

data class OnlineGameUiState(
    val gameState: GameState? = null,
    val selectedDomino: Domino? = null,
    val localPlayerIndex: Int = 0,
    val isMyTurn: Boolean = false,
    val isGameOver: Boolean = false,
    val winnerName: String? = null,
    val isBlocked: Boolean = false,
    val opponentName: String = "",
    val opponentTileCount: Int = 0,
    val isLoading: Boolean = true,
    val roomFinished: Boolean = false,
    /** True when the opponent has disconnected and the grace period countdown is active. */
    val opponentDisconnected: Boolean = false,
    /** Remaining seconds of the reconnection grace period (0 when not counting down). */
    val gracePeriodSeconds: Int = 0
)

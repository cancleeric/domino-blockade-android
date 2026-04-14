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
    val isRankedMatch: Boolean = false,
    val isLoading: Boolean = true,
    val roomFinished: Boolean = false,
    val reconnectionCountdown: Int? = null,
    val disconnectedOpponentName: String? = null
)

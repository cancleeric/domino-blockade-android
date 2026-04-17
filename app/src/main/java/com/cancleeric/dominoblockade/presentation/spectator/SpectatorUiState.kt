package com.cancleeric.dominoblockade.presentation.spectator

import com.cancleeric.dominoblockade.domain.model.GameState

data class SpectatorUiState(
    val gameState: GameState? = null,
    val spectatorCount: Int = 0,
    val spectatorNames: List<String> = emptyList(),
    val hostName: String = "",
    val guestName: String = "",
    val isLoading: Boolean = true,
    val roomFinished: Boolean = false,
    val error: String? = null
)

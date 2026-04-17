package com.cancleeric.dominoblockade.presentation.lobby

import com.cancleeric.dominoblockade.domain.model.OnlineRoomStatus

enum class MatchMode { CASUAL, RANKED }

data class NavigateToOnlineGame(
    val roomId: String,
    val localPlayerIndex: Int,
    val localPlayerId: String
)

data class NavigateToSpectator(
    val roomId: String,
    val spectatorId: String
)

data class LobbyUiState(
    val playerName: String = "",
    val roomCode: String = "",
    val mode: MatchMode = MatchMode.CASUAL,
    val isLoading: Boolean = false,
    val isQueueingRanked: Boolean = false,
    val error: String? = null,
    val createdRoomId: String? = null,
    val roomStatus: OnlineRoomStatus? = null,
    val navigateToGame: NavigateToOnlineGame? = null,
    val navigateToSpectator: NavigateToSpectator? = null,
    val spectators: Map<String, String> = emptyMap(),
    val allowSpectators: Boolean = true
)

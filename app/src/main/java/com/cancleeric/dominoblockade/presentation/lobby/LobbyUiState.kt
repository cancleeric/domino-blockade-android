package com.cancleeric.dominoblockade.presentation.lobby

import com.cancleeric.dominoblockade.domain.model.OnlineRoomStatus

data class NavigateToOnlineGame(
    val roomId: String,
    val localPlayerIndex: Int
)

data class LobbyUiState(
    val playerName: String = "",
    val roomCode: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val createdRoomId: String? = null,
    val roomStatus: OnlineRoomStatus? = null,
    val navigateToGame: NavigateToOnlineGame? = null
)

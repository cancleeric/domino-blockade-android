package com.cancleeric.dominoblockade.presentation.spectator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cancleeric.dominoblockade.domain.model.OnlineRoomStatus
import com.cancleeric.dominoblockade.domain.repository.OnlineGameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SpectatorViewModel @Inject constructor(
    private val onlineGameRepository: OnlineGameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SpectatorUiState())
    val uiState: StateFlow<SpectatorUiState> = _uiState.asStateFlow()

    private var roomId: String = ""
    private var spectatorId: String = ""

    fun setup(roomId: String, spectatorId: String) {
        if (this.roomId == roomId) return
        this.roomId = roomId
        this.spectatorId = spectatorId
        observeRoom()
    }

    fun leave() {
        viewModelScope.launch {
            runCatching { onlineGameRepository.leaveAsSpectator(roomId, spectatorId) }
        }
    }

    private fun observeRoom() {
        viewModelScope.launch {
            runCatching {
                onlineGameRepository.observeRoom(roomId).collect { room ->
                    if (room.status == OnlineRoomStatus.FINISHED) {
                        _uiState.value = _uiState.value.copy(roomFinished = true)
                        return@collect
                    }
                    _uiState.value = _uiState.value.copy(
                        gameState = room.gameState,
                        hostName = room.hostName,
                        guestName = room.guestName.orEmpty(),
                        spectatorCount = room.spectators.size,
                        spectatorNames = room.spectators.values.toList(),
                        isLoading = room.gameState == null
                    )
                }
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to connect to room"
                )
            }
        }
    }
}

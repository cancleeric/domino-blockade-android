package com.cancleeric.dominoblockade.presentation.lobby

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cancleeric.dominoblockade.domain.model.OnlineRoomStatus

private const val PADDING_DP = 16
private const val SPACING_DP = 12
private const val TITLE_SPACING_DP = 24

/**
 * Lobby screen for creating or joining an online multiplayer room.
 *
 * @param onNavigateToGame Called once both players are ready with the roomId, localPlayerIndex,
 *   and localPlayerId.
 * @param onNavigateToSpectator Called when the user joins as a spectator with the roomId and
 *   spectatorId.
 * @param onNavigateBack Called when the user presses the back button.
 */
@Composable
fun LobbyScreen(
    onNavigateToGame: (roomId: String, localPlayerIndex: Int, localPlayerId: String) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToSpectator: (roomId: String, spectatorId: String) -> Unit = { _, _ -> },
    initialRoomCode: String? = null,
    modifier: Modifier = Modifier,
    viewModel: LobbyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(initialRoomCode) {
        if (!initialRoomCode.isNullOrBlank()) {
            viewModel.setRoomCode(initialRoomCode)
        }
    }

    LaunchedEffect(uiState.navigateToGame) {
        uiState.navigateToGame?.let { nav ->
            viewModel.resetNavigation()
            onNavigateToGame(nav.roomId, nav.localPlayerIndex, nav.localPlayerId)
        }
    }

    LaunchedEffect(uiState.navigateToSpectator) {
        uiState.navigateToSpectator?.let { nav ->
            viewModel.resetSpectatorNavigation()
            onNavigateToSpectator(nav.roomId, nav.spectatorId)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(PADDING_DP.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SPACING_DP.dp)
    ) {
        Text(
            text = "Online Multiplayer",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = TITLE_SPACING_DP.dp)
        )
        OutlinedTextField(
            value = uiState.playerName,
            onValueChange = viewModel::setPlayerName,
            label = { Text("Your Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        ModeSelector(
            mode = uiState.mode,
            onModeChange = viewModel::setMatchMode
        )
        val errorMessage = uiState.error
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
        val createdRoomId = uiState.createdRoomId
        if (uiState.isLoading) {
            CircularProgressIndicator()
        } else if (uiState.isQueueingRanked) {
            RankedQueueSection(onCancelQueue = viewModel::cancelRankedQueue)
        } else if (createdRoomId != null) {
            WaitingSection(
                roomId = createdRoomId,
                status = uiState.roomStatus,
                spectators = uiState.spectators,
                allowSpectators = uiState.allowSpectators,
                onToggleSpectators = viewModel::toggleSpectatorPermission
            )
        } else {
            LobbyActions(
                mode = uiState.mode,
                roomCode = uiState.roomCode,
                onRoomCodeChange = viewModel::setRoomCode,
                onCreateRoom = viewModel::createRoom,
                onJoinRoom = viewModel::joinRoom,
                onJoinAsSpectator = viewModel::joinAsSpectator
            )
        }
        HorizontalDivider(modifier = Modifier.padding(top = SPACING_DP.dp))
        OutlinedButton(
            onClick = {
                if (uiState.isQueueingRanked) {
                    viewModel.cancelRankedQueue()
                }
                onNavigateBack()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Menu")
        }
    }
}

@Composable
private fun ModeSelector(mode: MatchMode, onModeChange: (MatchMode) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(SPACING_DP.dp)) {
        FilterChip(
            selected = mode == MatchMode.CASUAL,
            onClick = { onModeChange(MatchMode.CASUAL) },
            label = { Text("Casual") }
        )
        FilterChip(
            selected = mode == MatchMode.RANKED,
            onClick = { onModeChange(MatchMode.RANKED) },
            label = { Text("Ranked") }
        )
    }
}

@Composable
private fun RankedQueueSection(onCancelQueue: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SPACING_DP.dp)
    ) {
        CircularProgressIndicator()
        Text("Searching ranked opponent…", style = MaterialTheme.typography.bodyMedium)
        OutlinedButton(onClick = onCancelQueue) {
            Text("Cancel Queue")
        }
    }
}

@Composable
private fun WaitingSection(
    roomId: String,
    status: OnlineRoomStatus?,
    spectators: Map<String, String>,
    allowSpectators: Boolean,
    onToggleSpectators: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SPACING_DP.dp)
    ) {
        Text("Room Code:", style = MaterialTheme.typography.titleMedium)
        Text(text = roomId, style = MaterialTheme.typography.displaySmall)
        Text(
            text = if (status == OnlineRoomStatus.PLAYING) "Opponent joined! Starting\u2026"
            else "Waiting for opponent to join\u2026",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (status != OnlineRoomStatus.PLAYING) {
            CircularProgressIndicator()
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Allow spectators", style = MaterialTheme.typography.bodyMedium)
            Switch(checked = allowSpectators, onCheckedChange = { onToggleSpectators() })
        }
        if (spectators.isNotEmpty()) {
            Text(
                text = "Watching (${spectators.size}):",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.align(Alignment.Start)
            )
            spectators.values.forEach { name ->
                Text(
                    text = "\u2022 $name",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start)
                )
            }
        }
    }
}

@Composable
private fun LobbyActions(
    mode: MatchMode,
    roomCode: String,
    onRoomCodeChange: (String) -> Unit,
    onCreateRoom: () -> Unit,
    onJoinRoom: () -> Unit,
    onJoinAsSpectator: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(SPACING_DP.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        if (mode == MatchMode.RANKED) {
            Button(onClick = onCreateRoom, modifier = Modifier.fillMaxWidth()) {
                Text("Find Ranked Match")
            }
        } else {
            Button(onClick = onCreateRoom, modifier = Modifier.fillMaxWidth()) {
                Text("Create Room")
            }
            HorizontalDivider()
            Text("— or join an existing room —", style = MaterialTheme.typography.bodySmall)
            Row(
                horizontalArrangement = Arrangement.spacedBy(SPACING_DP.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = roomCode,
                    onValueChange = onRoomCodeChange,
                    label = { Text("Room Code") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = onJoinRoom) {
                    Text("Join")
                }
            }
            OutlinedButton(
                onClick = onJoinAsSpectator,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Join as Spectator")
            }
        }
    }
}

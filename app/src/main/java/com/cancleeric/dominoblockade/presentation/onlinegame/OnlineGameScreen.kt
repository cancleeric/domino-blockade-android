package com.cancleeric.dominoblockade.presentation.onlinegame

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cancleeric.dominoblockade.domain.model.Domino
import com.cancleeric.dominoblockade.domain.model.GameState
import com.cancleeric.dominoblockade.presentation.game.GameBoard
import com.cancleeric.dominoblockade.presentation.game.PlayerHand

private const val SCREEN_PADDING_DP = 16
private const val SECTION_SPACING_DP = 8

/**
 * Stateful online game screen backed by [OnlineGameViewModel].
 *
 * @param roomId The room code shared between players.
 * @param localPlayerIndex 0 for host, 1 for guest.
 * @param localPlayerId The local player's unique ID used for presence tracking.
 * @param onGameOver Callback invoked when the game ends.
 */
@Composable
fun OnlineGameScreen(
    roomId: String,
    localPlayerIndex: Int,
    onGameOver: (winnerName: String, isBlocked: Boolean) -> Unit,
    onOpponentLeft: () -> Unit,
    modifier: Modifier = Modifier,
    localPlayerId: String = "",
    viewModel: OnlineGameViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(roomId, localPlayerIndex) {
        viewModel.setup(roomId, localPlayerIndex, localPlayerId)
    }

    LaunchedEffect(uiState.isGameOver) {
        if (uiState.isGameOver) {
            onGameOver(uiState.winnerName.orEmpty(), uiState.isBlocked)
        }
    }

    LaunchedEffect(uiState.roomFinished) {
        if (uiState.roomFinished) onOpponentLeft()
    }

    OnlineGameContent(
        uiState = uiState,
        onSelectDomino = viewModel::selectDomino,
        onPlaceDomino = viewModel::placeDomino,
        onDrawDomino = viewModel::drawFromBoneyard,
        onLeave = {
            viewModel.leaveRoom()
            onOpponentLeft()
        },
        modifier = modifier
    )
}

/** Stateless online game content composable. */
@Composable
fun OnlineGameContent(
    uiState: OnlineGameUiState,
    onSelectDomino: (Domino) -> Unit,
    onPlaceDomino: () -> Unit,
    onDrawDomino: () -> Unit,
    onLeave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            return@Box
        }
        val gameState = uiState.gameState ?: return@Box
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(SCREEN_PADDING_DP.dp),
            verticalArrangement = Arrangement.spacedBy(SECTION_SPACING_DP.dp)
        ) {
            OnlineGameHeader(uiState = uiState, onLeave = onLeave)
            HorizontalDivider()
            GameBoard(board = gameState.board, modifier = Modifier.weight(1f))
            HorizontalDivider()
            OpponentSection(opponentName = uiState.opponentName, tileCount = uiState.opponentTileCount)
            HorizontalDivider()
            LocalPlayerSection(
                uiState = uiState,
                gameState = gameState,
                onSelectDomino = onSelectDomino,
                onPlaceDomino = onPlaceDomino,
                onDrawDomino = onDrawDomino
            )
        }

        if (uiState.reconnectionCountdown != null) {
            ReconnectionDialog(
                opponentName = uiState.disconnectedOpponentName.orEmpty(),
                countdown = uiState.reconnectionCountdown
            )
        }
    }
}

@Composable
private fun ReconnectionDialog(opponentName: String, countdown: Int) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("Opponent Disconnected") },
        text = {
            Text(
                text = if (opponentName.isNotEmpty()) {
                    "$opponentName disconnected — waiting $countdown seconds for reconnect\u2026"
                } else {
                    "Opponent disconnected — waiting $countdown seconds for reconnect\u2026"
                }
            )
        },
        confirmButton = {}
    )
}

@Composable
private fun OnlineGameHeader(uiState: OnlineGameUiState, onLeave: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val turnText = if (uiState.isMyTurn) "Your Turn" else "${uiState.opponentName}'s Turn"
        Text(text = turnText, style = MaterialTheme.typography.titleMedium)
        OutlinedButton(onClick = onLeave) {
            Text("Leave")
        }
    }
}

@Composable
private fun OpponentSection(opponentName: String, tileCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "$opponentName: $tileCount tiles", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun LocalPlayerSection(
    uiState: OnlineGameUiState,
    gameState: GameState,
    onSelectDomino: (Domino) -> Unit,
    onPlaceDomino: () -> Unit,
    onDrawDomino: () -> Unit
) {
    val localPlayer = gameState.players.getOrNull(uiState.localPlayerIndex) ?: return
    val hasValidMove = localPlayer.hand.any { gameState.canPlace(it) }
    Column(verticalArrangement = Arrangement.spacedBy(SECTION_SPACING_DP.dp)) {
        Text(
            text = "Your Hand (${localPlayer.hand.size} tiles)",
            style = MaterialTheme.typography.labelMedium
        )
        PlayerHand(
            hand = localPlayer.hand,
            selectedDomino = uiState.selectedDomino,
            canPlace = { gameState.canPlace(it) },
            onSelectDomino = if (uiState.isMyTurn) onSelectDomino else { _ -> }
        )
        if (uiState.isMyTurn) {
            OnlineActionButtons(
                hasValidMove = hasValidMove,
                selectedDomino = uiState.selectedDomino,
                boneyardEmpty = gameState.boneyard.isEmpty(),
                onPlaceDomino = onPlaceDomino,
                onDrawDomino = onDrawDomino
            )
        } else {
            Text(
                text = "Waiting for ${uiState.opponentName}\u2026",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun OnlineActionButtons(
    hasValidMove: Boolean,
    selectedDomino: Domino?,
    boneyardEmpty: Boolean,
    onPlaceDomino: () -> Unit,
    onDrawDomino: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SECTION_SPACING_DP.dp)
    ) {
        if (selectedDomino != null) {
            Button(onClick = onPlaceDomino, modifier = Modifier.weight(1f)) {
                Text("Place Tile")
            }
        }
        if (!hasValidMove) {
            OutlinedButton(onClick = onDrawDomino, modifier = Modifier.weight(1f)) {
                Text(if (boneyardEmpty) "Skip Turn" else "Draw Tile")
            }
        }
    }
}

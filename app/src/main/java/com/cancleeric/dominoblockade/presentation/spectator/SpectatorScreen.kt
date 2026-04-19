package com.cancleeric.dominoblockade.presentation.spectator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.cancleeric.dominoblockade.domain.model.GameState
import com.cancleeric.dominoblockade.presentation.game.GameBoard
import com.cancleeric.dominoblockade.presentation.game.PlayerHand

private const val SCREEN_PADDING_DP = 16
private const val SECTION_SPACING_DP = 8

/**
 * Stateful spectator screen backed by [SpectatorViewModel].
 *
 * @param roomId The room code of the game being watched.
 * @param spectatorId The unique ID assigned to this spectator when they joined.
 * @param onLeave Callback invoked when the spectator leaves or the room finishes.
 */
@Composable
fun SpectatorScreen(
    roomId: String,
    spectatorId: String,
    onLeave: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SpectatorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(roomId, spectatorId) {
        viewModel.setup(roomId, spectatorId)
    }

    LaunchedEffect(uiState.roomFinished) {
        if (uiState.roomFinished) onLeave()
    }

    SpectatorContent(
        uiState = uiState,
        onLeave = {
            viewModel.leave()
            onLeave()
        },
        modifier = modifier
    )
}

/** Stateless spectator content composable. */
@Composable
fun SpectatorContent(
    uiState: SpectatorUiState,
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
            SpectatorHeader(
                uiState = uiState,
                gameState = gameState,
                onLeave = onLeave
            )
            HorizontalDivider()
            GameBoard(board = gameState.board, modifier = Modifier.weight(1f))
            HorizontalDivider()
            SpectatorPlayerSection(
                playerIndex = 1,
                gameState = gameState,
                label = uiState.guestName
            )
            HorizontalDivider()
            SpectatorPlayerSection(
                playerIndex = 0,
                gameState = gameState,
                label = uiState.hostName
            )
        }
    }
}

@Composable
private fun SpectatorHeader(
    uiState: SpectatorUiState,
    gameState: GameState,
    onLeave: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val currentPlayer = gameState.players.getOrNull(gameState.currentPlayerIndex)
        val turnText = if (currentPlayer != null) "${currentPlayer.name}'s Turn" else "In Progress"
        Column {
            Text(text = turnText, style = MaterialTheme.typography.titleMedium)
            Text(
                text = "\uD83D\uDC41 ${uiState.spectatorCount} watching",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        OutlinedButton(onClick = onLeave) {
            Text("Leave")
        }
    }
}

@Composable
private fun SpectatorPlayerSection(
    playerIndex: Int,
    gameState: GameState,
    label: String
) {
    val player = gameState.players.getOrNull(playerIndex) ?: return
    val isCurrentPlayer = gameState.currentPlayerIndex == playerIndex
    Column(verticalArrangement = Arrangement.spacedBy(SECTION_SPACING_DP.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label.ifEmpty { player.name },
                style = MaterialTheme.typography.labelMedium,
                color = if (isCurrentPlayer) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${player.hand.size} tiles",
                style = MaterialTheme.typography.labelMedium
            )
        }
        PlayerHand(
            hand = player.hand,
            selectedDomino = null,
            canPlace = { false },
            onSelectDomino = {}
        )
    }
}

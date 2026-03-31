package com.cancleeric.dominoblockade.presentation.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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

private const val DEFAULT_PLAYER_COUNT = 2
private const val SCREEN_PADDING_DP = 16
private const val SECTION_SPACING_DP = 8

/**
 * Stateful game screen backed by [GameViewModel].
 *
 * @param playerCount Number of players in the game (2–4).
 * @param onGameOver Called with the winner's name and whether the game was blocked.
 */
@Composable
fun GameScreen(
    playerCount: Int = DEFAULT_PLAYER_COUNT,
    onGameOver: (winnerName: String, isBlocked: Boolean) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
    viewModel: GameViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(playerCount) {
        viewModel.startGame(playerCount)
    }

    LaunchedEffect(uiState.isGameOver) {
        if (uiState.isGameOver) {
            onGameOver(uiState.winnerName.orEmpty(), uiState.isBlocked)
        }
    }

    GameContent(
        uiState = uiState,
        onSelectDomino = viewModel::selectDomino,
        onPlaceDomino = viewModel::placeDomino,
        onDrawDomino = viewModel::drawFromBoneyard,
        modifier = modifier
    )
}

/**
 * Stateless game content composable — can be tested without Hilt.
 */
@Composable
fun GameContent(
    uiState: GameUiState,
    onSelectDomino: (Domino) -> Unit,
    onPlaceDomino: () -> Unit,
    onDrawDomino: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gameState = uiState.gameState
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(SCREEN_PADDING_DP.dp),
        verticalArrangement = Arrangement.spacedBy(SECTION_SPACING_DP.dp)
    ) {
        if (gameState != null) {
            GameHeader(gameState = gameState)
            HorizontalDivider()
            GameBoard(
                board = gameState.board,
                modifier = Modifier.weight(1f)
            )
            HorizontalDivider()
            PlayerSection(
                uiState = uiState,
                gameState = gameState,
                onSelectDomino = onSelectDomino,
                onPlaceDomino = onPlaceDomino,
                onDrawDomino = onDrawDomino
            )
        } else {
            LoadingContent()
        }
    }
}

@Composable
private fun GameHeader(gameState: GameState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${gameState.currentPlayer.name}'s Turn",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "Boneyard: ${gameState.boneyard.size}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PlayerSection(
    uiState: GameUiState,
    gameState: GameState,
    onSelectDomino: (Domino) -> Unit,
    onPlaceDomino: () -> Unit,
    onDrawDomino: () -> Unit
) {
    val hasValidMove = gameState.currentPlayer.hand.any { gameState.canPlace(it) }
    Column(verticalArrangement = Arrangement.spacedBy(SECTION_SPACING_DP.dp)) {
        Text(
            text = "Hand (${gameState.currentPlayer.hand.size} tiles)",
            style = MaterialTheme.typography.labelMedium
        )
        PlayerHand(
            hand = gameState.currentPlayer.hand,
            selectedDomino = uiState.selectedDomino,
            canPlace = { gameState.canPlace(it) },
            onSelectDomino = onSelectDomino
        )
        ActionButtons(
            hasValidMove = hasValidMove,
            selectedDomino = uiState.selectedDomino,
            boneyardEmpty = gameState.boneyard.isEmpty(),
            onPlaceDomino = onPlaceDomino,
            onDrawDomino = onDrawDomino
        )
    }
}

@Composable
private fun ActionButtons(
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
            Button(
                onClick = onPlaceDomino,
                modifier = Modifier.weight(1f)
            ) {
                Text("Place Tile")
            }
        }
        if (!hasValidMove) {
            OutlinedButton(
                onClick = onDrawDomino,
                modifier = Modifier.weight(1f)
            ) {
                Text(if (boneyardEmpty) "Skip Turn" else "Draw Tile")
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Text(
        text = "Starting game\u2026",
        style = MaterialTheme.typography.bodyLarge
    )
}

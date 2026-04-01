package com.cancleeric.dominoblockade.presentation.game

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cancleeric.dominoblockade.domain.model.Domino
import com.cancleeric.dominoblockade.domain.model.GameState
import com.cancleeric.dominoblockade.presentation.audio.SoundManager
import com.cancleeric.dominoblockade.presentation.haptic.HapticFeedbackManager

private const val DEFAULT_PLAYER_COUNT = 2
private const val SCREEN_PADDING_DP = 16
private const val SECTION_SPACING_DP = 8
private const val FLASH_ALPHA = 0.35f
private const val FLASH_DURATION_MS = 500

/**
 * Stateful game screen backed by [GameViewModel].
 *
 * @param playerCount Number of players in the game (2–4).
 * @param onGameOver Called with the winner's name and whether the game was blocked.
 * @param soundManager Optional sound manager for audio feedback.
 * @param hapticManager Optional haptic manager for vibration feedback.
 */
@Composable
fun GameScreen(
    playerCount: Int = DEFAULT_PLAYER_COUNT,
    onGameOver: (winnerName: String, isBlocked: Boolean) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
    soundManager: SoundManager? = null,
    hapticManager: HapticFeedbackManager? = null,
    viewModel: GameViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(playerCount) {
        viewModel.startGame(playerCount)
    }

    LaunchedEffect(uiState.isGameOver, uiState.isBlocked) {
        when {
            uiState.isBlocked -> {
                soundManager?.playBlocked()
                hapticManager?.strongFeedback()
            }
            uiState.isGameOver -> {
                soundManager?.playWin()
                hapticManager?.victoryFeedback()
            }
        }
        if (uiState.isGameOver) {
            onGameOver(uiState.winnerName.orEmpty(), uiState.isBlocked)
        }
    }

    LaunchedEffect(uiState.gameState?.board?.size) {
        val boardSize = uiState.gameState?.board?.size ?: 0
        if (boardSize > 0) {
            soundManager?.playPlaceDomino()
            hapticManager?.lightFeedback()
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
 *
 * When [uiState] indicates a blocked game, a red flash overlay is shown.
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
    val infiniteTransition = rememberInfiniteTransition(label = "blockedFlash")
    val flashAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (uiState.isBlocked) FLASH_ALPHA else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = FLASH_DURATION_MS),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flashAlpha"
    )
    val headerColor by animateColorAsState(
        targetValue = if (uiState.isBlocked) MaterialTheme.colorScheme.error
        else MaterialTheme.colorScheme.onBackground,
        label = "headerColor"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(SCREEN_PADDING_DP.dp),
            verticalArrangement = Arrangement.spacedBy(SECTION_SPACING_DP.dp)
        ) {
            if (gameState != null) {
                GameHeader(gameState = gameState, playerNameColor = headerColor)
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
        if (uiState.isBlocked) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Red.copy(alpha = flashAlpha))
            )
        }
    }
}

@Composable
private fun GameHeader(gameState: GameState, playerNameColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${gameState.currentPlayer.name}'s Turn",
            style = MaterialTheme.typography.titleMedium,
            color = playerNameColor
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
    val hasValidMove = remember(gameState) {
        gameState.currentPlayer.hand.any { gameState.canPlace(it) }
    }
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

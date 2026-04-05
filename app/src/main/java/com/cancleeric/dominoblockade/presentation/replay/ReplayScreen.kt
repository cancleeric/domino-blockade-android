package com.cancleeric.dominoblockade.presentation.replay

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cancleeric.dominoblockade.domain.model.Domino
import com.cancleeric.dominoblockade.domain.model.Player

private const val TILE_SIZE_DP = 48
private const val TILE_PIP_SIZE_DP = 20
private const val BOARD_PADDING_DP = 8
private const val SECTION_PADDING_DP = 16
private const val HIGHLIGHT_BORDER_DP = 3

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReplayScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReplayViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Game Replay") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        ReplayContent(
            uiState = uiState,
            onStepBack = viewModel::stepBackward,
            onStepForward = viewModel::stepForward,
            onTogglePlay = viewModel::togglePlay,
            onSeek = viewModel::seekTo,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun ReplayContent(
    uiState: ReplayUiState,
    onStepBack: () -> Unit,
    onStepForward: () -> Unit,
    onTogglePlay: () -> Unit,
    onSeek: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        uiState.isLoading -> LoadingContent(modifier)
        !uiState.hasReplayData -> NoReplayDataContent(modifier)
        else -> ReplayMainContent(uiState, onStepBack, onStepForward, onTogglePlay, onSeek, modifier)
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun NoReplayDataContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "No replay data available for this game.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(SECTION_PADDING_DP.dp)
        )
    }
}

@Composable
private fun ReplayMainContent(
    uiState: ReplayUiState,
    onStepBack: () -> Unit,
    onStepForward: () -> Unit,
    onTogglePlay: () -> Unit,
    onSeek: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(SECTION_PADDING_DP.dp),
        verticalArrangement = Arrangement.spacedBy(BOARD_PADDING_DP.dp)
    ) {
        StepIndicator(uiState)
        ReplayBoardSection(uiState)
        ReplayControls(
            uiState = uiState,
            onStepBack = onStepBack,
            onStepForward = onStepForward,
            onTogglePlay = onTogglePlay,
            onSeek = onSeek
        )
        ReplayPlayersSection(uiState)
    }
}

@Composable
private fun StepIndicator(uiState: ReplayUiState) {
    val move = uiState.currentMove
    val label = when {
        uiState.currentStep == 0 -> "Initial deal — no moves yet"
        move != null -> "Move ${uiState.currentStep}: ${move.playerName} placed [${move.dominoLeft}|${move.dominoRight}]"
        else -> "Step ${uiState.currentStep}"
    }
    Text(
        text = label,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ReplayBoardSection(uiState: ReplayUiState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(BOARD_PADDING_DP.dp)) {
            Text(
                text = "Board",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(BOARD_PADDING_DP.dp))
            if (uiState.currentBoard.isEmpty()) {
                Text(
                    text = "Empty board",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    uiState.currentBoard.forEachIndexed { idx, domino ->
                        val isLastPlaced = uiState.currentStep > 0 &&
                            idx == uiState.currentBoard.lastIndex &&
                            uiState.currentMove?.boardEnd != "LEFT"
                        val isFirstPlaced = uiState.currentStep > 0 &&
                            idx == 0 && uiState.currentMove?.boardEnd == "LEFT"
                        DominoTileView(
                            domino = domino,
                            highlight = isLastPlaced || isFirstPlaced
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReplayControls(
    uiState: ReplayUiState,
    onStepBack: () -> Unit,
    onStepForward: () -> Unit,
    onTogglePlay: () -> Unit,
    onSeek: (Int) -> Unit
) {
    Column {
        Slider(
            value = uiState.currentStep.toFloat(),
            onValueChange = { onSeek(it.toInt()) },
            valueRange = 0f..uiState.totalSteps.toFloat(),
            steps = if (uiState.totalSteps > 1) uiState.totalSteps - 1 else 0,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Step ${uiState.currentStep} / ${uiState.totalSteps}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onStepBack, enabled = uiState.currentStep > 0) {
                Icon(Icons.Filled.SkipPrevious, contentDescription = "Previous step")
            }
            Spacer(Modifier.width(BOARD_PADDING_DP.dp))
            IconButton(
                onClick = onTogglePlay,
                enabled = uiState.currentStep < uiState.totalSteps || uiState.isPlaying
            ) {
                Icon(
                    imageVector = if (uiState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (uiState.isPlaying) "Pause" else "Play"
                )
            }
            Spacer(Modifier.width(BOARD_PADDING_DP.dp))
            IconButton(onClick = onStepForward, enabled = uiState.currentStep < uiState.totalSteps) {
                Icon(Icons.Filled.SkipNext, contentDescription = "Next step")
            }
        }
    }
}

@Composable
private fun ReplayPlayersSection(uiState: ReplayUiState) {
    val currentMovePlayerId = uiState.currentMove?.playerId
    Column(verticalArrangement = Arrangement.spacedBy(BOARD_PADDING_DP.dp)) {
        uiState.currentPlayers.forEach { player ->
            PlayerHandCard(
                player = player,
                isCurrentTurn = player.id == currentMovePlayerId
            )
        }
    }
}

@Composable
private fun PlayerHandCard(player: Player, isCurrentTurn: Boolean) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(BOARD_PADDING_DP.dp)) {
            Text(
                text = "${player.name} (${player.hand.size} tiles)${if (isCurrentTurn) " ← played" else ""}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isCurrentTurn) FontWeight.Bold else FontWeight.Normal,
                color = if (isCurrentTurn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                player.hand.forEach { domino ->
                    DominoTileView(domino = domino, highlight = false)
                }
            }
        }
    }
}

@Composable
private fun DominoTileView(domino: Domino, highlight: Boolean) {
    val borderColor = if (highlight) MaterialTheme.colorScheme.primary else Color.Transparent
    val borderWidth = if (highlight) HIGHLIGHT_BORDER_DP.dp else 0.dp
    Row(
        modifier = Modifier
            .size(TILE_SIZE_DP.dp, TILE_PIP_SIZE_DP.dp + 4.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small)
            .border(borderWidth, borderColor, MaterialTheme.shapes.small),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${domino.left}",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
        Text(text = "|", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        Text(
            text = "${domino.right}",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

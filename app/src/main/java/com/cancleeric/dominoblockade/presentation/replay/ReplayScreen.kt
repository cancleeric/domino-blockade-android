package com.cancleeric.dominoblockade.presentation.replay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cancleeric.dominoblockade.domain.model.ReplayStep
import com.cancleeric.dominoblockade.presentation.game.GameBoard

private const val SCREEN_PADDING_DP = 16
private const val CARD_PADDING_DP = 12
private const val SECTION_SPACING_DP = 8
private const val BUTTON_WEIGHT = 1f

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
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                ReplayLoadingContent(modifier = Modifier.padding(innerPadding))
            }
            uiState.steps.isEmpty() -> {
                EmptyReplayContent(modifier = Modifier.padding(innerPadding))
            }
            else -> {
                ReplayContent(
                    uiState = uiState,
                    onPrevious = viewModel::previousStep,
                    onNext = viewModel::nextStep,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun ReplayContent(
    uiState: ReplayViewModel.UiState,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val step = uiState.currentStep ?: return

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(SCREEN_PADDING_DP.dp),
        verticalArrangement = Arrangement.spacedBy(SECTION_SPACING_DP.dp)
    ) {
        ReplayProgressBar(uiState = uiState)
        GameSummaryCard(uiState = uiState)
        ReplayMoveInfo(step = step)
        GameBoard(
            board = step.board,
            modifier = Modifier.weight(BUTTON_WEIGHT)
        )
        ReplayControls(
            uiState = uiState,
            onPrevious = onPrevious,
            onNext = onNext
        )
    }
}

@Composable
private fun ReplayProgressBar(uiState: ReplayViewModel.UiState) {
    Column(verticalArrangement = Arrangement.spacedBy(SECTION_SPACING_DP.dp / 2)) {
        val progress = if (uiState.totalSteps > 1) {
            uiState.currentIndex.toFloat() / (uiState.totalSteps - 1).toFloat()
        } else {
            1f
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Move ${uiState.currentIndex + 1} of ${uiState.totalSteps}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun GameSummaryCard(uiState: ReplayViewModel.UiState) {
    val summaryText = when {
        uiState.isBlocked -> "Result: Game blocked (no moves possible)"
        uiState.winnerName.isNotEmpty() -> "Result: ${uiState.winnerName} won!"
        else -> "Game in progress"
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = summaryText,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(CARD_PADDING_DP.dp)
        )
    }
}

@Composable
private fun ReplayMoveInfo(step: ReplayStep) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(CARD_PADDING_DP.dp),
            verticalArrangement = Arrangement.spacedBy(SECTION_SPACING_DP.dp / 2)
        ) {
            Text(
                text = step.moveDescription,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Board: ${step.board.size} tile(s) placed  •  Boneyard: ${step.boneyardSize}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ReplayControls(
    uiState: ReplayViewModel.UiState,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SECTION_SPACING_DP.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onPrevious,
            enabled = uiState.canGoBack,
            modifier = Modifier.weight(BUTTON_WEIGHT)
        ) {
            Text("← Previous")
        }
        Button(
            onClick = onNext,
            enabled = uiState.canGoForward,
            modifier = Modifier.weight(BUTTON_WEIGHT)
        ) {
            Text("Next →")
        }
    }
}

@Composable
private fun EmptyReplayContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No replay available",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Complete a game to record a replay.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = SECTION_SPACING_DP.dp)
        )
    }
}

@Composable
private fun ReplayLoadingContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Loading replay\u2026",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

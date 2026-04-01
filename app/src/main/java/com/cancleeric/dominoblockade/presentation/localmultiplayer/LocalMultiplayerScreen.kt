package com.cancleeric.dominoblockade.presentation.localmultiplayer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cancleeric.dominoblockade.presentation.game.GameContent
import com.cancleeric.dominoblockade.presentation.game.GameUiState

private const val MIN_PLAYERS = 2
private const val MAX_PLAYERS = 4
private const val PADDING_DP = 16
private const val SPACING_DP = 8

@Composable
fun LocalMultiplayerScreen(
    onGameOver: (winnerName: String, isBlocked: Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LocalMultiplayerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isGameOver) {
        if (uiState.isGameOver) {
            onGameOver(uiState.winnerName.orEmpty(), uiState.isBlocked)
        }
    }

    when {
        uiState.gameState == null -> SetupContent(
            uiState = uiState,
            onPlayerCountChange = viewModel::updatePlayerCount,
            onPlayerNameChange = viewModel::updatePlayerName,
            onStartGame = viewModel::startGame,
            onNavigateBack = onNavigateBack,
            modifier = modifier
        )
        uiState.isPassingDevice -> PassDeviceContent(
            playerName = uiState.gameState?.currentPlayer?.name.orEmpty(),
            onReady = viewModel::confirmDevicePassed,
            modifier = modifier
        )
        else -> GameContent(
            uiState = GameUiState(
                gameState = uiState.gameState,
                selectedDomino = uiState.selectedDomino,
                isGameOver = uiState.isGameOver,
                winnerName = uiState.winnerName,
                isBlocked = uiState.isBlocked
            ),
            onSelectDomino = viewModel::selectDomino,
            onPlaceDomino = viewModel::placeDomino,
            onDrawDomino = viewModel::drawFromBoneyard,
            modifier = modifier
        )
    }
}

@Composable
private fun SetupContent(
    uiState: LocalMultiplayerUiState,
    onPlayerCountChange: (Int) -> Unit,
    onPlayerNameChange: (Int, String) -> Unit,
    onStartGame: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(PADDING_DP.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SPACING_DP.dp)
    ) {
        Text(
            text = "Local Multiplayer",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = PADDING_DP.dp)
        )
        Text(text = "Number of Players", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(SPACING_DP.dp)) {
            (MIN_PLAYERS..MAX_PLAYERS).forEach { count ->
                FilterChip(
                    selected = uiState.playerCount == count,
                    onClick = { onPlayerCountChange(count) },
                    label = { Text("$count") }
                )
            }
        }
        Text(
            text = "Player Names",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = SPACING_DP.dp)
        )
        PlayerNameFields(
            playerNames = uiState.playerNames,
            playerCount = uiState.playerCount,
            onNameChange = onPlayerNameChange
        )
        Button(
            onClick = onStartGame,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = PADDING_DP.dp)
        ) {
            Text("Start Game")
        }
        OutlinedButton(
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}

@Composable
private fun PlayerNameFields(
    playerNames: List<String>,
    playerCount: Int,
    onNameChange: (Int, String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SPACING_DP.dp)
    ) {
        (0 until playerCount).forEach { index ->
            OutlinedTextField(
                value = playerNames.getOrElse(index) { "Player ${index + 1}" },
                onValueChange = { onNameChange(index, it) },
                label = { Text("Player ${index + 1}") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}

@Composable
private fun PassDeviceContent(
    playerName: String,
    onReady: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(PADDING_DP.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Pass the device to",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = playerName,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = SPACING_DP.dp)
        )
        Spacer(modifier = Modifier.height(PADDING_DP.dp))
        Button(onClick = onReady) {
            Text("I'm Ready")
        }
    }
}

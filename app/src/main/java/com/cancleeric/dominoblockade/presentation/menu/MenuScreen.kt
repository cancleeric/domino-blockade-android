package com.cancleeric.dominoblockade.presentation.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private const val MIN_PLAYERS = 2
private const val MAX_PLAYERS = 4
private const val TITLE_PADDING_DP = 32
private const val SECTION_PADDING_DP = 16
private const val CHIP_SPACING_DP = 8

@Composable
fun MenuScreen(
    onStartGame: (playerCount: Int) -> Unit,
    onLeaderboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPlayerCount by rememberSaveable { mutableIntStateOf(MIN_PLAYERS) }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Domino Blockade",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = TITLE_PADDING_DP.dp)
        )
        Text(
            text = "Number of Players",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = CHIP_SPACING_DP.dp)
        )
        PlayerCountSelector(
            selectedCount = selectedPlayerCount,
            onCountSelected = { selectedPlayerCount = it }
        )
        Button(
            onClick = { onStartGame(selectedPlayerCount) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SECTION_PADDING_DP.dp)
                .padding(top = TITLE_PADDING_DP.dp)
        ) {
            Text(text = "Start Game")
        }
        Button(
            onClick = onLeaderboard,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SECTION_PADDING_DP.dp)
                .padding(top = CHIP_SPACING_DP.dp)
        ) {
            Text(text = "Leaderboard")
        }
    }
}

@Composable
private fun PlayerCountSelector(
    selectedCount: Int,
    onCountSelected: (Int) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(CHIP_SPACING_DP.dp)) {
        (MIN_PLAYERS..MAX_PLAYERS).forEach { count ->
            FilterChip(
                selected = selectedCount == count,
                onClick = { onCountSelected(count) },
                label = { Text("$count") }
            )
        }
    }
}

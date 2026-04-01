package com.cancleeric.dominoblockade.presentation.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cancleeric.dominoblockade.data.local.entity.PlayerStatsEntity

private const val CARD_PADDING_DP = 8
private const val SCREEN_PADDING_DP = 16

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val playerStats by viewModel.playerStats.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Player Statistics") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (playerStats.isEmpty()) {
            EmptyStatsContent(modifier = Modifier.padding(innerPadding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = SCREEN_PADDING_DP.dp),
                verticalArrangement = Arrangement.spacedBy(CARD_PADDING_DP.dp)
            ) {
                items(playerStats) { stats ->
                    PlayerStatsCard(stats = stats)
                }
            }
        }
    }
}

@Composable
private fun EmptyStatsContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No statistics yet",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PlayerStatsCard(stats: PlayerStatsEntity) {
    val winRate = remember(stats) {
        if (stats.totalGames > 0) (stats.wins * 100) / stats.totalGames else 0
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(SCREEN_PADDING_DP.dp),
            verticalArrangement = Arrangement.spacedBy(CARD_PADDING_DP.dp / 2)
        ) {
            Text(text = stats.playerName, style = MaterialTheme.typography.titleMedium)
            HorizontalDivider()
            StatRow(label = "Total Games", value = "${stats.totalGames}")
            StatRow(label = "Wins", value = "${stats.wins}")
            StatRow(label = "Losses", value = "${stats.losses}")
            StatRow(label = "Win Rate", value = "$winRate%")
            StatRow(label = "Highest Score", value = "${stats.highestScore}")
            StatRow(label = "Blocked Wins", value = "${stats.blockedWins}")
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

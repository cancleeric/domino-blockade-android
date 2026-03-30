package com.cancleeric.dominoblockade.presentation.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cancleeric.dominoblockade.R
import com.cancleeric.dominoblockade.domain.model.DifficultyStats
import com.cancleeric.dominoblockade.domain.model.GameStats

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val stats by viewModel.stats.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.stats_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        if (stats.totalGames == 0) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.stats_empty_title),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.stats_empty_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            StatsContent(
                stats = stats,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun StatsContent(stats: GameStats, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OverallStatsCard(stats = stats)
        DifficultyStatsCard(
            title = stringResource(R.string.stats_vs_easy),
            diffStats = stats.easyStats
        )
        DifficultyStatsCard(
            title = stringResource(R.string.stats_vs_medium),
            diffStats = stats.mediumStats
        )
        DifficultyStatsCard(
            title = stringResource(R.string.stats_vs_hard),
            diffStats = stats.hardStats
        )
    }
}

@Composable
private fun OverallStatsCard(stats: GameStats, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.stats_overall),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            StatRow(
                label = stringResource(R.string.stats_total_games),
                value = stats.totalGames.toString()
            )
            StatRow(
                label = stringResource(R.string.stats_total_wins),
                value = stats.totalWins.toString()
            )
            StatRow(
                label = stringResource(R.string.stats_win_rate),
                value = "%.1f%%".format(stats.winRate * 100)
            )
            StatRow(
                label = stringResource(R.string.stats_best_score),
                value = stats.bestScore.toString()
            )
            StatRow(
                label = stringResource(R.string.stats_consecutive_wins),
                value = stats.consecutiveWins.toString()
            )
        }
    }
}

@Composable
private fun DifficultyStatsCard(
    title: String,
    diffStats: DifficultyStats,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (diffStats.totalGames == 0) {
                Text(
                    text = stringResource(R.string.stats_no_games_at_difficulty),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                StatRow(
                    label = stringResource(R.string.stats_total_games),
                    value = diffStats.totalGames.toString()
                )
                StatRow(
                    label = stringResource(R.string.stats_win_rate),
                    value = "%.1f%%".format(diffStats.winRate * 100)
                )
                StatRow(
                    label = stringResource(R.string.stats_best_score),
                    value = diffStats.bestScore.toString()
                )
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

package com.cancleeric.dominoblockade.presentation.quest

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cancleeric.dominoblockade.domain.model.QuestTask
import kotlinx.coroutines.flow.collectLatest

private const val CONTENT_PADDING_DP = 16
private const val ITEM_SPACING_DP = 12

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestScreen(
    onBack: () -> Unit,
    onMessage: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: QuestViewModel = hiltViewModel()
) {
    val dashboard = viewModel.dashboard.collectAsStateWithLifecycle().value

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest(onMessage)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Challenges & Quests") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(CONTENT_PADDING_DP.dp),
            verticalArrangement = Arrangement.spacedBy(ITEM_SPACING_DP.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(CONTENT_PADDING_DP.dp)) {
                        Text("Level ${dashboard.profile.level}", style = MaterialTheme.typography.titleLarge)
                        Text("XP: ${dashboard.profile.totalXp}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            item { SectionTitle("Daily Challenges") }
            items(dashboard.dailyChallenges) { task ->
                TaskCard(task = task, onClaim = { viewModel.claimReward(task.id) })
            }
            item { SectionTitle("Short-term Quests") }
            items(dashboard.shortTermQuests) { task ->
                TaskCard(task = task, onClaim = { viewModel.claimReward(task.id) })
            }
            item { SectionTitle("Long-term Quests") }
            items(dashboard.longTermQuests) { task ->
                TaskCard(task = task, onClaim = { viewModel.claimReward(task.id) })
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleMedium)
}

@Composable
private fun TaskCard(task: QuestTask, onClaim: () -> Unit) {
    val normalizedProgress = if (task.target <= 0) 0f else task.progress.toFloat() / task.target.toFloat()
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(CONTENT_PADDING_DP.dp),
            verticalArrangement = Arrangement.spacedBy(ITEM_SPACING_DP.dp)
        ) {
            Text(task.title, style = MaterialTheme.typography.titleMedium)
            Text(task.description, style = MaterialTheme.typography.bodyMedium)
            LinearProgressIndicator(progress = { normalizedProgress.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth())
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${task.progress}/${task.target}")
                Text("Rewards: ${task.rewardCoins}🪙 + ${task.rewardXp} XP")
            }
            if (task.isCompleted && !task.isClaimed) {
                Button(onClick = onClaim) { Text("Claim Reward") }
            } else {
                Text(
                    text = if (task.isClaimed) "Claimed" else "In Progress",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

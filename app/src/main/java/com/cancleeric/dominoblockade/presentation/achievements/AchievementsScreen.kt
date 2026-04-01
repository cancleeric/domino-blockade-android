package com.cancleeric.dominoblockade.presentation.achievements

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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.cancleeric.dominoblockade.domain.model.Achievement

private const val CARD_PADDING_DP = 8
private const val SCREEN_PADDING_DP = 16
private const val BADGE_PADDING_END_DP = 12

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AchievementsViewModel = hiltViewModel()
) {
    val achievements by viewModel.achievements.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Achievements") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        AchievementsList(
            achievements = achievements,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun AchievementsList(achievements: List<Achievement>, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = SCREEN_PADDING_DP.dp),
        verticalArrangement = Arrangement.spacedBy(CARD_PADDING_DP.dp)
    ) {
        items(achievements) { achievement ->
            AchievementItem(achievement = achievement)
        }
    }
}

@Composable
private fun AchievementItem(achievement: Achievement) {
    val unlocked = achievement.isUnlocked
    val containerColor = if (unlocked) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier.padding(SCREEN_PADDING_DP.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (unlocked) achievement.type.badge else "🔒",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(end = BADGE_PADDING_END_DP.dp)
            )
            Column {
                Text(text = achievement.type.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = achievement.type.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

package com.cancleeric.dominoblockade.presentation.profile

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cancleeric.dominoblockade.data.local.entity.PlayerStatsEntity
import com.cancleeric.dominoblockade.domain.model.PlayerProfile

private const val SCREEN_PADDING_DP = 16
private const val SECTION_SPACING_DP = 24
private const val CARD_PADDING_DP = 8
private const val AVATAR_SIZE_DP = 72
private const val AVATAR_GRID_COLUMNS = 4
private const val AVATAR_GRID_HEIGHT_DP = 240
private const val NAME_MAX_LENGTH = 20

private val avatarOptions = listOf(
    "🎮", "🏆", "🌟", "⭐", "🎯", "🎲", "🃏", "🧩",
    "🦁", "🐯", "🦊", "🐺", "🐻", "🦅", "🐉", "🤖"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerProfileScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlayerProfileViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Player Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        PlayerProfileContent(
            profile = profile,
            stats = stats,
            onSaveName = viewModel::saveName,
            onSelectAvatar = viewModel::saveAvatar,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun PlayerProfileContent(
    profile: PlayerProfile,
    stats: PlayerStatsEntity?,
    onSaveName: (String) -> Unit,
    onSelectAvatar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(SCREEN_PADDING_DP.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SECTION_SPACING_DP.dp)
    ) {
        AvatarDisplay(avatarEmoji = profile.avatarEmoji, playerName = profile.playerName)
        NameEditSection(currentName = profile.playerName, onSaveName = onSaveName)
        AvatarPickerSection(selectedAvatar = profile.avatarEmoji, onSelectAvatar = onSelectAvatar)
        StatsSection(stats = stats)
    }
}

@Composable
private fun AvatarDisplay(avatarEmoji: String, playerName: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(AVATAR_SIZE_DP.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = avatarEmoji, style = MaterialTheme.typography.displayMedium)
        }
        Text(text = playerName, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
private fun NameEditSection(currentName: String, onSaveName: (String) -> Unit) {
    var nameInput by rememberSaveable(currentName) { mutableStateOf(currentName) }
    val isNameValid = nameInput.isNotBlank() && nameInput.length <= NAME_MAX_LENGTH

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(SCREEN_PADDING_DP.dp),
            verticalArrangement = Arrangement.spacedBy(CARD_PADDING_DP.dp)
        ) {
            Text(text = "Edit Name", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = nameInput,
                onValueChange = { if (it.length <= NAME_MAX_LENGTH) nameInput = it },
                label = { Text("Player Name") },
                singleLine = true,
                isError = !isNameValid,
                supportingText = {
                    if (!isNameValid) Text("Name must be 1–$NAME_MAX_LENGTH characters")
                },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { if (isNameValid) onSaveName(nameInput) },
                enabled = isNameValid && nameInput.trim() != currentName,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Save")
            }
        }
    }
}

@Composable
private fun AvatarPickerSection(selectedAvatar: String, onSelectAvatar: (String) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(SCREEN_PADDING_DP.dp)) {
            Text(text = "Choose Avatar", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(CARD_PADDING_DP.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(AVATAR_GRID_COLUMNS),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AVATAR_GRID_HEIGHT_DP.dp),
                horizontalArrangement = Arrangement.spacedBy(CARD_PADDING_DP.dp),
                verticalArrangement = Arrangement.spacedBy(CARD_PADDING_DP.dp)
            ) {
                items(avatarOptions) { emoji ->
                    AvatarChip(
                        emoji = emoji,
                        isSelected = emoji == selectedAvatar,
                        onClick = { onSelectAvatar(emoji) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AvatarChip(emoji: String, isSelected: Boolean, onClick: () -> Unit) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val border = if (isSelected) {
        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    }
    OutlinedCard(
        onClick = onClick,
        colors = CardDefaults.outlinedCardColors(containerColor = containerColor),
        border = border
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CARD_PADDING_DP.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun StatsSection(stats: PlayerStatsEntity?) {
    val winRate = remember(stats) {
        if (stats != null && stats.totalGames > 0) (stats.wins * 100) / stats.totalGames else 0
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(SCREEN_PADDING_DP.dp),
            verticalArrangement = Arrangement.spacedBy(CARD_PADDING_DP.dp)
        ) {
            Text(text = "Statistics", style = MaterialTheme.typography.titleMedium)
            HorizontalDivider()
            if (stats == null) {
                Text(
                    text = "No games played yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                StatRow(label = "Total Games", value = "${stats.totalGames}")
                StatRow(label = "Wins", value = "${stats.wins}")
                StatRow(label = "Losses", value = "${stats.losses}")
                StatRow(label = "Win Rate", value = "$winRate%")
                StatRow(label = "Highest Score", value = "${stats.highestScore}")
                StatRow(label = "Blocked Wins", value = "${stats.blockedWins}")
            }
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

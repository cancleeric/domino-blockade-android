package com.cancleeric.dominoblockade.presentation.weeklyleaderboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cancleeric.dominoblockade.domain.model.WeeklyLeaderboardEntry

private const val RANK_GOLD = 1
private const val RANK_SILVER = 2
private const val RANK_BRONZE = 3
private const val RANK_BADGE_SIZE_DP = 32
private const val AVATAR_SIZE_DP = 40
private const val CARD_PADDING_DP = 12
private const val SPACER_DP = 12
private const val CONTENT_PADDING_DP = 16
private const val ITEM_SPACING_DP = 8
private const val AVATAR_INITIALS_MAX = 2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyLeaderboardScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WeeklyLeaderboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weekly Challenge") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        WeeklyLeaderboardContent(
            uiState = uiState,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
private fun WeeklyLeaderboardContent(
    uiState: WeeklyLeaderboardUiState,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                WeeklyLeaderboardList(
                    uiState = uiState,
                    modifier = Modifier.weight(1f)
                )
                StickyPlayerRow(uiState = uiState)
            }
        }
    }
}

@Composable
private fun WeeklyLeaderboardList(
    uiState: WeeklyLeaderboardUiState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ITEM_SPACING_DP.dp),
        contentPadding = PaddingValues(CONTENT_PADDING_DP.dp)
    ) {
        item {
            WeeklyInfoHeader(
                weekId = uiState.weekId,
                millisUntilReset = uiState.millisUntilReset,
                playerRank = uiState.playerRank
            )
        }
        if (uiState.entries.isEmpty()) {
            item {
                Text(
                    text = "No scores yet. Be the first!",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        itemsIndexed(uiState.entries) { index, entry ->
            val rank = index + 1
            val isCurrentUser = entry.userId == uiState.currentUser?.uid
            WeeklyEntryCard(rank = rank, entry = entry, isCurrentUser = isCurrentUser)
        }
    }
}

@Composable
private fun WeeklyInfoHeader(
    weekId: String,
    millisUntilReset: Long,
    playerRank: Int?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = ITEM_SPACING_DP.dp)
    ) {
        Text(
            text = "Week: $weekId",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Resets in: ${formatCountdown(millisUntilReset)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
        playerRank?.let {
            Text(
                text = "Your rank: #$it",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun StickyPlayerRow(uiState: WeeklyLeaderboardUiState) {
    val playerEntry = uiState.playerEntry ?: return
    val playerRank = uiState.playerRank ?: return
    val isOutsideTop = playerRank > uiState.entries.size || uiState.entries.none { it.userId == playerEntry.userId }
    if (!isOutsideTop) return

    Column {
        HorizontalDivider()
        WeeklyEntryCard(
            rank = playerRank,
            entry = playerEntry,
            isCurrentUser = true,
            modifier = Modifier.padding(horizontal = CONTENT_PADDING_DP.dp, vertical = ITEM_SPACING_DP.dp)
        )
    }
}

@Composable
private fun WeeklyEntryCard(
    rank: Int,
    entry: WeeklyLeaderboardEntry,
    isCurrentUser: Boolean,
    modifier: Modifier = Modifier
) {
    val cardColors = if (isCurrentUser) {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    } else {
        CardDefaults.cardColors()
    }

    Card(
        colors = cardColors,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(CARD_PADDING_DP.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RankBadge(rank = rank)
            Spacer(modifier = Modifier.width(SPACER_DP.dp))
            PlayerAvatar(displayName = entry.displayName.ifEmpty { "?" })
            Spacer(modifier = Modifier.width(SPACER_DP.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = entry.displayName.ifEmpty { "Anonymous" },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isCurrentUser) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (entry.hasBadge) {
                        Text(
                            text = "🏅",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
                Text(
                    text = "W: ${entry.wins}  L: ${entry.losses}  Streak: ${entry.currentStreak}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "${entry.score} pts",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun PlayerAvatar(displayName: String, modifier: Modifier = Modifier) {
    val initials = displayName
        .split(" ")
        .filter { it.isNotEmpty() }
        .take(AVATAR_INITIALS_MAX)
        .joinToString("") { word -> word.firstOrNull()?.uppercaseChar()?.toString() ?: "" }
        .ifEmpty { "?" }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(AVATAR_SIZE_DP.dp)
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = CircleShape
            )
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun RankBadge(rank: Int, modifier: Modifier = Modifier) {
    val color = when (rank) {
        RANK_GOLD -> Color(0xFFFFD700)
        RANK_SILVER -> Color(0xFFC0C0C0)
        RANK_BRONZE -> Color(0xFFCD7F32)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(RANK_BADGE_SIZE_DP.dp)
            .background(color = color, shape = CircleShape)
    ) {
        Text(
            text = "#$rank",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun formatCountdown(millis: Long): String {
    if (millis <= 0L) return "00:00:00"
    val totalSeconds = millis / 1_000L
    val days = totalSeconds / 86_400L
    val hours = (totalSeconds % 86_400L) / 3_600L
    val minutes = (totalSeconds % 3_600L) / 60L
    val seconds = totalSeconds % 60L
    return if (days > 0) {
        "${days}d ${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    } else {
        "${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    }
}

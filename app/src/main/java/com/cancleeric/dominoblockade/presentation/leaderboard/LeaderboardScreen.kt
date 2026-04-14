package com.cancleeric.dominoblockade.presentation.leaderboard

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import com.cancleeric.dominoblockade.domain.model.LeaderboardEntry
import com.cancleeric.dominoblockade.domain.repository.LeaderboardSegment

private const val RANK_GOLD = 1
private const val RANK_SILVER = 2
private const val RANK_BRONZE = 3
private const val RANK_BADGE_SIZE_DP = 32
private const val AVATAR_SIZE_DP = 40
private const val CARD_PADDING_DP = 12
private const val SPACER_LARGE_DP = 12
private const val SPACER_SMALL_DP = 4
private const val CONTENT_PADDING_DP = 16
private const val BUTTON_END_PADDING_DP = 8
private const val ITEM_SPACING_DP = 8
private const val RANK_LABEL_PADDING_DP = 8
private const val AVATAR_INITIALS_MAX = 2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LeaderboardViewModel = hiltViewModel()
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
                title = { Text("Leaderboard") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    AuthButton(
                        isSignedIn = uiState.currentUser != null,
                        isAnonymous = uiState.currentUser?.isAnonymous == true,
                        onSignIn = { viewModel.signInAnonymously() },
                        onSignOut = { viewModel.signOut() }
                    )
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        LeaderboardContent(
            uiState = uiState,
            onSegmentSelected = viewModel::selectSegment,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
private fun AuthButton(
    isSignedIn: Boolean,
    isAnonymous: Boolean,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit
) {
    if (!isSignedIn || isAnonymous) {
        Button(onClick = onSignIn, modifier = Modifier.padding(end = BUTTON_END_PADDING_DP.dp)) {
            Text(text = if (!isSignedIn) "Sign In" else "Link Account")
        }
    } else {
        Button(onClick = onSignOut, modifier = Modifier.padding(end = BUTTON_END_PADDING_DP.dp)) {
            Text("Sign Out")
        }
    }
}

@Composable
private fun LeaderboardContent(
    uiState: LeaderboardUiState,
    onSegmentSelected: (LeaderboardSegment) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            LeaderboardList(uiState = uiState, onSegmentSelected = onSegmentSelected)
        }
    }
}

@Composable
private fun LeaderboardList(uiState: LeaderboardUiState, onSegmentSelected: (LeaderboardSegment) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(ITEM_SPACING_DP.dp),
        contentPadding = PaddingValues(CONTENT_PADDING_DP.dp)
    ) {
        item {
            SegmentSelector(
                selected = uiState.selectedSegment,
                onSelected = onSegmentSelected
            )
        }
        uiState.playerRank?.let { rank ->
            item {
                Text(
                    text = "Your rank: #$rank",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = RANK_LABEL_PADDING_DP.dp)
                )
            }
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
            LeaderboardEntryCard(
                rank = rank,
                entry = entry,
                isCurrentUser = isCurrentUser
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
private fun LeaderboardEntryCard(
    rank: Int,
    entry: LeaderboardEntry,
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
            Spacer(modifier = Modifier.width(SPACER_LARGE_DP.dp))
            PlayerAvatar(displayName = entry.displayName.ifEmpty { "?" })
            Spacer(modifier = Modifier.width(SPACER_LARGE_DP.dp))
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
                }
                Text(
                    text = "W/L: ${entry.wins}/${entry.losses}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "${entry.elo}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
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

@Composable
private fun SegmentSelector(
    selected: LeaderboardSegment,
    onSelected: (LeaderboardSegment) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(SPACER_SMALL_DP.dp)) {
        FilterChip(
            selected = selected == LeaderboardSegment.CURRENT_SEASON,
            onClick = { onSelected(LeaderboardSegment.CURRENT_SEASON) },
            label = { Text("Current Season") }
        )
        FilterChip(
            selected = selected == LeaderboardSegment.ALL_TIME,
            onClick = { onSelected(LeaderboardSegment.ALL_TIME) },
            label = { Text("All Time") }
        )
        FilterChip(
            selected = selected == LeaderboardSegment.FRIENDS_ONLY,
            onClick = { onSelected(LeaderboardSegment.FRIENDS_ONLY) },
            label = { Text("Friends Only") }
        )
    }
}

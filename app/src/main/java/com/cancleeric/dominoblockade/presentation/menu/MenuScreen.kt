package com.cancleeric.dominoblockade.presentation.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cancleeric.dominoblockade.R
import com.cancleeric.dominoblockade.ui.theme.LocalWindowSizeClass
import com.cancleeric.dominoblockade.ui.theme.widthIsMediumOrExpanded

private const val MIN_PLAYERS = 2
private const val MAX_PLAYERS = 4
private const val TITLE_PADDING_DP = 32
private const val SECTION_PADDING_DP = 16
private const val CHIP_SPACING_DP = 8
private const val MENU_MAX_WIDTH_DP = 480

@Composable
fun MenuScreen(
    onStartGame: (playerCount: Int) -> Unit,
    onLeaderboard: () -> Unit,
    onLocalMultiplayer: () -> Unit = {},
    onThemeSettings: () -> Unit = {},
    onSettings: () -> Unit = {},
    onAchievements: () -> Unit = {},
    onProfile: () -> Unit = {},
    onOnlineMultiplayer: () -> Unit = {},
    onReplayLastGame: () -> Unit = {},
    onTournament: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedPlayerCount by rememberSaveable { mutableIntStateOf(MIN_PLAYERS) }
    val isMediumOrExpanded = LocalWindowSizeClass.current.widthIsMediumOrExpanded

    MenuContent(
        selectedPlayerCount = selectedPlayerCount,
        onCountSelected = { selectedPlayerCount = it },
        onStartGame = onStartGame,
        onLeaderboard = onLeaderboard,
        onLocalMultiplayer = onLocalMultiplayer,
        onThemeSettings = onThemeSettings,
        onSettings = onSettings,
        onAchievements = onAchievements,
        onProfile = onProfile,
        onOnlineMultiplayer = onOnlineMultiplayer,
        onReplayLastGame = onReplayLastGame,
        onTournament = onTournament,
        isMediumOrExpanded = isMediumOrExpanded,
        modifier = modifier.fillMaxSize()
    )
}

@Composable
private fun MenuContent(
    selectedPlayerCount: Int,
    onCountSelected: (Int) -> Unit,
    onStartGame: (playerCount: Int) -> Unit,
    onLeaderboard: () -> Unit,
    onLocalMultiplayer: () -> Unit,
    onThemeSettings: () -> Unit,
    onSettings: () -> Unit,
    onAchievements: () -> Unit,
    onProfile: () -> Unit,
    onOnlineMultiplayer: () -> Unit,
    onReplayLastGame: () -> Unit,
    onTournament: () -> Unit,
    isMediumOrExpanded: Boolean,
    modifier: Modifier = Modifier
) {
    val contentModifier = if (isMediumOrExpanded) {
        Modifier.widthIn(max = MENU_MAX_WIDTH_DP.dp)
    } else {
        Modifier.fillMaxWidth()
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Column(
            modifier = contentModifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.menu_title),
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = TITLE_PADDING_DP.dp)
            )
            Text(
                text = stringResource(R.string.menu_number_of_players),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = CHIP_SPACING_DP.dp)
            )
            PlayerCountSelector(
                selectedCount = selectedPlayerCount,
                onCountSelected = onCountSelected
            )
            Button(
                onClick = { onStartGame(selectedPlayerCount) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SECTION_PADDING_DP.dp)
                    .padding(top = TITLE_PADDING_DP.dp)
            ) {
                Text(text = stringResource(R.string.menu_start_game))
            }
            Button(
                onClick = onLocalMultiplayer,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SECTION_PADDING_DP.dp)
                    .padding(top = CHIP_SPACING_DP.dp)
            ) {
                Text(text = "Local Multiplayer")
            }
            Button(
                onClick = onLeaderboard,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SECTION_PADDING_DP.dp)
                    .padding(top = CHIP_SPACING_DP.dp)
            ) {
                Text(text = stringResource(R.string.menu_leaderboard))
            }
            Button(
                onClick = onThemeSettings,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SECTION_PADDING_DP.dp)
                    .padding(top = CHIP_SPACING_DP.dp)
            ) {
                Text(text = "Theme Settings")
            }
            Button(
                onClick = onSettings,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SECTION_PADDING_DP.dp)
                    .padding(top = CHIP_SPACING_DP.dp)
            ) {
                Text(text = "Settings")
            }
            Button(
                onClick = onAchievements,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SECTION_PADDING_DP.dp)
                    .padding(top = CHIP_SPACING_DP.dp)
            ) {
                Text(text = "Achievements")
            }
            Button(
                onClick = onProfile,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SECTION_PADDING_DP.dp)
                    .padding(top = CHIP_SPACING_DP.dp)
            ) {
                Text(text = "Profile")
            }
            Button(
                onClick = onOnlineMultiplayer,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SECTION_PADDING_DP.dp)
                    .padding(top = CHIP_SPACING_DP.dp)
            ) {
                Text(text = "Online Multiplayer")
            }
            Button(
                onClick = onReplayLastGame,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SECTION_PADDING_DP.dp)
                    .padding(top = CHIP_SPACING_DP.dp)
            ) {
                Text(text = "Replay Last Game")
            }
            Button(
                onClick = onTournament,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SECTION_PADDING_DP.dp)
                    .padding(top = CHIP_SPACING_DP.dp)
            ) {
                Text(text = "Tournament")
            }
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

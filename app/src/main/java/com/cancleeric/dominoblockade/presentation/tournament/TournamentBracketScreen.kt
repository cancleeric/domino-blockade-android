package com.cancleeric.dominoblockade.presentation.tournament

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cancleeric.dominoblockade.domain.model.Tournament
import com.cancleeric.dominoblockade.domain.model.TournamentMatch
import com.cancleeric.dominoblockade.domain.model.TournamentPlayer
import com.cancleeric.dominoblockade.domain.model.TournamentStatus

private const val MATCH_CARD_WIDTH_DP = 160
private const val CARD_PADDING_DP = 8
private const val SECTION_PADDING_DP = 16
private const val MATCH_SPACING_DP = 8
private const val CHAMPION_PADDING_DP = 12

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentBracketScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TournamentViewModel = hiltViewModel()
) {
    val tournament by viewModel.tournament.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadActiveTournament()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tournament Bracket") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        val t = tournament
        if (t != null) {
            BracketContent(
                tournament = t,
                onRecordWinner = { roundIndex, matchIndex, winnerId ->
                    viewModel.recordMatchWinner(t.id, roundIndex, matchIndex, winnerId)
                },
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("No active tournament")
            }
        }
    }
}

@Composable
private fun BracketContent(
    tournament: Tournament,
    onRecordWinner: (Int, Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        if (tournament.status == TournamentStatus.COMPLETED) {
            item {
                ChampionBanner(champion = tournament.champion)
            }
        }
        itemsIndexed(tournament.rounds) { roundIndex, round ->
            RoundRow(
                roundIndex = roundIndex,
                matches = round,
                onRecordWinner = { matchIndex, winnerId -> onRecordWinner(roundIndex, matchIndex, winnerId) }
            )
        }
    }
}

@Composable
private fun ChampionBanner(champion: TournamentPlayer?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(SECTION_PADDING_DP.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CHAMPION_PADDING_DP.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "🏆 Champion!", style = MaterialTheme.typography.headlineMedium)
            Text(text = champion?.playerName ?: "", style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun RoundRow(
    roundIndex: Int,
    matches: List<TournamentMatch>,
    onRecordWinner: (Int, String) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = CARD_PADDING_DP.dp)) {
        Text(
            text = "Round ${roundIndex + 1}",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = SECTION_PADDING_DP.dp, vertical = CARD_PADDING_DP.dp)
        )
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = SECTION_PADDING_DP.dp),
            horizontalArrangement = Arrangement.spacedBy(MATCH_SPACING_DP.dp)
        ) {
            matches.forEach { match ->
                MatchCard(
                    match = match,
                    onRecordWinner = { winnerId -> onRecordWinner(match.matchIndex, winnerId) }
                )
            }
        }
    }
}

@Composable
private fun MatchCard(match: TournamentMatch, onRecordWinner: (String) -> Unit) {
    Card(
        modifier = Modifier
            .width(MATCH_CARD_WIDTH_DP.dp)
            .padding(CARD_PADDING_DP.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(CARD_PADDING_DP.dp)) {
            PlayerRow(player = match.player1, isWinner = match.winnerId == match.player1?.playerId)
            Text(text = "vs", modifier = Modifier.align(Alignment.CenterHorizontally))
            PlayerRow(player = match.player2, isWinner = match.winnerId == match.player2?.playerId)
            if (match.player1 != null && match.player2 != null && match.winnerId == null) {
                WinnerButtons(match = match, onRecordWinner = onRecordWinner)
            }
        }
    }
}

@Composable
private fun PlayerRow(player: TournamentPlayer?, isWinner: Boolean) {
    val textColor = if (isWinner) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface
    Text(
        text = player?.playerName ?: "TBD",
        color = textColor,
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun WinnerButtons(match: TournamentMatch, onRecordWinner: (String) -> Unit) {
    Column(modifier = Modifier.padding(top = CARD_PADDING_DP.dp)) {
        match.player1?.let { p1 ->
            Button(
                onClick = { onRecordWinner(p1.playerId) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(p1.playerName, maxLines = 1)
            }
        }
        match.player2?.let { p2 ->
            Button(
                onClick = { onRecordWinner(p2.playerId) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(p2.playerName, maxLines = 1)
            }
        }
    }
}

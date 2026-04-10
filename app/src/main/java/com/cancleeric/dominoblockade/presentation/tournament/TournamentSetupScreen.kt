package com.cancleeric.dominoblockade.presentation.tournament

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

private const val PLAYER_COUNT_8 = 8
private const val PLAYER_COUNT_16 = 16
private const val SECTION_PADDING_DP = 16
private const val CHIP_SPACING_DP = 8
private const val FIELD_PADDING_DP = 4

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentSetupScreen(
    onBack: () -> Unit,
    onTournamentCreated: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TournamentViewModel = hiltViewModel()
) {
    var playerCount by rememberSaveable { mutableIntStateOf(PLAYER_COUNT_8) }
    var playerNames by rememberSaveable { mutableStateOf(List(PLAYER_COUNT_8) { "" }) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tournament Setup") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        TournamentSetupContent(
            playerCount = playerCount,
            playerNames = playerNames,
            onPlayerCountSelected = { count ->
                playerCount = count
                playerNames = List(count) { "" }
            },
            onPlayerNameChanged = { index, name ->
                playerNames = playerNames.toMutableList().also { it[index] = name }
            },
            onCreateTournament = {
                viewModel.createTournament(playerCount, playerNames)
                onTournamentCreated()
            },
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun TournamentSetupContent(
    playerCount: Int,
    playerNames: List<String>,
    onPlayerCountSelected: (Int) -> Unit,
    onPlayerNameChanged: (Int, String) -> Unit,
    onCreateTournament: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = SECTION_PADDING_DP.dp),
        verticalArrangement = Arrangement.spacedBy(FIELD_PADDING_DP.dp)
    ) {
        item {
            Text(
                text = "Number of Players",
                modifier = Modifier.padding(top = SECTION_PADDING_DP.dp)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(CHIP_SPACING_DP.dp),
                modifier = Modifier.padding(vertical = CHIP_SPACING_DP.dp)
            ) {
                FilterChip(
                    selected = playerCount == PLAYER_COUNT_8,
                    onClick = { onPlayerCountSelected(PLAYER_COUNT_8) },
                    label = { Text("8") }
                )
                FilterChip(
                    selected = playerCount == PLAYER_COUNT_16,
                    onClick = { onPlayerCountSelected(PLAYER_COUNT_16) },
                    label = { Text("16") }
                )
            }
        }
        itemsIndexed(playerNames) { index, name ->
            OutlinedTextField(
                value = name,
                onValueChange = { onPlayerNameChanged(index, it) },
                label = { Text("Player ${index + 1}") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Button(
                onClick = onCreateTournament,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = SECTION_PADDING_DP.dp),
                enabled = playerNames.all { it.isNotBlank() }
            ) {
                Text("Create Tournament")
            }
        }
    }
}

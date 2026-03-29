package com.cancleeric.dominoblockade.presentation.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cancleeric.dominoblockade.domain.model.GameConfig

/**
 * Main menu screen — lets users choose 2, 3, or 4 player mode
 * and enter player names before starting the game.
 */
@Composable
fun MenuScreen(
    onStartGame: (GameConfig) -> Unit
) {
    var playerCount by remember { mutableIntStateOf(2) }
    var playerNames by remember { mutableStateOf(List(4) { "Player ${it + 1}" }) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🀄 Domino Blockade",
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Local Multiplayer — Pass & Play",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            // Player count selector
            Text(
                text = "Number of Players",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(2, 3, 4).forEach { count ->
                    val selected = playerCount == count
                    Button(
                        onClick = { playerCount = count },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "$count",
                            style = if (selected) MaterialTheme.typography.titleMedium
                            else MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Player name inputs
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Player Names",
                        style = MaterialTheme.typography.titleSmall
                    )

                    repeat(playerCount) { index ->
                        OutlinedTextField(
                            value = playerNames[index],
                            onValueChange = { newName ->
                                playerNames = playerNames.toMutableList()
                                    .also { it[index] = newName }
                            },
                            label = { Text("Player ${index + 1}") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // Rules summary
            PlayerCountInfo(playerCount = playerCount)

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    val names = playerNames.take(playerCount)
                    onStartGame(GameConfig(playerCount, names))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "Start Game",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun PlayerCountInfo(playerCount: Int) {
    val handSize = if (playerCount == 2) 7 else 5
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            InfoItem(label = "Players", value = "$playerCount")
            Spacer(Modifier.width(8.dp))
            InfoItem(label = "Hand Size", value = "$handSize tiles")
        }
    }
}

@Composable
private fun InfoItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

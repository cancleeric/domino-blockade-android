package com.cancleeric.dominoblockade.presentation.menu

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cancleeric.dominoblockade.domain.model.AiDifficulty
import com.cancleeric.dominoblockade.ui.theme.DominoBlockadeTheme

@Composable
fun MenuScreen(
    onStartGame: (numPlayers: Int, difficulty: String) -> Unit
) {
    var numPlayers by remember { mutableIntStateOf(2) }
    var aiDifficulty by remember { mutableStateOf(AiDifficulty.EASY) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF1B5E20)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Domino Blockade",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "多米諾封鎖",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF81C784)
            )

            Spacer(modifier = Modifier.height(48.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2E7D32))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Players: $numPlayers",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        (2..4).forEach { n ->
                            FilterChip(
                                selected = numPlayers == n,
                                onClick = { numPlayers = n },
                                label = { Text("$n") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF4CAF50),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFF388E3C))

                    Text(
                        text = "AI Difficulty",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AiDifficulty.entries.forEach { diff ->
                            FilterChip(
                                selected = aiDifficulty == diff,
                                onClick = { aiDifficulty = diff },
                                label = { Text(diff.name) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF4CAF50),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onStartGame(numPlayers, aiDifficulty.name) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text(
                    text = "Start Game",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { /* TODO: Game history */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF81C784)
                )
            ) {
                Text("Game History (Coming Soon)")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MenuScreenPreview() {
    DominoBlockadeTheme {
        MenuScreen(onStartGame = { _, _ -> })
    }
}

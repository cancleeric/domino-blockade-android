package com.cancleeric.dominoblockade.presentation.result

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cancleeric.dominoblockade.ui.theme.DominoBlockadeTheme

@Composable
fun ResultScreen(
    winnerName: String,
    scoresEncoded: String,
    onPlayAgain: () -> Unit,
    onBackToMenu: () -> Unit
) {
    val playerScores = parseScores(scoresEncoded)

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
                text = "Game Over!",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$winnerName Wins!",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFFFFD54F),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2E7D32))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Final Scores",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    playerScores.forEachIndexed { index, (name, score) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (name == winnerName) ">> $name" else name,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (name == winnerName) Color(0xFFFFD54F) else Color.White
                            )
                            Text(
                                text = "$score pts",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color(0xFF81C784),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (index < playerScores.lastIndex) {
                            HorizontalDivider(color = Color(0xFF388E3C))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onPlayAgain,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text(
                    text = "Play Again",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onBackToMenu,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF81C784))
            ) {
                Text("Back to Menu")
            }
        }
    }
}

private fun parseScores(encoded: String): List<Pair<String, Int>> {
    if (encoded.isBlank()) return emptyList()
    return encoded.split("|").mapNotNull { entry ->
        val parts = entry.split(":")
        if (parts.size == 2) {
            parts[0] to (parts[1].toIntOrNull() ?: 0)
        } else null
    }
}

@Preview(showBackground = true)
@Composable
fun ResultScreenPreview() {
    DominoBlockadeTheme {
        ResultScreen(
            winnerName = "You",
            scoresEncoded = "You:0|AI 1:15|AI 2:22",
            onPlayAgain = {},
            onBackToMenu = {}
        )
    }
}

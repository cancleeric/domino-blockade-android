package com.cancleeric.dominoblockade.presentation.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cancleeric.dominoblockade.domain.model.*
import com.cancleeric.dominoblockade.presentation.components.GameBoard
import com.cancleeric.dominoblockade.presentation.components.PlayerHand
import com.cancleeric.dominoblockade.ui.theme.DominoBlockadeTheme

@Composable
fun GameScreen(
    numPlayers: Int,
    aiDifficulty: String,
    onGameEnd: (winnerName: String, scores: String) -> Unit,
    viewModel: GameViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(numPlayers, aiDifficulty) {
        viewModel.startGame(numPlayers, aiDifficulty)
    }

    val gameState = uiState.gameState
    if (gameState != null && gameState.phase != GamePhase.PLAYING) {
        val winnerName = gameState.players.getOrNull(gameState.winnerIndex ?: -1)?.name ?: "Unknown"
        val scores = gameState.players.joinToString("|") { "${it.name}:${it.handPipCount}" }
        LaunchedEffect(gameState.phase) {
            onGameEnd(winnerName, scores)
        }
    }

    GameScreenContent(
        uiState = uiState,
        onDominoSelected = viewModel::onDominoSelected,
        onEndSelected = viewModel::onEndSelected,
        onDrawTile = viewModel::onDrawTile,
        onSkipTurn = viewModel::onSkipTurn,
        canPlaceAt = viewModel::canPlaceAt
    )
}

@Composable
fun GameScreenContent(
    uiState: GameUiState,
    onDominoSelected: (Domino) -> Unit,
    onEndSelected: (BoardEnd) -> Unit,
    onDrawTile: () -> Unit,
    onSkipTurn: () -> Unit,
    canPlaceAt: (BoardEnd) -> Boolean
) {
    val gameState = uiState.gameState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1B5E20))
            .systemBarsPadding()
    ) {
        Surface(
            color = Color(0xFF2E7D32),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = uiState.message,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Text(
                    text = "Draw: ${gameState?.drawPile?.size ?: 0}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }
        }

        if (gameState != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                gameState.players.filter { it.isAi }.forEach { player ->
                    Surface(
                        color = Color(0xFF388E3C),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "${player.name}: ${player.hand.size} tiles",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        Surface(
            color = Color(0xFF2E7D32),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (gameState != null) {
                GameBoard(
                    board = gameState.board,
                    leftEnd = gameState.leftEnd,
                    rightEnd = gameState.rightEnd,
                    selectedDomino = uiState.selectedDomino,
                    canPlaceLeft = canPlaceAt(BoardEnd.LEFT),
                    canPlaceRight = canPlaceAt(BoardEnd.RIGHT),
                    onEndClick = onEndSelected,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        if (uiState.isAiThinking) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF81C784)
            )
        }

        Surface(
            color = Color(0xFF1B5E20),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                if (gameState != null) {
                    val humanPlayer = gameState.players.firstOrNull { !it.isAi }
                    if (humanPlayer != null) {
                        Text(
                            text = "Your hand (${humanPlayer.hand.size} tiles)",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                        PlayerHand(
                            hand = humanPlayer.hand,
                            selectedDomino = uiState.selectedDomino,
                            playableDominoes = if (!gameState.currentPlayer.isAi) uiState.playableDominoes else emptySet(),
                            onDominoClick = onDominoSelected
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val canDraw = gameState?.drawPile?.isNotEmpty() == true &&
                        gameState.currentPlayer.let { !it.isAi }
                    val canSkip = gameState?.currentPlayer?.let { !it.isAi } == true &&
                        gameState.validMovesFor(gameState.currentPlayer).isEmpty()

                    Button(
                        onClick = onDrawTile,
                        enabled = canDraw && !uiState.isAiThinking,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Text("Draw Tile")
                    }

                    Button(
                        onClick = onSkipTurn,
                        enabled = canSkip && !uiState.isAiThinking,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF57F17)
                        )
                    ) {
                        Text("Skip Turn")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GameScreenPreview() {
    DominoBlockadeTheme {
        val sampleState = GameState(
            players = listOf(
                Player(0, "You", listOf(Domino(3, 5), Domino(0, 6), Domino(2, 4)), isAi = false),
                Player(1, "AI 1", listOf(Domino(1, 3), Domino(4, 4)), isAi = true)
            ),
            board = listOf(PlacedDomino(Domino(6, 4)), PlacedDomino(Domino(4, 2))),
            drawPile = List(10) { Domino(it % 7, (it + 1) % 7) },
            currentPlayerIndex = 0,
            phase = GamePhase.PLAYING
        )
        GameScreenContent(
            uiState = GameUiState(
                gameState = sampleState,
                message = "Your turn",
                playableDominoes = setOf(Domino(3, 5)),
                selectedDomino = null
            ),
            onDominoSelected = {},
            onEndSelected = {},
            onDrawTile = {},
            onSkipTurn = {},
            canPlaceAt = { false }
        )
    }
}

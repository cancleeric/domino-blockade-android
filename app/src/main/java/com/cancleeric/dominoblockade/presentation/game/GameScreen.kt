package com.cancleeric.dominoblockade.presentation.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cancleeric.dominoblockade.domain.engine.BoardSide
import com.cancleeric.dominoblockade.domain.model.Domino
import com.cancleeric.dominoblockade.domain.model.GamePhase
import com.cancleeric.dominoblockade.domain.model.GameState
import com.cancleeric.dominoblockade.domain.model.Player

/**
 * Game screen for local multiplayer mode.
 *
 * Renders:
 * - Player info bar (scores, current turn indicator)
 * - Board (horizontal scrollable chain)
 * - Current player's hand (tiles)
 * - Draw pile & action buttons
 */
@Composable
fun GameScreen(
    viewModel: MultiplayerGameViewModel,
    onPassAndPlay: (previousName: String, nextName: String) -> Unit,
    onGameOver: () -> Unit
) {
    val gameState by viewModel.gameState.collectAsState()
    val error by viewModel.error.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show errors via Snackbar
    LaunchedEffect(error) {
        if (error != null) {
            snackbarHostState.showSnackbar(error!!)
            viewModel.clearError()
        }
    }

    // Handle phase transitions
    LaunchedEffect(gameState?.phase) {
        val state = gameState ?: return@LaunchedEffect
        val phase = state.phase
        when {
            phase is GamePhase.PassAndPlay -> {
                val prev = state.players[phase.previousPlayerIndex].name
                val next = state.players[phase.nextPlayerIndex].name
                onPassAndPlay(prev, next)
            }
            phase is GamePhase.GameOver -> onGameOver()
            else -> {}
        }
    }

    val state = gameState ?: return

    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Player scores bar
                PlayerInfoBar(
                    players = state.players,
                    currentPlayerIndex = state.currentPlayerIndex
                )

                Spacer(Modifier.height(8.dp))

                // Board
                BoardView(
                    board = state.board,
                    leftEnd = state.boardLeftEnd,
                    rightEnd = state.boardRightEnd,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                Spacer(Modifier.height(8.dp))

                // Current player info
                val currentPlayer = state.currentPlayer
                Text(
                    text = "${currentPlayer.name}'s turn",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(4.dp))

                // Hand
                PlayerHandView(
                    player = currentPlayer,
                    boardLeftEnd = state.boardLeftEnd,
                    boardRightEnd = state.boardRightEnd,
                    boardEmpty = state.board.isEmpty(),
                    onPlayLeft = { domino -> viewModel.playDomino(domino, BoardSide.LEFT) },
                    onPlayRight = { domino -> viewModel.playDomino(domino, BoardSide.RIGHT) }
                )

                Spacer(Modifier.height(8.dp))

                // Draw pile button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    OutlinedButton(
                        onClick = { viewModel.drawTile() },
                        enabled = state.drawPile.isNotEmpty() &&
                            !viewModel.currentPlayerCanPlay()
                    ) {
                        Text("Draw Tile (${state.drawPile.size} left)")
                    }
                }

                Spacer(Modifier.height(4.dp))
            }
        }

        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// -------------------------------------------------------------------------
// Sub-components
// -------------------------------------------------------------------------

@Composable
private fun PlayerInfoBar(players: List<Player>, currentPlayerIndex: Int) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            players.forEachIndexed { index, player ->
                PlayerChip(
                    player = player,
                    isCurrentPlayer = index == currentPlayerIndex
                )
            }
        }
    }
}

@Composable
private fun PlayerChip(player: Player, isCurrentPlayer: Boolean) {
    val bgColor = if (isCurrentPlayer)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = player.name,
                style = MaterialTheme.typography.labelMedium,
                color = if (isCurrentPlayer)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${player.handSize} tiles · ${player.handScore} pts",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BoardView(
    board: List<Domino>,
    leftEnd: Int?,
    rightEnd: Int?,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (board.isEmpty()) {
                Text(
                    text = "Board is empty\nPlay the first tile!",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Board  ←$leftEnd  ·  $rightEnd→",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        board.forEach { domino ->
                            DominoTileView(domino = domino, isSmall = true)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerHandView(
    player: Player,
    boardLeftEnd: Int?,
    boardRightEnd: Int?,
    boardEmpty: Boolean,
    onPlayLeft: (Domino) -> Unit,
    onPlayRight: (Domino) -> Unit
) {
    var selectedDomino by remember(player) { mutableStateOf<Domino?>(null) }

    Column {
        if (player.hand.isEmpty()) {
            Text(
                text = "Hand is empty!",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            return
        }

        // Tile row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            player.hand.forEach { domino ->
                val isPlayable = boardEmpty ||
                    (boardLeftEnd != null && domino.canConnectTo(boardLeftEnd)) ||
                    (boardRightEnd != null && domino.canConnectTo(boardRightEnd))

                DominoTileView(
                    domino = domino,
                    isSelected = domino == selectedDomino,
                    isPlayable = isPlayable,
                    onClick = {
                        selectedDomino = if (selectedDomino == domino) null else domino
                    }
                )
            }
        }

        // Action buttons for selected tile
        selectedDomino?.let { domino ->
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                val canLeft = boardEmpty || (boardLeftEnd != null && domino.canConnectTo(boardLeftEnd))
                val canRight = boardEmpty || (boardRightEnd != null && domino.canConnectTo(boardRightEnd))

                if (canLeft || boardEmpty) {
                    Button(
                        onClick = {
                            onPlayLeft(domino)
                            selectedDomino = null
                        }
                    ) {
                        Text("← Play Left")
                    }
                }

                if ((canRight || boardEmpty) && !boardEmpty) {
                    Button(
                        onClick = {
                            onPlayRight(domino)
                            selectedDomino = null
                        }
                    ) {
                        Text("Play Right →")
                    }
                }

                if (boardEmpty) {
                    Button(
                        onClick = {
                            onPlayRight(domino)
                            selectedDomino = null
                        }
                    ) {
                        Text("Play Tile")
                    }
                }
            }
        }
    }
}

@Composable
private fun DominoTileView(
    domino: Domino,
    isSelected: Boolean = false,
    isPlayable: Boolean = true,
    isSmall: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val size = if (isSmall) 40.dp else 60.dp
    val borderColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        !isPlayable -> MaterialTheme.colorScheme.outlineVariant
        else -> MaterialTheme.colorScheme.outline
    }
    val bgColor = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        !isPlayable -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surface
    }

    Box(
        modifier = Modifier
            .size(width = size, height = size * 0.6f)
            .background(bgColor, RoundedCornerShape(4.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(4.dp)
            )
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        val textStyle = if (isSmall) MaterialTheme.typography.labelSmall
        else MaterialTheme.typography.bodyMedium
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = "${domino.left}", style = textStyle)
            Text(text = "|", style = textStyle, color = Color.Gray)
            Text(text = "${domino.right}", style = textStyle)
        }
    }
}

package com.cancleeric.dominoblockade.presentation.game

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cancleeric.dominoblockade.domain.model.Domino

private const val BOARD_HEIGHT_DP = 120
private const val BOARD_PADDING_DP = 8
private const val BOARD_SPACING_DP = 4

/**
 * Displays the sequence of domino tiles placed on the game board, scrollable horizontally.
 *
 * @param board Ordered list of tiles on the board (left-to-right).
 */
@Composable
fun GameBoard(
    board: List<Domino>,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    LaunchedEffect(board.size) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(BOARD_HEIGHT_DP.dp),
        contentAlignment = Alignment.Center
    ) {
        if (board.isEmpty()) {
            Text(
                text = "Place the first tile",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .padding(BOARD_PADDING_DP.dp),
                horizontalArrangement = Arrangement.spacedBy(BOARD_SPACING_DP.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                board.forEach { domino ->
                    key(domino) {
                        DominoTile(
                            left = domino.left,
                            right = domino.right,
                            isVertical = false
                        )
                    }
                }
            }
        }
    }
}

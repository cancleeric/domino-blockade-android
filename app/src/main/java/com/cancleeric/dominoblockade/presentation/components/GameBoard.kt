package com.cancleeric.dominoblockade.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cancleeric.dominoblockade.domain.model.BoardEnd
import com.cancleeric.dominoblockade.domain.model.Domino
import com.cancleeric.dominoblockade.domain.model.PlacedDomino
import com.cancleeric.dominoblockade.ui.theme.DominoBlockadeTheme

@Composable
fun GameBoard(
    board: List<PlacedDomino>,
    leftEnd: Int?,
    rightEnd: Int?,
    selectedDomino: Domino?,
    canPlaceLeft: Boolean,
    canPlaceRight: Boolean,
    onEndClick: (BoardEnd) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (selectedDomino != null && leftEnd != null) {
            EndDropZone(
                end = BoardEnd.LEFT,
                endValue = leftEnd,
                canPlace = canPlaceLeft,
                onClick = { onEndClick(BoardEnd.LEFT) }
            )
        }

        if (board.isEmpty()) {
            Box(
                modifier = Modifier
                    .size(160.dp, 60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF388E3C).copy(alpha = 0.3f))
                    .border(2.dp, Color(0xFF388E3C), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Play first tile",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF388E3C),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            board.forEach { placedDomino ->
                DominoTile(
                    domino = placedDomino.domino,
                    isHorizontal = placedDomino.isHorizontal
                )
            }
        }

        if (selectedDomino != null && rightEnd != null && board.isNotEmpty()) {
            EndDropZone(
                end = BoardEnd.RIGHT,
                endValue = rightEnd,
                canPlace = canPlaceRight,
                onClick = { onEndClick(BoardEnd.RIGHT) }
            )
        }
    }
}

@Composable
private fun EndDropZone(
    end: BoardEnd,
    endValue: Int,
    canPlace: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (canPlace) Color(0xFF81C784) else Color(0xFFEF9A9A)
    val label = when (end) {
        BoardEnd.LEFT -> "<- $endValue"
        BoardEnd.RIGHT -> "$endValue ->"
    }

    Box(
        modifier = modifier
            .size(48.dp, 48.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .then(if (canPlace) Modifier.clickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GameBoardPreview() {
    DominoBlockadeTheme {
        GameBoard(
            board = listOf(
                PlacedDomino(Domino(6, 4)),
                PlacedDomino(Domino(4, 2)),
                PlacedDomino(Domino(2, 5))
            ),
            leftEnd = 6,
            rightEnd = 5,
            selectedDomino = Domino(5, 3),
            canPlaceLeft = false,
            canPlaceRight = true,
            onEndClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GameBoardEmptyPreview() {
    DominoBlockadeTheme {
        GameBoard(
            board = emptyList(),
            leftEnd = null,
            rightEnd = null,
            selectedDomino = null,
            canPlaceLeft = false,
            canPlaceRight = false,
            onEndClick = {}
        )
    }
}

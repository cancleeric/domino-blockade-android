package com.cancleeric.dominoblockade.presentation.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cancleeric.dominoblockade.domain.model.Domino
import com.cancleeric.dominoblockade.ui.theme.DominoBlockadeTheme

@Composable
fun PlayerHand(
    hand: List<Domino>,
    selectedDomino: Domino?,
    playableDominoes: Set<Domino>,
    onDominoClick: (Domino) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        hand.forEach { domino ->
            DominoTile(
                domino = domino,
                isHorizontal = true,
                isSelected = domino == selectedDomino,
                isPlayable = domino in playableDominoes,
                onClick = { onDominoClick(domino) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PlayerHandPreview() {
    DominoBlockadeTheme {
        val hand = listOf(
            Domino(3, 5),
            Domino(0, 6),
            Domino(2, 4),
            Domino(1, 1),
            Domino(5, 5)
        )
        PlayerHand(
            hand = hand,
            selectedDomino = hand[1],
            playableDominoes = setOf(hand[0], hand[1], hand[2]),
            onDominoClick = {}
        )
    }
}

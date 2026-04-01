package com.cancleeric.dominoblockade.presentation.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cancleeric.dominoblockade.domain.model.Domino

private const val HAND_PADDING_DP = 8
private const val HAND_SPACING_DP = 4

/**
 * Displays the current player's hand of domino tiles in a horizontally scrollable row.
 *
 * @param hand The list of dominoes in the player's hand.
 * @param selectedDomino The currently selected domino, or null.
 * @param canPlace A function that returns true if the given domino can be legally placed.
 * @param onSelectDomino Called when a tile is tapped.
 */
@Composable
fun PlayerHand(
    hand: List<Domino>,
    selectedDomino: Domino?,
    canPlace: (Domino) -> Boolean,
    onSelectDomino: (Domino) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(HAND_PADDING_DP.dp),
        horizontalArrangement = Arrangement.spacedBy(HAND_SPACING_DP.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        hand.forEach { domino ->
            key(domino) {
                var visible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { visible = true }
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn() + expandHorizontally(),
                    exit = fadeOut() + shrinkHorizontally()
                ) {
                    DominoTile(
                        left = domino.left,
                        right = domino.right,
                        isSelected = domino == selectedDomino,
                        isPlayable = canPlace(domino),
                        isVertical = true,
                        onClick = { onSelectDomino(domino) }
                    )
                }
            }
        }
    }
}

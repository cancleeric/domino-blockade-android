package com.cancleeric.dominoblockade.presentation.game

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cancleeric.dominoblockade.domain.model.DominoSkin
import com.cancleeric.dominoblockade.domain.model.DominoStyle
import com.cancleeric.dominoblockade.ui.theme.LocalDominoSkin
import com.cancleeric.dominoblockade.ui.theme.LocalDominoStyle

private const val SELECTED_SCALE = 1.08f
private const val NORMAL_SCALE = 1f

private const val HALF_SIZE_DP = 36
private const val DOT_RADIUS_DP = 4
private const val BORDER_WIDTH_DP = 2
private const val CORNER_RADIUS_DP = 6
private const val DIVIDER_DP = 2
private const val PADDING_DP = 4

private const val POS_LOW = 0.25f
private const val POS_MID = 0.5f
private const val POS_HIGH = 0.75f

private data class DotPosition(val x: Float, val y: Float)

private val dotPatterns: List<List<DotPosition>> = listOf(
    emptyList(),
    listOf(DotPosition(POS_MID, POS_MID)),
    listOf(DotPosition(POS_LOW, POS_LOW), DotPosition(POS_HIGH, POS_HIGH)),
    listOf(DotPosition(POS_LOW, POS_LOW), DotPosition(POS_MID, POS_MID), DotPosition(POS_HIGH, POS_HIGH)),
    listOf(
        DotPosition(POS_LOW, POS_LOW), DotPosition(POS_HIGH, POS_LOW),
        DotPosition(POS_LOW, POS_HIGH), DotPosition(POS_HIGH, POS_HIGH)
    ),
    listOf(
        DotPosition(POS_LOW, POS_LOW), DotPosition(POS_HIGH, POS_LOW),
        DotPosition(POS_MID, POS_MID),
        DotPosition(POS_LOW, POS_HIGH), DotPosition(POS_HIGH, POS_HIGH)
    ),
    listOf(
        DotPosition(POS_LOW, POS_LOW), DotPosition(POS_HIGH, POS_LOW),
        DotPosition(POS_LOW, POS_MID), DotPosition(POS_HIGH, POS_MID),
        DotPosition(POS_LOW, POS_HIGH), DotPosition(POS_HIGH, POS_HIGH)
    )
)

/**
 * Displays a single domino tile with dot pips on each half.
 *
 * @param left Left (or top in vertical mode) pip value, 0–6.
 * @param right Right (or bottom in vertical mode) pip value, 0–6.
 * @param isSelected Whether this tile is currently selected (highlighted border).
 * @param isPlayable Whether this tile can legally be placed.
 * @param isVertical If true, the tile is oriented vertically (top/bottom halves).
 * @param onClick Called when the tile is tapped.
 */
@Composable
fun DominoTile(
    left: Int,
    right: Int,
    isSelected: Boolean = false,
    isPlayable: Boolean = false,
    isVertical: Boolean = true,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val description = buildContentDescription(left, right)
    val scale by animateFloatAsState(
        targetValue = if (isSelected) SELECTED_SCALE else NORMAL_SCALE,
        animationSpec = spring(),
        label = "tileScale"
    )
    val borderColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isPlayable -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.outline
    }
    val borderWidth: Dp = if (isSelected || isPlayable) (BORDER_WIDTH_DP * 2).dp else BORDER_WIDTH_DP.dp
    val shape = RoundedCornerShape(CORNER_RADIUS_DP.dp)
    val dominoStyle = LocalDominoStyle.current
    val dominoSkin = LocalDominoSkin.current
    val (skinTileColor, skinDotColor) = skinColors(dominoSkin)
    val bgColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        skinTileColor
    }
    val dotColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else skinDotColor

    val tileModifier = modifier
        .scale(scale)
        .clip(shape)
        .background(bgColor)
        .border(borderWidth, borderColor, shape)
        .clickable(onClick = onClick)
        .padding(PADDING_DP.dp)
        .semantics { contentDescription = description }

    if (isVertical) {
        Column(
            modifier = tileModifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PipFace(pips = left, dotColor = dotColor, style = dominoStyle)
            TileDivider(isVertical = true, color = MaterialTheme.colorScheme.outline)
            PipFace(pips = right, dotColor = dotColor, style = dominoStyle)
        }
    } else {
        Row(
            modifier = tileModifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PipFace(pips = left, dotColor = dotColor, style = dominoStyle)
            TileDivider(isVertical = false, color = MaterialTheme.colorScheme.outline)
            PipFace(pips = right, dotColor = dotColor, style = dominoStyle)
        }
    }
}

@Composable
private fun TileDivider(isVertical: Boolean, color: Color) {
    val dividerModifier = if (isVertical) {
        Modifier.height(DIVIDER_DP.dp)
    } else {
        Modifier.width(DIVIDER_DP.dp)
    }
    Spacer(dividerModifier.background(color))
}

@Composable
private fun PipFace(pips: Int, dotColor: Color, style: DominoStyle) {
    if (style == DominoStyle.NUMBERS) {
        NumberFace(pips = pips, dotColor = dotColor)
    } else {
        DotFace(pips = pips, dotColor = dotColor)
    }
}

@Composable
private fun DotFace(pips: Int, dotColor: Color) {
    val safeIndex = pips.coerceIn(0, dotPatterns.size - 1)
    val positions = dotPatterns[safeIndex]
    val dotRadiusDp = DOT_RADIUS_DP.dp
    Box(
        modifier = Modifier.size(HALF_SIZE_DP.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(HALF_SIZE_DP.dp)) {
            val dotRadius = dotRadiusDp.toPx()
            positions.forEach { pos ->
                drawCircle(
                    color = dotColor,
                    radius = dotRadius,
                    center = Offset(pos.x * size.width, pos.y * size.height)
                )
            }
        }
    }
}

@Composable
private fun NumberFace(pips: Int, dotColor: Color) {
    Box(
        modifier = Modifier.size(HALF_SIZE_DP.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = pips.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = dotColor
        )
    }
}

private fun buildContentDescription(left: Int, right: Int): String {
    return if (left == right) "Double $left domino" else "Domino $left $right"
}

private fun skinColors(skin: DominoSkin): Pair<Color, Color> = when (skin) {
    DominoSkin.CLASSIC -> Color.White to Color.Black
    DominoSkin.MARBLE -> Color(0xFFF2F2F2) to Color(0xFF2C2C2C)
    DominoSkin.NEON -> Color(0xFF101820) to Color(0xFF7DF9FF)
    DominoSkin.GOLD -> Color(0xFFFFD54F) to Color(0xFF5D4037)
}

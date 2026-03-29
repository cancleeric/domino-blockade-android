package com.cancleeric.dominoblockade.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cancleeric.dominoblockade.domain.model.Domino
import com.cancleeric.dominoblockade.ui.theme.DominoBlockadeTheme

@Composable
fun DominoTile(
    domino: Domino,
    isHorizontal: Boolean = true,
    isSelected: Boolean = false,
    isPlayable: Boolean = true,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        !isPlayable -> Color(0xFFBDBDBD)
        isSelected -> Color(0xFFFFF9C4)
        else -> Color.White
    }
    val borderColor = when {
        isSelected -> Color(0xFFF9A825)
        else -> Color(0xFF424242)
    }
    val dotColor = when {
        !isPlayable -> Color(0xFF757575)
        else -> Color(0xFF212121)
    }

    val tileWidth: Dp
    val tileHeight: Dp
    if (isHorizontal) {
        tileWidth = 80.dp
        tileHeight = 40.dp
    } else {
        tileWidth = 40.dp
        tileHeight = 80.dp
    }

    val shape = RoundedCornerShape(4.dp)

    Canvas(
        modifier = modifier
            .size(tileWidth, tileHeight)
            .clip(shape)
            .border(2.dp, borderColor, shape)
            .drawBehind { drawRect(color = backgroundColor) }
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        val halfW = size.width / 2f
        val halfH = size.height / 2f

        if (isHorizontal) {
            drawLine(
                color = Color(0xFF424242),
                start = Offset(halfW, 4f),
                end = Offset(halfW, size.height - 4f),
                strokeWidth = 1.5f
            )
            drawPips(domino.left, dotColor, Offset(0f, 0f), Size(halfW, size.height))
            drawPips(domino.right, dotColor, Offset(halfW, 0f), Size(halfW, size.height))
        } else {
            drawLine(
                color = Color(0xFF424242),
                start = Offset(4f, halfH),
                end = Offset(size.width - 4f, halfH),
                strokeWidth = 1.5f
            )
            drawPips(domino.left, dotColor, Offset(0f, 0f), Size(size.width, halfH))
            drawPips(domino.right, dotColor, Offset(0f, halfH), Size(size.width, halfH))
        }
    }
}

private fun DrawScope.drawPips(count: Int, color: Color, offset: Offset, area: Size) {
    val dotRadius = minOf(area.width, area.height) * 0.09f

    val positions: List<Pair<Float, Float>> = when (count) {
        0 -> emptyList()
        1 -> listOf(0.5f to 0.5f)
        2 -> listOf(0.25f to 0.25f, 0.75f to 0.75f)
        3 -> listOf(0.25f to 0.25f, 0.5f to 0.5f, 0.75f to 0.75f)
        4 -> listOf(0.25f to 0.25f, 0.75f to 0.25f, 0.25f to 0.75f, 0.75f to 0.75f)
        5 -> listOf(0.25f to 0.25f, 0.75f to 0.25f, 0.5f to 0.5f, 0.25f to 0.75f, 0.75f to 0.75f)
        6 -> listOf(
            0.25f to 0.2f, 0.75f to 0.2f,
            0.25f to 0.5f, 0.75f to 0.5f,
            0.25f to 0.8f, 0.75f to 0.8f
        )
        else -> emptyList()
    }

    for ((xRatio, yRatio) in positions) {
        val cx = offset.x + area.width * xRatio
        val cy = offset.y + area.height * yRatio
        drawCircle(color = color, radius = dotRadius, center = Offset(cx, cy))
    }
}

@Preview(showBackground = true)
@Composable
fun DominoTilePreview() {
    DominoBlockadeTheme {
        Column {
            DominoTile(domino = Domino(3, 5), isHorizontal = true)
            DominoTile(domino = Domino(6, 2), isHorizontal = false)
            DominoTile(domino = Domino(0, 4), isSelected = true)
            DominoTile(domino = Domino(1, 3), isPlayable = false)
        }
    }
}

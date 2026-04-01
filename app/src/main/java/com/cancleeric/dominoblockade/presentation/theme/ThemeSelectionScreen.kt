package com.cancleeric.dominoblockade.presentation.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cancleeric.dominoblockade.domain.model.AppTheme
import com.cancleeric.dominoblockade.domain.model.DominoStyle
import com.cancleeric.dominoblockade.presentation.game.DominoTile
import com.cancleeric.dominoblockade.ui.theme.ClassicBoard
import com.cancleeric.dominoblockade.ui.theme.ClassicDot
import com.cancleeric.dominoblockade.ui.theme.ClassicTile
import com.cancleeric.dominoblockade.ui.theme.DarkBoard
import com.cancleeric.dominoblockade.ui.theme.DarkDot
import com.cancleeric.dominoblockade.ui.theme.DarkTile
import com.cancleeric.dominoblockade.ui.theme.LocalDominoStyle
import com.cancleeric.dominoblockade.ui.theme.NeonBoard
import com.cancleeric.dominoblockade.ui.theme.NeonDot
import com.cancleeric.dominoblockade.ui.theme.NeonTile
import com.cancleeric.dominoblockade.ui.theme.WoodBoard
import com.cancleeric.dominoblockade.ui.theme.WoodDot
import com.cancleeric.dominoblockade.ui.theme.WoodTile

private const val PREVIEW_BOARD_HEIGHT_DP = 80
private const val CARD_CORNER_DP = 12
private const val CARD_PADDING_DP = 12
private const val SECTION_SPACING_DP = 24
private const val SELECTED_BORDER_DP = 3
private const val UNSELECTED_BORDER_DP = 1
private const val STYLE_CARD_HEIGHT_DP = 72

private data class ThemePreviewColors(
    val tileColor: Color,
    val dotColor: Color,
    val boardColor: Color
)

private val themePreviewColors = mapOf(
    AppTheme.CLASSIC to ThemePreviewColors(ClassicTile, ClassicDot, ClassicBoard),
    AppTheme.DARK to ThemePreviewColors(DarkTile, DarkDot, DarkBoard),
    AppTheme.WOOD to ThemePreviewColors(WoodTile, WoodDot, WoodBoard),
    AppTheme.NEON to ThemePreviewColors(NeonTile, NeonDot, NeonBoard)
)

private val themeLabels = mapOf(
    AppTheme.CLASSIC to "Classic",
    AppTheme.DARK to "Dark",
    AppTheme.WOOD to "Wood",
    AppTheme.NEON to "Neon"
)

private val styleLabels = mapOf(
    DominoStyle.DOTS to "Dots",
    DominoStyle.NUMBERS to "Numbers"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSelectionScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ThemeViewModel = hiltViewModel()
) {
    val selectedTheme by viewModel.appTheme.collectAsStateWithLifecycle()
    val selectedStyle by viewModel.dominoStyle.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Theme Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(CARD_PADDING_DP.dp),
            verticalArrangement = Arrangement.spacedBy(SECTION_SPACING_DP.dp)
        ) {
            ThemeSectionLabel(text = "Board Theme")
            ThemeGrid(
                selectedTheme = selectedTheme,
                onThemeSelected = viewModel::selectTheme
            )
            ThemeSectionLabel(text = "Domino Style")
            StyleRow(
                selectedStyle = selectedStyle,
                onStyleSelected = viewModel::selectDominoStyle
            )
        }
    }
}

@Composable
private fun ThemeSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
private fun ThemeGrid(
    selectedTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(CARD_PADDING_DP.dp)
    ) {
        AppTheme.entries.forEach { theme ->
            ThemeCard(
                theme = theme,
                isSelected = theme == selectedTheme,
                onClick = { onThemeSelected(theme) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ThemeCard(
    theme: AppTheme,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = themePreviewColors[theme] ?: themePreviewColors[AppTheme.CLASSIC]!!
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val borderWidth = if (isSelected) SELECTED_BORDER_DP.dp else UNSELECTED_BORDER_DP.dp
    val shape = RoundedCornerShape(CARD_CORNER_DP.dp)

    Column(
        modifier = modifier
            .clip(shape)
            .border(borderWidth, borderColor, shape)
            .clickable(onClick = onClick)
            .padding(CARD_PADDING_DP.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ThemeBoardPreview(
            tileColor = colors.tileColor,
            dotColor = colors.dotColor,
            boardColor = colors.boardColor
        )
        Text(
            text = themeLabels[theme].orEmpty(),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun ThemeBoardPreview(
    tileColor: Color,
    dotColor: Color,
    boardColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(PREVIEW_BOARD_HEIGHT_DP.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(boardColor),
        contentAlignment = Alignment.Center
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            PreviewTile(left = 3, right = 5, tileColor = tileColor, dotColor = dotColor)
            PreviewTile(left = 5, right = 2, tileColor = tileColor, dotColor = dotColor)
        }
    }
}

@Composable
private fun PreviewTile(left: Int, right: Int, tileColor: Color, dotColor: Color) {
    Box(
        modifier = Modifier
            .size(width = 30.dp, height = 56.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(tileColor)
            .border(1.dp, dotColor.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            PipDots(value = left, dotColor = dotColor)
            Spacer(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(dotColor.copy(alpha = 0.3f))
            )
            PipDots(value = right, dotColor = dotColor)
        }
    }
}

@Composable
private fun PipDots(value: Int, dotColor: Color) {
    val count = value.coerceIn(0, 6)
    Box(
        modifier = Modifier
            .size(24.dp)
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = dotColor
        )
    }
}

@Composable
private fun StyleRow(
    selectedStyle: DominoStyle,
    onStyleSelected: (DominoStyle) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(CARD_PADDING_DP.dp)
    ) {
        DominoStyle.entries.forEach { style ->
            StyleCard(
                style = style,
                isSelected = style == selectedStyle,
                onClick = { onStyleSelected(style) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StyleCard(
    style: DominoStyle,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val borderWidth = if (isSelected) SELECTED_BORDER_DP.dp else UNSELECTED_BORDER_DP.dp
    val shape = RoundedCornerShape(CARD_CORNER_DP.dp)

    Column(
        modifier = modifier
            .clip(shape)
            .border(borderWidth, borderColor, shape)
            .clickable(onClick = onClick)
            .padding(CARD_PADDING_DP.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(STYLE_CARD_HEIGHT_DP.dp),
            contentAlignment = Alignment.Center
        ) {
            CompositionLocalProvider(LocalDominoStyle provides style) {
                DominoTile(left = 3, right = 5, isVertical = true)
            }
        }
        Text(
            text = styleLabels[style].orEmpty(),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

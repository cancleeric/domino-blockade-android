package com.cancleeric.dominoblockade.presentation.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cancleeric.dominoblockade.domain.model.AppTheme
import com.cancleeric.dominoblockade.domain.model.DominoStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSelectionScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ThemeViewModel = hiltViewModel()
) {
    val prefs by viewModel.themePreferences.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Theme Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
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
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "App Theme",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height(300.dp)
            ) {
                items(AppTheme.entries) { theme ->
                    ThemePreviewCard(
                        theme = theme,
                        isSelected = prefs.appTheme == theme,
                        onClick = { viewModel.setAppTheme(theme) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Domino Style",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                DominoStyle.entries.forEach { style ->
                    FilterChip(
                        selected = prefs.dominoStyle == style,
                        onClick = { viewModel.setDominoStyle(style) },
                        label = { Text(style.displayName) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Preview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            DominoPreview(
                dominoStyle = prefs.dominoStyle,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ThemePreviewCard(
    theme: AppTheme,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (bgColor, primaryColor, tileColor) = when (theme) {
        AppTheme.CLASSIC -> Triple(Color(0xFFF5F5F5), Color(0xFF6650A4), Color(0xFFFAFAFA))
        AppTheme.DARK -> Triple(Color(0xFF121212), Color(0xFF90CAF9), Color(0xFF212121))
        AppTheme.WOOD -> Triple(Color(0xFFEFEBE9), Color(0xFF795548), Color(0xFFD7CCC8))
        AppTheme.NEON -> Triple(Color(0xFF0A0A0A), Color(0xFF00FF88), Color(0xFF1A1A2E))
    }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 6.dp else 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Mini domino tile preview
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    repeat(2) {
                        Box(
                            modifier = Modifier
                                .size(width = 20.dp, height = 36.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(tileColor)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(primaryColor)
                                    .align(Alignment.Center)
                            )
                        }
                    }
                }
                Text(
                    text = theme.displayName,
                    style = MaterialTheme.typography.labelMedium,
                    color = primaryColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun DominoPreview(
    dominoStyle: DominoStyle,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DominoTile(value = 3, style = dominoStyle)
            Spacer(modifier = Modifier.size(8.dp))
            DominoTile(value = 5, style = dominoStyle)
        }
    }
}

@Composable
private fun DominoTile(
    value: Int,
    style: DominoStyle,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(width = 50.dp, height = 90.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        when (style) {
            DominoStyle.DOTS -> {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    repeat(value.coerceAtMost(3)) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
            DominoStyle.NUMBERS -> {
                Text(
                    text = value.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            DominoStyle.SYMBOLS -> {
                val symbol = when (value) {
                    1 -> "♦"
                    2 -> "♠"
                    3 -> "♣"
                    4 -> "♥"
                    5 -> "★"
                    6 -> "⚡"
                    else -> value.toString()
                }
                Text(
                    text = symbol,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

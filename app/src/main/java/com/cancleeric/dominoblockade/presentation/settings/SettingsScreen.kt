package com.cancleeric.dominoblockade.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private const val SCREEN_PADDING_DP = 16
private const val ITEM_SPACING_DP = 8
private const val SECTION_SPACING_DP = 16
private const val CHIP_SPACING_DP = 8

private val aiDifficultyOptions = listOf("easy", "medium", "hard")
private val aiDifficultyLabels = mapOf("easy" to "Easy", "medium" to "Medium", "hard" to "Hard")

private val languageOptions = listOf("en", "zh-TW", "zh-CN")
private val languageLabels = mapOf("en" to "English", "zh-TW" to "繁中", "zh-CN" to "簡中")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        SettingsContent(
            uiState = uiState,
            onSoundToggle = viewModel::setSoundEnabled,
            onMusicToggle = viewModel::setMusicEnabled,
            onVibrationToggle = viewModel::setVibrationEnabled,
            onAiDifficultyChange = viewModel::setAiDifficulty,
            onLanguageChange = viewModel::setLanguage,
            onDarkModeToggle = viewModel::setDarkModeEnabled,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    onSoundToggle: (Boolean) -> Unit,
    onMusicToggle: (Boolean) -> Unit,
    onVibrationToggle: (Boolean) -> Unit,
    onAiDifficultyChange: (String) -> Unit,
    onLanguageChange: (String) -> Unit,
    onDarkModeToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = SCREEN_PADDING_DP.dp),
        verticalArrangement = Arrangement.spacedBy(ITEM_SPACING_DP.dp)
    ) {
        SettingsSectionHeader(title = "AI Difficulty")
        AiDifficultySelector(
            selected = uiState.aiDifficulty,
            onSelect = onAiDifficultyChange
        )
        SettingsSectionHeader(
            title = "Audio",
            modifier = Modifier.padding(top = SECTION_SPACING_DP.dp)
        )
        SettingsToggleItem(
            label = "Sound Effects",
            checked = uiState.soundEnabled,
            onCheckedChange = onSoundToggle
        )
        SettingsToggleItem(
            label = "Background Music",
            checked = uiState.musicEnabled,
            onCheckedChange = onMusicToggle
        )
        SettingsSectionHeader(
            title = "Feedback",
            modifier = Modifier.padding(top = SECTION_SPACING_DP.dp)
        )
        SettingsToggleItem(
            label = "Vibration",
            checked = uiState.vibrationEnabled,
            onCheckedChange = onVibrationToggle
        )
        SettingsSectionHeader(
            title = "Language",
            modifier = Modifier.padding(top = SECTION_SPACING_DP.dp)
        )
        LanguageSelector(
            selected = uiState.language,
            onSelect = onLanguageChange
        )
        SettingsSectionHeader(
            title = "Display",
            modifier = Modifier.padding(top = SECTION_SPACING_DP.dp)
        )
        SettingsToggleItem(
            label = "Dark Mode",
            checked = uiState.darkModeEnabled,
            onCheckedChange = onDarkModeToggle
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
    )
}

@Composable
private fun SettingsToggleItem(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun AiDifficultySelector(
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(CHIP_SPACING_DP.dp),
        modifier = modifier
    ) {
        aiDifficultyOptions.forEach { option ->
            FilterChip(
                selected = selected == option,
                onClick = { onSelect(option) },
                label = { Text(aiDifficultyLabels[option] ?: option) }
            )
        }
    }
}

@Composable
private fun LanguageSelector(
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(CHIP_SPACING_DP.dp),
        modifier = modifier
    ) {
        languageOptions.forEach { option ->
            FilterChip(
                selected = selected == option,
                onClick = { onSelect(option) },
                label = { Text(languageLabels[option] ?: option) }
            )
        }
    }
}

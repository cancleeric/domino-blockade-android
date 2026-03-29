package com.cancleeric.dominoblockade.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cancleeric.dominoblockade.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val soundEnabled: StateFlow<Boolean> = settingsRepository.soundEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val musicEnabled: StateFlow<Boolean> = settingsRepository.musicEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val vibrationEnabled: StateFlow<Boolean> = settingsRepository.vibrationEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val aiDifficulty: StateFlow<String> = settingsRepository.defaultAiDifficulty
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "medium")

    val language: StateFlow<String> = settingsRepository.language
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "zh-TW")

    val darkMode: StateFlow<String> = settingsRepository.darkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "system")

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setSoundEnabled(enabled) }
    }

    fun setMusicEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setMusicEnabled(enabled) }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setVibrationEnabled(enabled) }
    }

    fun setAiDifficulty(difficulty: String) {
        viewModelScope.launch { settingsRepository.setDefaultAiDifficulty(difficulty) }
    }

    fun setLanguage(language: String) {
        viewModelScope.launch { settingsRepository.setLanguage(language) }
    }

    fun setDarkMode(mode: String) {
        viewModelScope.launch { settingsRepository.setDarkMode(mode) }
    }
}

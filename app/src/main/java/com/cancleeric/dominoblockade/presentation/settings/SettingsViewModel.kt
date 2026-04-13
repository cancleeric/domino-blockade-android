package com.cancleeric.dominoblockade.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cancleeric.dominoblockade.domain.repository.GameSettingsRepository
import com.cancleeric.dominoblockade.domain.usecase.AdaptiveAiManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val adaptiveAiLevel: Int = 50,
    val language: String = "en",
    val darkModeEnabled: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: GameSettingsRepository,
    adaptiveAiManager: AdaptiveAiManager
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        combine(
            repository.soundEnabled,
            repository.musicEnabled,
            repository.vibrationEnabled
        ) { sound, music, vibration -> Triple(sound, music, vibration) },
        combine(
            repository.language,
            repository.darkModeEnabled
        ) { lang, dark -> lang to dark },
        adaptiveAiManager.currentLevel
    ) { (sound, music, vibration), (lang, dark), aiLevel ->
        SettingsUiState(sound, music, vibration, aiLevel, lang, dark)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SettingsUiState())

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setSoundEnabled(enabled) }
    }

    fun setMusicEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setMusicEnabled(enabled) }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setVibrationEnabled(enabled) }
    }

    fun setLanguage(language: String) {
        viewModelScope.launch { repository.setLanguage(language) }
    }

    fun setDarkModeEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setDarkModeEnabled(enabled) }
    }
}

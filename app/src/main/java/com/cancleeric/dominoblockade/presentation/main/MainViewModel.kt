package com.cancleeric.dominoblockade.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cancleeric.dominoblockade.domain.repository.GameSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

private const val STOP_TIMEOUT_MS = 5000L

@HiltViewModel
class MainViewModel @Inject constructor(
    settingsRepository: GameSettingsRepository
) : ViewModel() {

    val darkModeEnabled: StateFlow<Boolean> = settingsRepository.darkModeEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), false)
}

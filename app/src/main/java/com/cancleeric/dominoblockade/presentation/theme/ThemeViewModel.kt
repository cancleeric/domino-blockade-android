package com.cancleeric.dominoblockade.presentation.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cancleeric.dominoblockade.data.preferences.UserPreferencesRepository
import com.cancleeric.dominoblockade.domain.model.AppTheme
import com.cancleeric.dominoblockade.domain.model.DominoStyle
import com.cancleeric.dominoblockade.domain.model.ThemePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val repository: UserPreferencesRepository
) : ViewModel() {

    val themePreferences: StateFlow<ThemePreferences> = repository.themePreferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ThemePreferences()
        )

    fun setAppTheme(appTheme: AppTheme) {
        viewModelScope.launch { repository.setAppTheme(appTheme) }
    }

    fun setDominoStyle(dominoStyle: DominoStyle) {
        viewModelScope.launch { repository.setDominoStyle(dominoStyle) }
    }
}

package com.cancleeric.dominoblockade.presentation.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cancleeric.dominoblockade.domain.model.AppTheme
import com.cancleeric.dominoblockade.domain.model.DominoStyle
import com.cancleeric.dominoblockade.domain.repository.ThemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themeRepository: ThemeRepository
) : ViewModel() {

    val appTheme: StateFlow<AppTheme> = themeRepository.getAppTheme()
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppTheme.CLASSIC)

    val dominoStyle: StateFlow<DominoStyle> = themeRepository.getDominoStyle()
        .stateIn(viewModelScope, SharingStarted.Eagerly, DominoStyle.DOTS)

    fun selectTheme(theme: AppTheme) {
        viewModelScope.launch { themeRepository.setAppTheme(theme) }
    }

    fun selectDominoStyle(style: DominoStyle) {
        viewModelScope.launch { themeRepository.setDominoStyle(style) }
    }
}

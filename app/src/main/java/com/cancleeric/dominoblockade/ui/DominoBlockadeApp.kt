package com.cancleeric.dominoblockade.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cancleeric.dominoblockade.ui.screen.HomeScreen
import com.cancleeric.dominoblockade.ui.theme.DominoBlockadeTheme
import com.cancleeric.dominoblockade.ui.viewmodel.MainViewModel

@Composable
fun DominoBlockadeApp(
    windowSizeClass: WindowSizeClass,
    viewModel: MainViewModel = hiltViewModel()
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val isTablet = windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Medium

    DominoBlockadeTheme(themeMode = themeMode) {
        Surface(modifier = Modifier.fillMaxSize()) {
            HomeScreen(
                isTablet = isTablet,
                currentThemeMode = themeMode,
                onThemeModeChange = viewModel::setThemeMode
            )
        }
    }
}

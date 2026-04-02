package com.cancleeric.dominoblockade

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cancleeric.dominoblockade.presentation.navigation.AppNavigation
import com.cancleeric.dominoblockade.presentation.theme.ThemeViewModel
import com.cancleeric.dominoblockade.ui.theme.DominoBlockadeTheme
import com.cancleeric.dominoblockade.widget.QuickStartWidget.Companion.EXTRA_PLAYER_COUNT
import com.cancleeric.dominoblockade.widget.QuickStartWidget.Companion.EXTRA_QUICK_START
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val quickStartPlayerCount = if (intent.getBooleanExtra(EXTRA_QUICK_START, false)) {
            intent.getIntExtra(EXTRA_PLAYER_COUNT, -1)
        } else {
            -1
        }
        setContent {
            val appTheme by themeViewModel.appTheme.collectAsStateWithLifecycle()
            val dominoStyle by themeViewModel.dominoStyle.collectAsStateWithLifecycle()
            DominoBlockadeTheme(appTheme = appTheme, dominoStyle = dominoStyle) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation(
                        modifier = Modifier.padding(innerPadding),
                        quickStartPlayerCount = quickStartPlayerCount
                    )
                }
            }
        }
    }
}

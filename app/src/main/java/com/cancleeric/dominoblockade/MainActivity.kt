package com.cancleeric.dominoblockade

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.cancleeric.dominoblockade.presentation.navigation.AppNavigation
import com.cancleeric.dominoblockade.ui.theme.DominoBlockadeTheme
import com.cancleeric.dominoblockade.widget.QuickStartWidget.Companion.EXTRA_PLAYER_COUNT
import com.cancleeric.dominoblockade.widget.QuickStartWidget.Companion.EXTRA_QUICK_START
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
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
            DominoBlockadeTheme {
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

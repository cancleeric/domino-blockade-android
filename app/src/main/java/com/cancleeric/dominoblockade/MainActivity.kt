package com.cancleeric.dominoblockade

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cancleeric.dominoblockade.presentation.navigation.AppNavigation
import com.cancleeric.dominoblockade.presentation.theme.ThemeViewModel
import com.cancleeric.dominoblockade.ui.theme.DominoBlockadeTheme
import com.cancleeric.dominoblockade.ui.theme.LocalWindowSizeClass
import com.cancleeric.dominoblockade.widget.QuickStartWidget.Companion.EXTRA_PLAYER_COUNT
import com.cancleeric.dominoblockade.widget.QuickStartWidget.Companion.EXTRA_QUICK_START
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val themeViewModel: ThemeViewModel by viewModels()
    private val pendingDeepLink = mutableStateOf<String?>(null)

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val quickStartPlayerCount = if (intent.getBooleanExtra(EXTRA_QUICK_START, false)) {
            intent.getIntExtra(EXTRA_PLAYER_COUNT, -1)
        } else {
            -1
        }
        pendingDeepLink.value = intent?.dataString
        setContent {
            val appTheme by themeViewModel.appTheme.collectAsStateWithLifecycle()
            val dominoStyle by themeViewModel.dominoStyle.collectAsStateWithLifecycle()
            val dominoSkin by themeViewModel.dominoSkin.collectAsStateWithLifecycle()
            val windowSizeClass = calculateWindowSizeClass(this)
            DominoBlockadeTheme(appTheme = appTheme, dominoStyle = dominoStyle, dominoSkin = dominoSkin) {
                CompositionLocalProvider(LocalWindowSizeClass provides windowSizeClass) {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        AppNavigation(
                            modifier = Modifier.padding(innerPadding),
                            quickStartPlayerCount = quickStartPlayerCount,
                            incomingDeepLink = pendingDeepLink.value,
                            onDeepLinkHandled = { pendingDeepLink.value = null }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        pendingDeepLink.value = intent.dataString
    }
}

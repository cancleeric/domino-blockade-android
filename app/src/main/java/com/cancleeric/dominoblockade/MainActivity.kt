package com.cancleeric.dominoblockade

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.cancleeric.dominoblockade.presentation.navigation.AppNavigation
import com.cancleeric.dominoblockade.ui.theme.DominoBlockadeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install the splash screen BEFORE super.onCreate() so the system can
        // display it while the app is initialising.
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        // Attach a fade-out (logo fade-in → app fade-in) exit animation.
        // The SplashScreen icon fades out while the first app frame fades in.
        splashScreen.setOnExitAnimationListener { splashScreenView ->
            val fadeOut = ObjectAnimator.ofFloat(
                splashScreenView.view,
                View.ALPHA,
                1f,
                0f
            ).apply {
                duration = 400L
                interpolator = AccelerateDecelerateInterpolator()
                doOnEnd { splashScreenView.remove() }
            }
            fadeOut.start()
        }

        enableEdgeToEdge()
        setContent {
            DominoBlockadeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

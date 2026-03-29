package com.cancleeric.dominoblockade.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.cancleeric.dominoblockade.data.preferences.ThemeMode

private val LightColorScheme = lightColorScheme(
    primary = DominoPrimary,
    onPrimary = DominoOnPrimary,
    primaryContainer = DominoPrimaryVariant,
    secondary = DominoSecondary,
    onSecondary = DominoOnSecondary,
    secondaryContainer = DominoSecondaryVariant,
    background = DominoBackground,
    onBackground = DominoOnBackground,
    surface = DominoSurface,
    onSurface = DominoOnSurface,
    error = DominoError
)

private val DarkColorScheme = darkColorScheme(
    primary = DominoPrimaryVariant,
    onPrimary = DominoOnPrimary,
    primaryContainer = DominoPrimary,
    secondary = DominoSecondary,
    onSecondary = DominoOnSecondary,
    secondaryContainer = DominoSecondaryVariant,
    background = DominoBackgroundDark,
    onBackground = DominoOnBackgroundDark,
    surface = DominoSurfaceDark,
    onSurface = DominoOnSurfaceDark,
    error = DominoError
)

@Composable
fun DominoBlockadeTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val systemInDarkTheme = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> systemInDarkTheme
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

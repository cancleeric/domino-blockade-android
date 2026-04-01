package com.cancleeric.dominoblockade.ui.theme

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.cancleeric.dominoblockade.domain.model.AppTheme
import com.cancleeric.dominoblockade.domain.model.DominoStyle

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

// High-contrast scheme for visually impaired users (WCAG AAA)
private val HighContrastColorScheme = darkColorScheme(
    primary = HcPrimary,
    onPrimary = HcOnPrimary,
    primaryContainer = HcSurface,
    onPrimaryContainer = HcText,
    secondary = HcSecondary,
    onSecondary = HcOnSecondary,
    background = HcBackground,
    onBackground = HcText,
    surface = HcSurface,
    onSurface = HcText,
    error = HcError,
    onError = HcOnPrimary
)

private val ClassicColorScheme = lightColorScheme(
    primary = ClassicPrimary,
    secondary = ClassicSecondary,
    surface = ClassicTile,
    onSurface = ClassicDot,
    background = ClassicBoard,
    onBackground = Color.White
)

private val DarkAppColorScheme = darkColorScheme(
    primary = DarkPrimary,
    secondary = DarkSecondary,
    surface = DarkTile,
    onSurface = DarkDot,
    background = DarkBoard,
    onBackground = Color.White
)

private val WoodColorScheme = lightColorScheme(
    primary = WoodPrimary,
    secondary = WoodSecondary,
    surface = WoodTile,
    onSurface = WoodDot,
    background = WoodBoard,
    onBackground = Color.White
)

private val NeonColorScheme = darkColorScheme(
    primary = NeonPrimary,
    secondary = NeonSecondary,
    surface = NeonTile,
    onSurface = NeonDot,
    background = NeonBoard,
    onBackground = NeonPrimary
)

/**
 * Composition local for high-contrast mode state.
 * Read in components to adjust colours for low-vision users.
 */
val LocalHighContrast = staticCompositionLocalOf { false }

/**
 * Composition local providing the board background colour for the current theme.
 */
val LocalBoardBackground = staticCompositionLocalOf { ClassicBoard }

/**
 * Composition local providing the active domino rendering style.
 */
val LocalDominoStyle = staticCompositionLocalOf { DominoStyle.DOTS }

@Suppress("CyclomaticComplexMethod")
private fun resolveColorSchemeAndBoard(
    appTheme: AppTheme,
    darkTheme: Boolean,
    dynamicColor: Boolean,
    highContrast: Boolean,
    context: Context
): Pair<ColorScheme, Color> = when {
    highContrast -> HighContrastColorScheme to HcBackground
    appTheme == AppTheme.CLASSIC -> ClassicColorScheme to ClassicBoard
    appTheme == AppTheme.DARK -> DarkAppColorScheme to DarkBoard
    appTheme == AppTheme.WOOD -> WoodColorScheme to WoodBoard
    appTheme == AppTheme.NEON -> NeonColorScheme to NeonBoard
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val scheme = if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        scheme to scheme.background
    }
    darkTheme -> DarkColorScheme to DarkColorScheme.background
    else -> LightColorScheme to LightColorScheme.background
}

@Composable
fun DominoBlockadeTheme(
    appTheme: AppTheme = AppTheme.CLASSIC,
    dominoStyle: DominoStyle = DominoStyle.DOTS,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    highContrast: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val (colorScheme, boardBackground) = resolveColorSchemeAndBoard(
        appTheme, darkTheme, dynamicColor, highContrast, context
    )
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                !darkTheme && !highContrast && appTheme != AppTheme.DARK && appTheme != AppTheme.NEON
        }
    }

    CompositionLocalProvider(
        LocalHighContrast provides highContrast,
        LocalBoardBackground provides boardBackground,
        LocalDominoStyle provides dominoStyle
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

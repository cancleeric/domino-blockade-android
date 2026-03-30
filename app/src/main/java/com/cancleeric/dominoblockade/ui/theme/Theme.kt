package com.cancleeric.dominoblockade.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.cancleeric.dominoblockade.domain.model.AppTheme

private val ClassicColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    secondary = DarkSecondary,
    tertiary = DarkTertiary,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface
)

private val WoodColorScheme = lightColorScheme(
    primary = WoodPrimary,
    secondary = WoodSecondary,
    tertiary = WoodTertiary,
    background = WoodBackground,
    surface = WoodSurface,
    onBackground = WoodOnBackground,
    onSurface = WoodOnSurface
)

private val NeonColorScheme = darkColorScheme(
    primary = NeonPrimary,
    secondary = NeonSecondary,
    tertiary = NeonTertiary,
    background = NeonBackground,
    surface = NeonSurface,
    onBackground = NeonOnBackground,
    onSurface = NeonOnSurface
)

@Composable
fun DominoBlockadeTheme(
    appTheme: AppTheme = AppTheme.CLASSIC,
    content: @Composable () -> Unit
) {
    val colorScheme = when (appTheme) {
        AppTheme.CLASSIC -> ClassicColorScheme
        AppTheme.DARK -> DarkColorScheme
        AppTheme.WOOD -> WoodColorScheme
        AppTheme.NEON -> NeonColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                appTheme == AppTheme.CLASSIC || appTheme == AppTheme.WOOD
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

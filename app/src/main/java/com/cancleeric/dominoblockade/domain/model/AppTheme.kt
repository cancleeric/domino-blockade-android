package com.cancleeric.dominoblockade.domain.model

enum class AppTheme(val displayName: String) {
    CLASSIC("Classic"),
    DARK("Dark"),
    WOOD("Wood"),
    NEON("Neon")
}

enum class DominoStyle(val displayName: String) {
    DOTS("Dots"),
    NUMBERS("Numbers"),
    SYMBOLS("Symbols")
}

data class ThemePreferences(
    val appTheme: AppTheme = AppTheme.CLASSIC,
    val dominoStyle: DominoStyle = DominoStyle.DOTS
)

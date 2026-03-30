package com.cancleeric.dominoblockade.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class AppThemeTest {

    @Test
    fun `AppTheme has exactly four entries`() {
        assertEquals(4, AppTheme.entries.size)
    }

    @Test
    fun `AppTheme entries contain required themes`() {
        val names = AppTheme.entries.map { it.name }
        assert(names.contains("CLASSIC"))
        assert(names.contains("DARK"))
        assert(names.contains("WOOD"))
        assert(names.contains("NEON"))
    }

    @Test
    fun `DominoStyle has exactly three entries`() {
        assertEquals(3, DominoStyle.entries.size)
    }

    @Test
    fun `DominoStyle entries contain required styles`() {
        val names = DominoStyle.entries.map { it.name }
        assert(names.contains("DOTS"))
        assert(names.contains("NUMBERS"))
        assert(names.contains("SYMBOLS"))
    }

    @Test
    fun `ThemePreferences defaults are CLASSIC and DOTS`() {
        val prefs = ThemePreferences()
        assertEquals(AppTheme.CLASSIC, prefs.appTheme)
        assertEquals(DominoStyle.DOTS, prefs.dominoStyle)
    }

    @Test
    fun `ThemePreferences valueOf round trip`() {
        AppTheme.entries.forEach { theme ->
            assertEquals(theme, AppTheme.valueOf(theme.name))
        }
        DominoStyle.entries.forEach { style ->
            assertEquals(style, DominoStyle.valueOf(style.name))
        }
    }
}

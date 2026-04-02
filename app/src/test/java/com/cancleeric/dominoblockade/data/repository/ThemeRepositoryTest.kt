package com.cancleeric.dominoblockade.data.repository

import com.cancleeric.dominoblockade.data.local.dao.ThemeDao
import com.cancleeric.dominoblockade.data.local.entity.ThemeEntity
import com.cancleeric.dominoblockade.domain.model.AppTheme
import com.cancleeric.dominoblockade.domain.model.DominoStyle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ThemeRepositoryTest {

    private class FakeThemeDao : ThemeDao {
        private val state = MutableStateFlow<ThemeEntity?>(null)

        override fun getTheme(): Flow<ThemeEntity?> = state

        override suspend fun getThemeOnce(): ThemeEntity? = state.value

        override suspend fun upsertTheme(theme: ThemeEntity) {
            state.value = theme
        }
    }

    private lateinit var dao: FakeThemeDao
    private lateinit var repository: ThemeRepositoryImpl

    @Before
    fun setUp() {
        dao = FakeThemeDao()
        repository = ThemeRepositoryImpl(dao)
    }

    @Test
    fun `getAppTheme returns CLASSIC by default when no row exists`() = runTest {
        val result = repository.getAppTheme().first()
        assertEquals(AppTheme.CLASSIC, result)
    }

    @Test
    fun `getDominoStyle returns DOTS by default when no row exists`() = runTest {
        val result = repository.getDominoStyle().first()
        assertEquals(DominoStyle.DOTS, result)
    }

    @Test
    fun `setAppTheme persists the selected theme`() = runTest {
        repository.setAppTheme(AppTheme.NEON)
        val result = repository.getAppTheme().first()
        assertEquals(AppTheme.NEON, result)
    }

    @Test
    fun `setDominoStyle persists the selected style`() = runTest {
        repository.setDominoStyle(DominoStyle.NUMBERS)
        val result = repository.getDominoStyle().first()
        assertEquals(DominoStyle.NUMBERS, result)
    }

    @Test
    fun `setAppTheme preserves dominoStyle`() = runTest {
        repository.setDominoStyle(DominoStyle.NUMBERS)
        repository.setAppTheme(AppTheme.WOOD)
        val style = repository.getDominoStyle().first()
        val theme = repository.getAppTheme().first()
        assertEquals(DominoStyle.NUMBERS, style)
        assertEquals(AppTheme.WOOD, theme)
    }

    @Test
    fun `setDominoStyle preserves appTheme`() = runTest {
        repository.setAppTheme(AppTheme.DARK)
        repository.setDominoStyle(DominoStyle.NUMBERS)
        val theme = repository.getAppTheme().first()
        val style = repository.getDominoStyle().first()
        assertEquals(AppTheme.DARK, theme)
        assertEquals(DominoStyle.NUMBERS, style)
    }

    @Test
    fun `getAppTheme returns CLASSIC for unknown stored value`() = runTest {
        dao.upsertTheme(ThemeEntity(appTheme = "UNKNOWN_VALUE", dominoStyle = DominoStyle.DOTS.name))
        val result = repository.getAppTheme().first()
        assertEquals(AppTheme.CLASSIC, result)
    }
}

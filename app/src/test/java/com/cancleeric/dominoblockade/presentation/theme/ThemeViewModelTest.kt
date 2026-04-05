package com.cancleeric.dominoblockade.presentation.theme

import com.cancleeric.dominoblockade.domain.model.AppTheme
import com.cancleeric.dominoblockade.domain.model.DominoStyle
import com.cancleeric.dominoblockade.domain.repository.ThemeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ThemeViewModelTest {

    private val appThemeFlow = MutableStateFlow(AppTheme.CLASSIC)
    private val dominoStyleFlow = MutableStateFlow(DominoStyle.DOTS)

    private val fakeRepository = object : ThemeRepository {
        override fun getAppTheme(): Flow<AppTheme> = appThemeFlow
        override fun getDominoStyle(): Flow<DominoStyle> = dominoStyleFlow
        override suspend fun setAppTheme(theme: AppTheme) { appThemeFlow.value = theme }
        override suspend fun setDominoStyle(style: DominoStyle) { dominoStyleFlow.value = style }
    }

    private lateinit var viewModel: ThemeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = ThemeViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial appTheme is CLASSIC`() = runTest {
        assertEquals(AppTheme.CLASSIC, viewModel.appTheme.value)
    }

    @Test
    fun `initial dominoStyle is DOTS`() = runTest {
        assertEquals(DominoStyle.DOTS, viewModel.dominoStyle.value)
    }

    @Test
    fun `selectTheme updates appTheme state`() = runTest {
        viewModel.selectTheme(AppTheme.DARK)
        assertEquals(AppTheme.DARK, viewModel.appTheme.value)
    }

    @Test
    fun `selectTheme NEON updates appTheme to NEON`() = runTest {
        viewModel.selectTheme(AppTheme.NEON)
        assertEquals(AppTheme.NEON, viewModel.appTheme.value)
    }

    @Test
    fun `selectTheme WOOD updates appTheme to WOOD`() = runTest {
        viewModel.selectTheme(AppTheme.WOOD)
        assertEquals(AppTheme.WOOD, viewModel.appTheme.value)
    }

    @Test
    fun `selectDominoStyle updates dominoStyle state`() = runTest {
        viewModel.selectDominoStyle(DominoStyle.NUMBERS)
        assertEquals(DominoStyle.NUMBERS, viewModel.dominoStyle.value)
    }

    @Test
    fun `selectDominoStyle back to DOTS updates state`() = runTest {
        viewModel.selectDominoStyle(DominoStyle.NUMBERS)
        viewModel.selectDominoStyle(DominoStyle.DOTS)
        assertEquals(DominoStyle.DOTS, viewModel.dominoStyle.value)
    }

    @Test
    fun `appTheme reflects external repository change`() = runTest {
        appThemeFlow.value = AppTheme.WOOD
        assertEquals(AppTheme.WOOD, viewModel.appTheme.value)
    }

    @Test
    fun `dominoStyle reflects external repository change`() = runTest {
        dominoStyleFlow.value = DominoStyle.NUMBERS
        assertEquals(DominoStyle.NUMBERS, viewModel.dominoStyle.value)
    }
}

package com.cancleeric.dominoblockade.presentation.theme

import app.cash.turbine.test
import com.cancleeric.dominoblockade.domain.model.AppTheme
import com.cancleeric.dominoblockade.domain.model.DominoStyle
import com.cancleeric.dominoblockade.domain.repository.ThemeRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ThemeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val themeFlow = MutableStateFlow(AppTheme.CLASSIC)
    private val styleFlow = MutableStateFlow(DominoStyle.DOTS)
    private val themeRepository: ThemeRepository = mockk(relaxed = true) {
        every { getAppTheme() } returns themeFlow
        every { getDominoStyle() } returns styleFlow
    }

    private lateinit var viewModel: ThemeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ThemeViewModel(themeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `appTheme initial value is CLASSIC`() = runTest(testDispatcher) {
        advanceUntilIdle()
        assertEquals(AppTheme.CLASSIC, viewModel.appTheme.value)
    }

    @Test
    fun `dominoStyle initial value is DOTS`() = runTest(testDispatcher) {
        advanceUntilIdle()
        assertEquals(DominoStyle.DOTS, viewModel.dominoStyle.value)
    }

    @Test
    fun `appTheme updates when repository emits new theme`() = runTest(testDispatcher) {
        viewModel.appTheme.test {
            assertEquals(AppTheme.CLASSIC, awaitItem())
            themeFlow.value = AppTheme.DARK
            assertEquals(AppTheme.DARK, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `dominoStyle updates when repository emits new style`() = runTest(testDispatcher) {
        viewModel.dominoStyle.test {
            assertEquals(DominoStyle.DOTS, awaitItem())
            styleFlow.value = DominoStyle.NUMBERS
            assertEquals(DominoStyle.NUMBERS, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `selectTheme calls repository setAppTheme`() = runTest(testDispatcher) {
        viewModel.selectTheme(AppTheme.NEON)
        advanceUntilIdle()
        coVerify { themeRepository.setAppTheme(AppTheme.NEON) }
    }

    @Test
    fun `selectDominoStyle calls repository setDominoStyle`() = runTest(testDispatcher) {
        viewModel.selectDominoStyle(DominoStyle.NUMBERS)
        advanceUntilIdle()
        coVerify { themeRepository.setDominoStyle(DominoStyle.NUMBERS) }
    }

    @Test
    fun `selectTheme with WOOD theme calls repository`() = runTest(testDispatcher) {
        viewModel.selectTheme(AppTheme.WOOD)
        advanceUntilIdle()
        coVerify { themeRepository.setAppTheme(AppTheme.WOOD) }
    }
}

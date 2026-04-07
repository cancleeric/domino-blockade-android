package com.cancleeric.dominoblockade.presentation.main

import app.cash.turbine.test
import com.cancleeric.dominoblockade.domain.repository.GameSettingsRepository
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val darkModeFlow = MutableStateFlow(false)
    private val settingsRepository: GameSettingsRepository = mockk(relaxed = true) {
        every { darkModeEnabled } returns darkModeFlow
    }

    private lateinit var viewModel: MainViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = MainViewModel(settingsRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `darkModeEnabled initial value is false`() = runTest(testDispatcher) {
        advanceUntilIdle()
        assertFalse(viewModel.darkModeEnabled.value)
    }

    @Test
    fun `darkModeEnabled updates when repository emits true`() = runTest(testDispatcher) {
        darkModeFlow.value = true
        advanceUntilIdle()
        assertTrue(viewModel.darkModeEnabled.value)
    }

    @Test
    fun `darkModeEnabled emits via turbine`() = runTest(testDispatcher) {
        viewModel.darkModeEnabled.test {
            assertFalse(awaitItem())
            darkModeFlow.value = true
            assertTrue(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}

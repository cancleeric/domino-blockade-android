package com.cancleeric.dominoblockade.presentation.main

import com.cancleeric.dominoblockade.domain.repository.GameSettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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

    private val darkModeFlow = MutableStateFlow(false)

    private val fakeRepository = object : GameSettingsRepository {
        override val soundEnabled: Flow<Boolean> = MutableStateFlow(true)
        override val musicEnabled: Flow<Boolean> = MutableStateFlow(true)
        override val vibrationEnabled: Flow<Boolean> = MutableStateFlow(true)
        override val defaultAiDifficulty: Flow<String> = MutableStateFlow("medium")
        override val defaultPlayerCount: Flow<Int> = MutableStateFlow(2)
        override val language: Flow<String> = MutableStateFlow("en")
        override val darkModeEnabled: Flow<Boolean> = darkModeFlow
        override suspend fun setSoundEnabled(enabled: Boolean) = Unit
        override suspend fun setMusicEnabled(enabled: Boolean) = Unit
        override suspend fun setVibrationEnabled(enabled: Boolean) = Unit
        override suspend fun setDefaultAiDifficulty(difficulty: String) = Unit
        override suspend fun setDefaultPlayerCount(count: Int) = Unit
        override suspend fun setLanguage(language: String) = Unit
        override suspend fun setDarkModeEnabled(enabled: Boolean) { darkModeFlow.value = enabled }
    }

    private lateinit var viewModel: MainViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = MainViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial darkModeEnabled is false`() = runTest {
        assertFalse(viewModel.darkModeEnabled.value)
    }

    @Test
    fun `darkModeEnabled updates when repository emits true`() = runTest {
        darkModeFlow.value = true
        assertTrue(viewModel.darkModeEnabled.value)
    }

    @Test
    fun `darkModeEnabled reverts when repository emits false again`() = runTest {
        darkModeFlow.value = true
        darkModeFlow.value = false
        assertFalse(viewModel.darkModeEnabled.value)
    }
}

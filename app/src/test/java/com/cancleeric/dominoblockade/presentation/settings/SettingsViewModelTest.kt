package com.cancleeric.dominoblockade.presentation.settings

import com.cancleeric.dominoblockade.data.local.entity.AdaptiveAiGameEntity
import com.cancleeric.dominoblockade.domain.model.GameMode
import com.cancleeric.dominoblockade.domain.repository.AdaptiveAiRepository
import com.cancleeric.dominoblockade.domain.repository.GameSettingsRepository
import com.cancleeric.dominoblockade.domain.usecase.AdaptiveAiManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeGameSettingsRepository : GameSettingsRepository {
        private val _soundEnabled = MutableStateFlow(true)
        private val _musicEnabled = MutableStateFlow(true)
        private val _vibrationEnabled = MutableStateFlow(true)
        private val _aiDifficulty = MutableStateFlow("medium")
        private val _playerCount = MutableStateFlow(2)
        private val _language = MutableStateFlow("en")
        private val _darkModeEnabled = MutableStateFlow(false)

        override val soundEnabled: Flow<Boolean> = _soundEnabled
        override val musicEnabled: Flow<Boolean> = _musicEnabled
        override val vibrationEnabled: Flow<Boolean> = _vibrationEnabled
        override val defaultAiDifficulty: Flow<String> = _aiDifficulty
        override val defaultPlayerCount: Flow<Int> = _playerCount
        override val language: Flow<String> = _language
        override val darkModeEnabled: Flow<Boolean> = _darkModeEnabled

        override suspend fun setSoundEnabled(enabled: Boolean) { _soundEnabled.value = enabled }
        override suspend fun setMusicEnabled(enabled: Boolean) { _musicEnabled.value = enabled }
        override suspend fun setVibrationEnabled(enabled: Boolean) { _vibrationEnabled.value = enabled }
        override suspend fun setDefaultAiDifficulty(difficulty: String) { _aiDifficulty.value = difficulty }
        override suspend fun setDefaultPlayerCount(count: Int) { _playerCount.value = count }
        override suspend fun setLanguage(language: String) { _language.value = language }
        override suspend fun setDarkModeEnabled(enabled: Boolean) { _darkModeEnabled.value = enabled }
    }

    private class FakeAdaptiveAiRepository : AdaptiveAiRepository {
        private val _currentLevel = MutableStateFlow(50)
        override val currentLevel: Flow<Int> = _currentLevel
        override suspend fun getCurrentLevel(): Int = _currentLevel.value
        override suspend fun setCurrentLevel(level: Int) {
            _currentLevel.value = level
        }

        override suspend fun insertGameResult(gameMode: GameMode, playerWon: Boolean) = Unit

        override suspend fun getRecentGames(gameModes: List<GameMode>, limit: Int): List<AdaptiveAiGameEntity> =
            emptyList()
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial uiState has default values`() = runTest(testDispatcher) {
        val viewModel = SettingsViewModel(
            FakeGameSettingsRepository(),
            AdaptiveAiManager(FakeAdaptiveAiRepository())
        )
        advanceUntilIdle()
        val state = viewModel.uiState.first()
        assertTrue(state.soundEnabled)
        assertTrue(state.musicEnabled)
        assertTrue(state.vibrationEnabled)
        assertEquals(50, state.adaptiveAiLevel)
        assertEquals("en", state.language)
        assertFalse(state.darkModeEnabled)
    }

    @Test
    fun `setSoundEnabled updates soundEnabled in state`() = runTest(testDispatcher) {
        val viewModel = SettingsViewModel(
            FakeGameSettingsRepository(),
            AdaptiveAiManager(FakeAdaptiveAiRepository())
        )
        advanceUntilIdle()
        viewModel.setSoundEnabled(false)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.first().soundEnabled)
    }

    @Test
    fun `setMusicEnabled updates musicEnabled in state`() = runTest(testDispatcher) {
        val viewModel = SettingsViewModel(
            FakeGameSettingsRepository(),
            AdaptiveAiManager(FakeAdaptiveAiRepository())
        )
        advanceUntilIdle()
        viewModel.setMusicEnabled(false)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.first().musicEnabled)
    }

    @Test
    fun `setVibrationEnabled updates vibrationEnabled in state`() = runTest(testDispatcher) {
        val viewModel = SettingsViewModel(
            FakeGameSettingsRepository(),
            AdaptiveAiManager(FakeAdaptiveAiRepository())
        )
        advanceUntilIdle()
        viewModel.setVibrationEnabled(false)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.first().vibrationEnabled)
    }

    @Test
    fun `setLanguage updates language in state`() = runTest(testDispatcher) {
        val viewModel = SettingsViewModel(
            FakeGameSettingsRepository(),
            AdaptiveAiManager(FakeAdaptiveAiRepository())
        )
        advanceUntilIdle()
        viewModel.setLanguage("zh-TW")
        advanceUntilIdle()
        assertEquals("zh-TW", viewModel.uiState.first().language)
    }

    @Test
    fun `setDarkModeEnabled updates darkModeEnabled in state`() = runTest(testDispatcher) {
        val viewModel = SettingsViewModel(
            FakeGameSettingsRepository(),
            AdaptiveAiManager(FakeAdaptiveAiRepository())
        )
        advanceUntilIdle()
        viewModel.setDarkModeEnabled(true)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.first().darkModeEnabled)
    }
}

package com.cancleeric.dominoblockade.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.cancleeric.dominoblockade.domain.repository.GameSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private const val DEFAULT_AI_DIFFICULTY = "medium"
private const val DEFAULT_PLAYER_COUNT = 2
private const val DEFAULT_LANGUAGE = "en"

@Singleton
class GameSettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : GameSettingsRepository {

    private object Keys {
        val soundEnabled = booleanPreferencesKey("sound_enabled")
        val musicEnabled = booleanPreferencesKey("music_enabled")
        val vibrationEnabled = booleanPreferencesKey("vibration_enabled")
        val defaultAiDifficulty = stringPreferencesKey("default_ai_difficulty")
        val defaultPlayerCount = intPreferencesKey("default_player_count")
        val language = stringPreferencesKey("language")
        val darkModeEnabled = booleanPreferencesKey("dark_mode_enabled")
    }

    override val soundEnabled: Flow<Boolean> =
        dataStore.data.map { it[Keys.soundEnabled] ?: true }

    override val musicEnabled: Flow<Boolean> =
        dataStore.data.map { it[Keys.musicEnabled] ?: true }

    override val vibrationEnabled: Flow<Boolean> =
        dataStore.data.map { it[Keys.vibrationEnabled] ?: true }

    override val defaultAiDifficulty: Flow<String> =
        dataStore.data.map { it[Keys.defaultAiDifficulty] ?: DEFAULT_AI_DIFFICULTY }

    override val defaultPlayerCount: Flow<Int> =
        dataStore.data.map { it[Keys.defaultPlayerCount] ?: DEFAULT_PLAYER_COUNT }

    override val language: Flow<String> =
        dataStore.data.map { it[Keys.language] ?: DEFAULT_LANGUAGE }

    override val darkModeEnabled: Flow<Boolean> =
        dataStore.data.map { it[Keys.darkModeEnabled] ?: false }

    override suspend fun setSoundEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.soundEnabled] = enabled }
    }

    override suspend fun setMusicEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.musicEnabled] = enabled }
    }

    override suspend fun setVibrationEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.vibrationEnabled] = enabled }
    }

    override suspend fun setDefaultAiDifficulty(difficulty: String) {
        dataStore.edit { it[Keys.defaultAiDifficulty] = difficulty }
    }

    override suspend fun setDefaultPlayerCount(count: Int) {
        dataStore.edit { it[Keys.defaultPlayerCount] = count }
    }

    override suspend fun setLanguage(language: String) {
        dataStore.edit { it[Keys.language] = language }
    }

    override suspend fun setDarkModeEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.darkModeEnabled] = enabled }
    }
}

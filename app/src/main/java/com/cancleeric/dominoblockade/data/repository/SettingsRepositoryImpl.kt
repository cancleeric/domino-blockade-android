package com.cancleeric.dominoblockade.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.cancleeric.dominoblockade.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    private object Keys {
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val MUSIC_ENABLED = booleanPreferencesKey("music_enabled")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val DEFAULT_AI_DIFFICULTY = stringPreferencesKey("default_ai_difficulty")
        val DEFAULT_PLAYER_COUNT = intPreferencesKey("default_player_count")
    }

    override val soundEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.SOUND_ENABLED] ?: true
    }

    override val musicEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.MUSIC_ENABLED] ?: true
    }

    override val vibrationEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.VIBRATION_ENABLED] ?: true
    }

    override val defaultAiDifficulty: Flow<String> = dataStore.data.map { prefs ->
        prefs[Keys.DEFAULT_AI_DIFFICULTY] ?: "medium"
    }

    override val defaultPlayerCount: Flow<Int> = dataStore.data.map { prefs ->
        prefs[Keys.DEFAULT_PLAYER_COUNT] ?: 2
    }

    override suspend fun setSoundEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[Keys.SOUND_ENABLED] = enabled }
    }

    override suspend fun setMusicEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[Keys.MUSIC_ENABLED] = enabled }
    }

    override suspend fun setVibrationEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[Keys.VIBRATION_ENABLED] = enabled }
    }

    override suspend fun setDefaultAiDifficulty(difficulty: String) {
        dataStore.edit { prefs -> prefs[Keys.DEFAULT_AI_DIFFICULTY] = difficulty }
    }

    override suspend fun setDefaultPlayerCount(count: Int) {
        dataStore.edit { prefs -> prefs[Keys.DEFAULT_PLAYER_COUNT] = count }
    }
}

package com.cancleeric.dominoblockade.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        private val MUSIC_ENABLED = booleanPreferencesKey("music_enabled")
        private val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
    }

    val soundEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[SOUND_ENABLED] ?: true }

    val musicEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[MUSIC_ENABLED] ?: true }

    val vibrationEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[VIBRATION_ENABLED] ?: true }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SOUND_ENABLED] = enabled
        }
    }

    suspend fun setMusicEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[MUSIC_ENABLED] = enabled
        }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[VIBRATION_ENABLED] = enabled
        }
    }
}

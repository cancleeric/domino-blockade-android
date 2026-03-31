package com.cancleeric.dominoblockade.data.preferences

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

private val Context.audioDataStore: DataStore<Preferences> by preferencesDataStore("audio_prefs")

@Singleton
class AudioPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val MUSIC_ENABLED = booleanPreferencesKey("music_enabled")
        val HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
    }

    val soundEnabled: Flow<Boolean> = context.audioDataStore.data
        .map { prefs -> prefs[Keys.SOUND_ENABLED] ?: true }

    val musicEnabled: Flow<Boolean> = context.audioDataStore.data
        .map { prefs -> prefs[Keys.MUSIC_ENABLED] ?: true }

    val hapticEnabled: Flow<Boolean> = context.audioDataStore.data
        .map { prefs -> prefs[Keys.HAPTIC_ENABLED] ?: true }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.audioDataStore.edit { prefs -> prefs[Keys.SOUND_ENABLED] = enabled }
    }

    suspend fun setMusicEnabled(enabled: Boolean) {
        context.audioDataStore.edit { prefs -> prefs[Keys.MUSIC_ENABLED] = enabled }
    }

    suspend fun setHapticEnabled(enabled: Boolean) {
        context.audioDataStore.edit { prefs -> prefs[Keys.HAPTIC_ENABLED] = enabled }
    }
}

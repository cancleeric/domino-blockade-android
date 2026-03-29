package com.cancleeric.dominoblockade.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val soundEnabled: Flow<Boolean>
    val musicEnabled: Flow<Boolean>
    val vibrationEnabled: Flow<Boolean>
    val defaultAiDifficulty: Flow<String>
    val defaultPlayerCount: Flow<Int>
    val language: Flow<String>
    val darkMode: Flow<String>

    suspend fun setSoundEnabled(enabled: Boolean)
    suspend fun setMusicEnabled(enabled: Boolean)
    suspend fun setVibrationEnabled(enabled: Boolean)
    suspend fun setDefaultAiDifficulty(difficulty: String)
    suspend fun setDefaultPlayerCount(count: Int)
    suspend fun setLanguage(language: String)
    suspend fun setDarkMode(mode: String)
}

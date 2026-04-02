package com.cancleeric.dominoblockade.domain.repository

import kotlinx.coroutines.flow.Flow

interface GameSettingsRepository {
    val soundEnabled: Flow<Boolean>
    val musicEnabled: Flow<Boolean>
    val vibrationEnabled: Flow<Boolean>
    val defaultAiDifficulty: Flow<String>
    val defaultPlayerCount: Flow<Int>
    val language: Flow<String>
    val darkModeEnabled: Flow<Boolean>

    suspend fun setSoundEnabled(enabled: Boolean)
    suspend fun setMusicEnabled(enabled: Boolean)
    suspend fun setVibrationEnabled(enabled: Boolean)
    suspend fun setDefaultAiDifficulty(difficulty: String)
    suspend fun setDefaultPlayerCount(count: Int)
    suspend fun setLanguage(language: String)
    suspend fun setDarkModeEnabled(enabled: Boolean)
}

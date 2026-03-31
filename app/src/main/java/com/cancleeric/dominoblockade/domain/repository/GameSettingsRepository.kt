package com.cancleeric.dominoblockade.domain.repository

import kotlinx.coroutines.flow.Flow

interface GameSettingsRepository {
    val soundEnabled: Flow<Boolean>
    val musicEnabled: Flow<Boolean>
    val vibrationEnabled: Flow<Boolean>
    val defaultAiDifficulty: Flow<String>
    val defaultPlayerCount: Flow<Int>

    suspend fun setSoundEnabled(enabled: Boolean)
    suspend fun setMusicEnabled(enabled: Boolean)
    suspend fun setVibrationEnabled(enabled: Boolean)
    suspend fun setDefaultAiDifficulty(difficulty: String)
    suspend fun setDefaultPlayerCount(count: Int)
}

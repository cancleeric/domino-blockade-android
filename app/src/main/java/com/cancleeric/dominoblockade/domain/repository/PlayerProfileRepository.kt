package com.cancleeric.dominoblockade.domain.repository

import com.cancleeric.dominoblockade.domain.model.PlayerProfile
import kotlinx.coroutines.flow.Flow

interface PlayerProfileRepository {
    fun getProfile(): Flow<PlayerProfile>
    suspend fun saveProfile(profile: PlayerProfile)
}

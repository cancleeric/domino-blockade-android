package com.cancleeric.dominoblockade.data.repository

import com.cancleeric.dominoblockade.data.local.dao.PlayerProfileDao
import com.cancleeric.dominoblockade.data.local.entity.PlayerProfileEntity
import com.cancleeric.dominoblockade.domain.model.PlayerProfile
import com.cancleeric.dominoblockade.domain.repository.PlayerProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PlayerProfileRepositoryImpl @Inject constructor(
    private val dao: PlayerProfileDao
) : PlayerProfileRepository {

    override fun getProfile(): Flow<PlayerProfile> =
        dao.getProfile().map { entity -> entity?.toModel() ?: PlayerProfile() }

    override suspend fun saveProfile(profile: PlayerProfile) {
        dao.upsert(profile.toEntity())
    }
}

private fun PlayerProfileEntity.toModel() = PlayerProfile(
    id = id,
    playerName = playerName,
    avatarEmoji = avatarEmoji
)

private fun PlayerProfile.toEntity() = PlayerProfileEntity(
    id = id,
    playerName = playerName,
    avatarEmoji = avatarEmoji
)

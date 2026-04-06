package com.cancleeric.dominoblockade.domain.model

data class PlayerProfile(
    val id: Int = DEFAULT_PROFILE_ID,
    val playerName: String = DEFAULT_NAME,
    val avatarEmoji: String = DEFAULT_AVATAR
) {
    companion object {
        const val DEFAULT_PROFILE_ID = 1
        const val DEFAULT_NAME = "Player"
        const val DEFAULT_AVATAR = "🎮"
    }
}

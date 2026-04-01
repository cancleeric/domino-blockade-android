package com.cancleeric.dominoblockade.domain.model

data class LeaderboardEntry(
    val userId: String = "",
    val displayName: String = "",
    val highScore: Int = 0,
    val totalWins: Int = 0,
    val platform: String = PLATFORM_ANDROID,
    val lastUpdated: Long = 0L
) {
    companion object {
        const val PLATFORM_ANDROID = "android"
        const val PLATFORM_IOS = "ios"
    }
}

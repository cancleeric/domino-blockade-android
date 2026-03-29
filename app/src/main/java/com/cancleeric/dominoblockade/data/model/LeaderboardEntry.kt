package com.cancleeric.dominoblockade.data.model

/**
 * Leaderboard entry stored in Firestore under leaderboard/domino_blockade/{userId}.
 * Compatible with iOS LifeSnap Firestore structure.
 */
data class LeaderboardEntry(
    val userId: String = "",
    val displayName: String = "",
    val highScore: Int = 0,
    val totalWins: Int = 0,
    /** "android" or "ios" */
    val platform: String = "android",
    val lastUpdated: Long = System.currentTimeMillis()
)

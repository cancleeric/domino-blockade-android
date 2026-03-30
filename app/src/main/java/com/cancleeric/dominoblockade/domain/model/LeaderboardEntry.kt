package com.cancleeric.dominoblockade.domain.model

data class LeaderboardEntry(
    val rank: Int,
    val uid: String,
    val displayName: String,
    val eloRating: Int,
    val wins: Int,
    val winRate: Float
)

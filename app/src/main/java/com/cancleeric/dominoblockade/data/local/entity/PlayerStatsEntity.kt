package com.cancleeric.dominoblockade.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_stats")
data class PlayerStatsEntity(
    @PrimaryKey val playerName: String,
    val totalGames: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val totalScore: Int = 0,
    val highestScore: Int = 0,
    val blockedWins: Int = 0
)

package com.cancleeric.dominoblockade.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_replays")
data class GameReplayEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playerCount: Int,
    val winnerName: String,
    val isBlocked: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

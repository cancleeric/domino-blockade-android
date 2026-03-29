package com.cancleeric.dominoblockade.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_records")
data class GameRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playerCount: Int,
    val winnerName: String,
    val winnerScore: Int,
    val gameMode: String,
    val aiDifficulty: String?,
    val isBlocked: Boolean,
    val durationSeconds: Int,
    val timestamp: Long = System.currentTimeMillis()
)

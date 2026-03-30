package com.cancleeric.dominoblockade.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_records")
data class GameRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val playerCount: Int = 2,
    val winnerName: String = "",
    val difficulty: String = "EASY",
    val isWin: Boolean = false,
    val playerScore: Int = 0,
    val opponentScore: Int = 0,
    val durationSeconds: Int = 0,
    val aiName: String = "AI"
)

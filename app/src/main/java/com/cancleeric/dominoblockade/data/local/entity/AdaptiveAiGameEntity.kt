package com.cancleeric.dominoblockade.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "adaptive_ai_games")
data class AdaptiveAiGameEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gameMode: String,
    val playerWon: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

package com.cancleeric.dominoblockade.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "game_records",
    indices = [Index(value = ["timestamp"], orders = [Index.Order.DESC])]
)
data class GameRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playerCount: Int,
    val winnerName: String,
    val winnerScore: Int,
    val gameMode: String,
    val aiDifficulty: String?,
    val isBlocked: Boolean,
    val durationSeconds: Int,
    val timestamp: Long = System.currentTimeMillis(),
    /** JSON array of [com.cancleeric.dominoblockade.domain.model.GameMove] objects. Null for legacy records. */
    val moveHistory: String? = null,
    /** JSON array of initial player hands at game start. Null for legacy records. */
    val initialHandsJson: String? = null
)

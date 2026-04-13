package com.cancleeric.dominoblockade.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "adaptive_ai_state")
data class AdaptiveAiStateEntity(
    @PrimaryKey val id: Int = 1,
    val currentLevel: Int
)

package com.cancleeric.dominoblockade.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quest_profile")
data class QuestProfileEntity(
    @PrimaryKey val id: Int = 1,
    val totalXp: Int = 0,
    val level: Int = 1,
    val updatedAt: Long = 0L
)

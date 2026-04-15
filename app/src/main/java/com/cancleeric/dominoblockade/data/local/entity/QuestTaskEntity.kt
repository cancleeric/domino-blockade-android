package com.cancleeric.dominoblockade.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quest_tasks")
data class QuestTaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val type: String,
    val target: Int,
    val progress: Int,
    val rewardCoins: Int,
    val rewardXp: Int,
    val rewardAchievement: String?,
    val isCompleted: Boolean,
    val isClaimed: Boolean,
    val rotationDate: String,
    val updatedAt: Long
)

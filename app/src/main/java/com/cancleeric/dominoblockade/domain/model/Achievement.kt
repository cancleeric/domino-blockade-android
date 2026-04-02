package com.cancleeric.dominoblockade.domain.model

data class Achievement(
    val type: AchievementType,
    val isUnlocked: Boolean,
    val unlockedAt: Long?
)

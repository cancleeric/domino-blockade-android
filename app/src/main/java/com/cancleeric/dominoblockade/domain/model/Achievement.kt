package com.cancleeric.dominoblockade.domain.model

/**
 * Domain model representing an achievement and its current unlock state.
 */
data class Achievement(
    val type: AchievementType,
    val isUnlocked: Boolean,
    val unlockedAt: Long? = null
) {
    val title: String get() = type.title
    val description: String get() = type.description
    val badgeEmoji: String get() = type.badgeEmoji
}

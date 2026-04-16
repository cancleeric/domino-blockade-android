package com.cancleeric.dominoblockade.domain.model

enum class QuestType {
    DAILY,
    SHORT_TERM,
    LONG_TERM
}

data class QuestTask(
    val id: String,
    val title: String,
    val description: String,
    val type: QuestType,
    val target: Int,
    val progress: Int,
    val rewardCoins: Int,
    val rewardXp: Int,
    val rewardAchievement: AchievementType?,
    val isCompleted: Boolean,
    val isClaimed: Boolean,
    val rotationDate: String = ""
)

data class QuestProfile(
    val totalXp: Int = 0,
    val level: Int = 1
)

data class QuestDashboard(
    val dailyChallenges: List<QuestTask> = emptyList(),
    val shortTermQuests: List<QuestTask> = emptyList(),
    val longTermQuests: List<QuestTask> = emptyList(),
    val profile: QuestProfile = QuestProfile()
)

data class QuestRewardResult(
    val coinsAwarded: Int = 0,
    val xpAwarded: Int = 0,
    val achievementUnlocked: AchievementType? = null
)

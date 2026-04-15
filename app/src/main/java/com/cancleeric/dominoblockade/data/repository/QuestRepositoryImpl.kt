package com.cancleeric.dominoblockade.data.repository

import com.cancleeric.dominoblockade.data.local.dao.QuestDao
import com.cancleeric.dominoblockade.data.local.entity.QuestProfileEntity
import com.cancleeric.dominoblockade.data.local.entity.QuestTaskEntity
import com.cancleeric.dominoblockade.domain.model.AchievementType
import com.cancleeric.dominoblockade.domain.model.QuestDashboard
import com.cancleeric.dominoblockade.domain.model.QuestProfile
import com.cancleeric.dominoblockade.domain.model.QuestRewardResult
import com.cancleeric.dominoblockade.domain.model.QuestTask
import com.cancleeric.dominoblockade.domain.model.QuestType
import com.cancleeric.dominoblockade.domain.repository.AchievementRepository
import com.cancleeric.dominoblockade.domain.repository.QuestRepository
import com.cancleeric.dominoblockade.domain.repository.ShopRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.absoluteValue

private const val COLLECTION_QUEST_PROFILES = "challenge_quest_profiles"
private const val FIELD_TASKS = "tasks"
private const val FIELD_TOTAL_XP = "totalXp"
private const val FIELD_LEVEL = "level"
private const val FIELD_UPDATED_AT = "updatedAt"
private const val XP_PER_LEVEL = 100

@Singleton
class QuestRepositoryImpl @Inject constructor(
    private val questDao: QuestDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val shopRepository: ShopRepository,
    private val achievementRepository: AchievementRepository
) : QuestRepository {

    override fun observeDashboard(): Flow<QuestDashboard> = combine(
        questDao.observeTasks(),
        questDao.observeProfile()
    ) { tasks, profile ->
        val mappedTasks = tasks.map { it.toModel() }
        QuestDashboard(
            dailyChallenges = mappedTasks.filter { it.type == QuestType.DAILY },
            shortTermQuests = mappedTasks.filter { it.type == QuestType.SHORT_TERM },
            longTermQuests = mappedTasks.filter { it.type == QuestType.LONG_TERM },
            profile = profile?.toModel() ?: QuestProfile()
        )
    }

    override suspend fun refresh() {
        ensureLocalQuestData()
        runCatching { syncWithRemote() }
    }

    override suspend fun recordGameResult(isWin: Boolean, isBlocked: Boolean) {
        ensureLocalQuestData()
        val now = System.currentTimeMillis()
        val updatedTasks = questDao.getTasksOnce().map { task ->
            if (task.isCompleted) {
                task
            } else {
                val increment = task.progressIncrement(isWin = isWin, isBlocked = isBlocked)
                val nextProgress = (task.progress + increment).coerceAtMost(task.target)
                task.copy(
                    progress = nextProgress,
                    isCompleted = nextProgress >= task.target,
                    updatedAt = now
                )
            }
        }
        questDao.upsertTasks(updatedTasks)
        runCatching { syncWithRemote() }
    }

    override suspend fun claimReward(taskId: String): QuestRewardResult? {
        ensureLocalQuestData()
        val currentTasks = questDao.getTasksOnce()
        val task = currentTasks.firstOrNull { it.id == taskId && it.isCompleted && !it.isClaimed } ?: return null
        val now = System.currentTimeMillis()
        val updatedTask = task.copy(isClaimed = true, updatedAt = now)
        questDao.upsertTasks(listOf(updatedTask))

        val profile = questDao.getProfileOnce() ?: QuestProfileEntity()
        val newXp = profile.totalXp + task.rewardXp
        val newLevel = levelFromXp(newXp)
        questDao.upsertProfile(profile.copy(totalXp = newXp, level = newLevel, updatedAt = now))

        val coinsAwarded = shopRepository.grantCoins(task.rewardCoins)
        val achievement = task.rewardAchievement
            ?.let { name -> runCatching { AchievementType.valueOf(name) }.getOrNull() }
            ?.also { achievementRepository.unlock(it) }

        runCatching { syncWithRemote() }
        return QuestRewardResult(
            coinsAwarded = coinsAwarded,
            xpAwarded = task.rewardXp,
            achievementUnlocked = achievement
        )
    }

    private suspend fun ensureLocalQuestData() {
        val now = System.currentTimeMillis()
        val today = LocalDate.now(ZoneOffset.UTC).toString()
        val existing = questDao.getTasksOnce()
        val nonDaily = existing.filter { it.type != QuestType.DAILY.name }
        val shortAndLongMissing = (nonDaily.isEmpty() || nonDaily.none { it.type == QuestType.SHORT_TERM.name } ||
            nonDaily.none { it.type == QuestType.LONG_TERM.name })
        if (shortAndLongMissing) {
            val existingById = existing.associateBy { it.id }
            val persistentTasks = (shortTermTemplates + longTermTemplates).map { template ->
                val previous = existingById[template.id]
                template.toEntity(
                    progress = previous?.progress ?: 0,
                    isClaimed = previous?.isClaimed ?: false,
                    rotationDate = "",
                    updatedAt = now
                )
            }
            questDao.upsertTasks(persistentTasks)
        }

        val todaysDaily = existing.filter { it.type == QuestType.DAILY.name && it.rotationDate == today }
        if (todaysDaily.size < DAILY_CHALLENGE_COUNT) {
            questDao.deleteTasksByType(QuestType.DAILY.name)
            questDao.upsertTasks(
                dailyTemplatesFor(today).map { template ->
                    template.toEntity(
                        progress = 0,
                        isClaimed = false,
                        rotationDate = today,
                        updatedAt = now
                    )
                }
            )
        }

        if (questDao.getProfileOnce() == null) {
            questDao.upsertProfile(QuestProfileEntity(updatedAt = now))
        }
    }

    private suspend fun syncWithRemote() {
        val uid = ensureUid() ?: return
        val ref = firestore.collection(COLLECTION_QUEST_PROFILES).document(uid)
        val remote = ref.get().await()
        val localTasks = questDao.getTasksOnce()
        val localProfile = questDao.getProfileOnce() ?: QuestProfileEntity()
        val localUpdatedAt = maxOf(localProfile.updatedAt, localTasks.maxOfOrNull { it.updatedAt } ?: 0L)

        if (!remote.exists()) {
            pushLocalToRemote(ref = ref, tasks = localTasks, profile = localProfile)
            return
        }

        val remoteUpdatedAt = remote.getLong(FIELD_UPDATED_AT) ?: 0L
        if (remoteUpdatedAt > localUpdatedAt) {
            val remoteTasks = (remote.get(FIELD_TASKS) as? List<*>).orEmpty()
                .mapNotNull { it as? Map<*, *> }
                .mapNotNull { it.toTaskEntity() }
            if (remoteTasks.isNotEmpty()) {
                questDao.deleteTasksByType(QuestType.DAILY.name)
                questDao.deleteTasksByType(QuestType.SHORT_TERM.name)
                questDao.deleteTasksByType(QuestType.LONG_TERM.name)
                questDao.upsertTasks(remoteTasks)
            }
            questDao.upsertProfile(
                QuestProfileEntity(
                    id = 1,
                    totalXp = remote.getLong(FIELD_TOTAL_XP)?.toInt() ?: 0,
                    level = remote.getLong(FIELD_LEVEL)?.toInt() ?: 1,
                    updatedAt = remoteUpdatedAt
                )
            )
        } else {
            pushLocalToRemote(ref = ref, tasks = localTasks, profile = localProfile)
        }
    }

    private suspend fun pushLocalToRemote(
        ref: com.google.firebase.firestore.DocumentReference,
        tasks: List<QuestTaskEntity>,
        profile: QuestProfileEntity
    ) {
        val updatedAt = maxOf(profile.updatedAt, tasks.maxOfOrNull { it.updatedAt } ?: System.currentTimeMillis())
        ref.set(
            mapOf(
                FIELD_TOTAL_XP to profile.totalXp,
                FIELD_LEVEL to profile.level,
                FIELD_UPDATED_AT to updatedAt,
                FIELD_TASKS to tasks.map { it.toMap() }
            ),
            SetOptions.merge()
        ).await()
    }

    private suspend fun ensureUid(): String? {
        val current = auth.currentUser
        if (current != null) return current.uid
        return runCatching { auth.signInAnonymously().await().user?.uid }.getOrNull()
    }

    private fun levelFromXp(totalXp: Int): Int = (totalXp / XP_PER_LEVEL) + 1

    private fun QuestTaskEntity.progressIncrement(isWin: Boolean, isBlocked: Boolean): Int = when {
        id.contains("_play_") -> 1
        id.contains("_win_") && isWin -> 1
        id.contains("_blocked_") && isBlocked -> 1
        else -> 0
    }
}

private const val DAILY_CHALLENGE_COUNT = 3

private data class QuestTemplate(
    val id: String,
    val title: String,
    val description: String,
    val type: QuestType,
    val target: Int,
    val rewardCoins: Int,
    val rewardXp: Int,
    val rewardAchievement: AchievementType?
)

private val dailyTemplatePool = listOf(
    QuestTemplate("daily_play_3", "Play 3 games", "Finish 3 matches today", QuestType.DAILY, 3, 60, 25, null),
    QuestTemplate("daily_win_1", "Win once", "Win 1 match today", QuestType.DAILY, 1, 70, 30, null),
    QuestTemplate(
        "daily_win_2",
        "Double Victory",
        "Win 2 matches today",
        QuestType.DAILY,
        2,
        120,
        45,
        AchievementType.DAILY_GRINDER
    ),
    QuestTemplate("daily_blocked_1", "Blockade Expert", "Finish 1 blocked game today", QuestType.DAILY, 1, 80, 35, null)
)

private val shortTermTemplates = listOf(
    QuestTemplate(
        "short_win_5",
        "Winning Momentum",
        "Win 5 matches",
        QuestType.SHORT_TERM,
        5,
        180,
        80,
        AchievementType.QUEST_INITIATE
    ),
    QuestTemplate("short_play_10", "Quick Grinder", "Play 10 matches", QuestType.SHORT_TERM, 10, 150, 70, null)
)

private val longTermTemplates = listOf(
    QuestTemplate(
        "long_win_25",
        "Domino Legend",
        "Win 25 matches",
        QuestType.LONG_TERM,
        25,
        400,
        180,
        AchievementType.QUEST_LEGEND
    ),
    QuestTemplate("long_play_50", "Seasoned Player", "Play 50 matches", QuestType.LONG_TERM, 50, 300, 140, null),
    QuestTemplate("long_blocked_10", "Blockade Strategist", "Finish 10 blocked games", QuestType.LONG_TERM, 10, 220, 110, null)
)

private fun dailyTemplatesFor(date: String): List<QuestTemplate> {
    val start = date.hashCode().absoluteValue % dailyTemplatePool.size
    return (0 until DAILY_CHALLENGE_COUNT).map { offset ->
        val base = dailyTemplatePool[(start + offset) % dailyTemplatePool.size]
        base.copy(id = "${base.id}_$date")
    }
}

private fun QuestTemplate.toEntity(
    progress: Int,
    isClaimed: Boolean,
    rotationDate: String,
    updatedAt: Long
) = QuestTaskEntity(
    id = id,
    title = title,
    description = description,
    type = type.name,
    target = target,
    progress = progress.coerceAtMost(target),
    rewardCoins = rewardCoins,
    rewardXp = rewardXp,
    rewardAchievement = rewardAchievement?.name,
    isCompleted = progress >= target,
    isClaimed = isClaimed,
    rotationDate = rotationDate,
    updatedAt = updatedAt
)

private fun QuestTaskEntity.toModel(): QuestTask = QuestTask(
    id = id,
    title = title,
    description = description,
    type = runCatching { QuestType.valueOf(type) }.getOrDefault(QuestType.DAILY),
    target = target,
    progress = progress,
    rewardCoins = rewardCoins,
    rewardXp = rewardXp,
    rewardAchievement = rewardAchievement?.let { runCatching { AchievementType.valueOf(it) }.getOrNull() },
    isCompleted = isCompleted,
    isClaimed = isClaimed,
    rotationDate = rotationDate
)

private fun QuestProfileEntity.toModel(): QuestProfile = QuestProfile(
    totalXp = totalXp,
    level = level
)

private fun QuestTaskEntity.toMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "title" to title,
    "description" to description,
    "type" to type,
    "target" to target,
    "progress" to progress,
    "rewardCoins" to rewardCoins,
    "rewardXp" to rewardXp,
    "rewardAchievement" to rewardAchievement,
    "isCompleted" to isCompleted,
    "isClaimed" to isClaimed,
    "rotationDate" to rotationDate,
    FIELD_UPDATED_AT to updatedAt
)

private fun Map<*, *>.toTaskEntity(): QuestTaskEntity? {
    val id = this["id"] as? String ?: return null
    val type = this["type"] as? String ?: return null
    return QuestTaskEntity(
        id = id,
        title = this["title"] as? String ?: "",
        description = this["description"] as? String ?: "",
        type = type,
        target = (this["target"] as? Number)?.toInt() ?: 1,
        progress = (this["progress"] as? Number)?.toInt() ?: 0,
        rewardCoins = (this["rewardCoins"] as? Number)?.toInt() ?: 0,
        rewardXp = (this["rewardXp"] as? Number)?.toInt() ?: 0,
        rewardAchievement = this["rewardAchievement"] as? String,
        isCompleted = this["isCompleted"] as? Boolean ?: false,
        isClaimed = this["isClaimed"] as? Boolean ?: false,
        rotationDate = this["rotationDate"] as? String ?: "",
        updatedAt = (this[FIELD_UPDATED_AT] as? Number)?.toLong() ?: 0L
    )
}

package com.cancleeric.dominoblockade.data.repository

import com.cancleeric.dominoblockade.data.local.entity.AchievementEntity
import com.cancleeric.dominoblockade.domain.model.AchievementType
import com.cancleeric.dominoblockade.domain.repository.AchievementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import com.cancleeric.dominoblockade.domain.model.Achievement

class AchievementRepositoryTest {

    private class FakeAchievementRepository : AchievementRepository {
        private val entities = mutableMapOf<AchievementType, AchievementEntity>()
        private val flow = MutableStateFlow<List<Achievement>>(emptyList())

        override fun getAll(): Flow<List<Achievement>> = flow

        override suspend fun getAllUnlocked(): List<Achievement> =
            entities.values.filter { it.isUnlocked }.map { entity ->
                Achievement(
                    type = AchievementType.valueOf(entity.type),
                    isUnlocked = true,
                    unlockedAt = entity.unlockedAt
                )
            }

        override suspend fun unlock(type: AchievementType) {
            entities[type] = AchievementEntity(type = type.name, isUnlocked = true, unlockedAt = 1L)
            flow.value = entities.values.map { e ->
                Achievement(
                    type = AchievementType.valueOf(e.type),
                    isUnlocked = e.isUnlocked,
                    unlockedAt = e.unlockedAt
                )
            }
        }

        override suspend fun isUnlocked(type: AchievementType): Boolean =
            entities[type]?.isUnlocked == true
    }

    private val repository: AchievementRepository = FakeAchievementRepository()

    @Test
    fun `unlock stores achievement and isUnlocked returns true`() = runTest {
        repository.unlock(AchievementType.FIRST_WIN)
        assertTrue(repository.isUnlocked(AchievementType.FIRST_WIN))
    }

    @Test
    fun `unlocked achievement appears in getAllUnlocked`() = runTest {
        repository.unlock(AchievementType.BLOCKADE_PLAYED)
        val unlocked = repository.getAllUnlocked()
        assertEquals(1, unlocked.size)
        assertEquals(AchievementType.BLOCKADE_PLAYED, unlocked.first().type)
    }

    @Test
    fun `isUnlocked returns false for locked achievement`() = runTest {
        assertFalse(repository.isUnlocked(AchievementType.VETERAN))
    }

    @Test
    fun `getAll emits updated list after unlock`() = runTest {
        repository.unlock(AchievementType.WIN_STREAK_3)
        val list = repository.getAll().first()
        assertTrue(list.any { it.type == AchievementType.WIN_STREAK_3 && it.isUnlocked })
    }
}

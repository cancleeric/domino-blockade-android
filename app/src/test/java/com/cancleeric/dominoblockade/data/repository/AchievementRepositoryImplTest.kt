package com.cancleeric.dominoblockade.data.repository

import app.cash.turbine.test
import com.cancleeric.dominoblockade.data.local.dao.AchievementDao
import com.cancleeric.dominoblockade.data.local.entity.AchievementEntity
import com.cancleeric.dominoblockade.domain.model.AchievementType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AchievementRepositoryImplTest {

    private lateinit var dao: AchievementDao
    private lateinit var repository: AchievementRepositoryImpl

    @Before
    fun setup() {
        dao = mockk(relaxed = true)
        repository = AchievementRepositoryImpl(dao)
    }

    @Test
    fun `getAllAchievements maps all AchievementTypes including unseeded entries`() = runTest {
        // Only one entity in the DB — the rest should appear as locked
        every { dao.getAll() } returns flowOf(
            listOf(
                AchievementEntity(
                    type = AchievementType.FIRST_WIN.name,
                    isUnlocked = true,
                    unlockedAt = 1000L
                )
            )
        )

        repository.getAllAchievements().test {
            val list = awaitItem()
            assertEquals(AchievementType.entries.size, list.size)

            val firstWin = list.first { it.type == AchievementType.FIRST_WIN }
            assertTrue(firstWin.isUnlocked)
            assertEquals(1000L, firstWin.unlockedAt)

            val tenWins = list.first { it.type == AchievementType.TEN_WINS }
            assertFalse(tenWins.isUnlocked)

            awaitComplete()
        }
    }

    @Test
    fun `getUnlockedAchievements returns only unlocked achievements`() = runTest {
        every { dao.getUnlocked() } returns flowOf(
            listOf(
                AchievementEntity(
                    type = AchievementType.PERFECT_BLOCK.name,
                    isUnlocked = true,
                    unlockedAt = 2000L
                )
            )
        )

        repository.getUnlockedAchievements().test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals(AchievementType.PERFECT_BLOCK, list[0].type)
            assertTrue(list[0].isUnlocked)
            awaitComplete()
        }
    }

    @Test
    fun `unlockAchievement saves entity to dao when not already unlocked`() = runTest {
        coEvery { dao.getByType(AchievementType.FIRST_WIN.name) } returns null

        repository.unlockAchievement(AchievementType.FIRST_WIN)

        coVerify {
            dao.upsert(
                match { it.type == AchievementType.FIRST_WIN.name && it.isUnlocked }
            )
        }
    }

    @Test
    fun `unlockAchievement is a no-op when achievement is already unlocked`() = runTest {
        coEvery { dao.getByType(AchievementType.FIRST_WIN.name) } returns AchievementEntity(
            type = AchievementType.FIRST_WIN.name,
            isUnlocked = true,
            unlockedAt = 999L
        )

        repository.unlockAchievement(AchievementType.FIRST_WIN)

        coVerify(exactly = 0) { dao.upsert(any()) }
    }

    @Test
    fun `isUnlocked returns true for unlocked achievement`() = runTest {
        coEvery { dao.getByType(AchievementType.QUICK_WIN_60.name) } returns AchievementEntity(
            type = AchievementType.QUICK_WIN_60.name,
            isUnlocked = true
        )

        assertTrue(repository.isUnlocked(AchievementType.QUICK_WIN_60))
    }

    @Test
    fun `isUnlocked returns false for locked achievement`() = runTest {
        coEvery { dao.getByType(AchievementType.HUNDRED_WINS.name) } returns AchievementEntity(
            type = AchievementType.HUNDRED_WINS.name,
            isUnlocked = false
        )

        assertFalse(repository.isUnlocked(AchievementType.HUNDRED_WINS))
    }

    @Test
    fun `isUnlocked returns false when achievement is not in database`() = runTest {
        coEvery { dao.getByType(any()) } returns null

        assertFalse(repository.isUnlocked(AchievementType.WIN_STREAK_5))
    }

    @Test
    fun `initializeAchievements seeds missing achievement entries`() = runTest {
        // Simulate all achievements missing from DB
        coEvery { dao.getByType(any()) } returns null

        repository.initializeAchievements()

        coVerify {
            dao.upsertAll(
                match { list ->
                    list.size == AchievementType.entries.size &&
                        list.all { !it.isUnlocked }
                }
            )
        }
    }

    @Test
    fun `initializeAchievements does not re-seed existing achievements`() = runTest {
        // All achievements already exist in DB
        coEvery { dao.getByType(any()) } returns AchievementEntity(
            type = AchievementType.FIRST_WIN.name,
            isUnlocked = false
        )

        repository.initializeAchievements()

        coVerify(exactly = 0) { dao.upsertAll(any()) }
    }
}

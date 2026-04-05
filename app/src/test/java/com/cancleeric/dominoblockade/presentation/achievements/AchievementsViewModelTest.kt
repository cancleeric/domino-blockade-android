package com.cancleeric.dominoblockade.presentation.achievements

import com.cancleeric.dominoblockade.domain.model.Achievement
import com.cancleeric.dominoblockade.domain.model.AchievementType
import com.cancleeric.dominoblockade.domain.repository.AchievementRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AchievementsViewModelTest {

    private val achievementsFlow = MutableStateFlow<List<Achievement>>(emptyList())

    private val fakeRepository = object : AchievementRepository {
        override fun getAll(): Flow<List<Achievement>> = achievementsFlow
        override suspend fun getAllUnlocked(): List<Achievement> =
            achievementsFlow.value.filter { it.isUnlocked }
        override suspend fun unlock(type: AchievementType) {
            val current = achievementsFlow.value.toMutableList()
            val index = current.indexOfFirst { it.type == type }
            if (index >= 0) {
                current[index] = current[index].copy(isUnlocked = true, unlockedAt = 1L)
            } else {
                current.add(Achievement(type = type, isUnlocked = true, unlockedAt = 1L))
            }
            achievementsFlow.value = current
        }
        override suspend fun isUnlocked(type: AchievementType): Boolean =
            achievementsFlow.value.firstOrNull { it.type == type }?.isUnlocked == true
    }

    private lateinit var viewModel: AchievementsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = AchievementsViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial achievements list is empty`() = runTest {
        assertTrue(viewModel.achievements.value.isEmpty())
    }

    @Test
    fun `achievements updates when repository emits data`() = runTest {
        achievementsFlow.value = listOf(
            Achievement(type = AchievementType.FIRST_WIN, isUnlocked = true, unlockedAt = 1L)
        )
        assertEquals(1, viewModel.achievements.value.size)
    }

    @Test
    fun `achievements reflects isUnlocked state correctly`() = runTest {
        achievementsFlow.value = listOf(
            Achievement(type = AchievementType.FIRST_WIN, isUnlocked = true, unlockedAt = 1L),
            Achievement(type = AchievementType.VETERAN, isUnlocked = false, unlockedAt = null)
        )
        val unlocked = viewModel.achievements.value.filter { it.isUnlocked }
        val locked = viewModel.achievements.value.filter { !it.isUnlocked }
        assertEquals(1, unlocked.size)
        assertEquals(1, locked.size)
    }

    @Test
    fun `achievements updates reflect all AchievementType entries`() = runTest {
        achievementsFlow.value = AchievementType.entries.map {
            Achievement(type = it, isUnlocked = false, unlockedAt = null)
        }
        assertEquals(AchievementType.entries.size, viewModel.achievements.value.size)
    }

    @Test
    fun `unlocking achievement via repository is reflected in ViewModel`() = runTest {
        achievementsFlow.value = listOf(
            Achievement(type = AchievementType.BLOCKADE_PLAYED, isUnlocked = false, unlockedAt = null)
        )
        assertFalse(viewModel.achievements.value.first().isUnlocked)
        fakeRepository.unlock(AchievementType.BLOCKADE_PLAYED)
        assertTrue(viewModel.achievements.value.first().isUnlocked)
    }
}

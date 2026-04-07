package com.cancleeric.dominoblockade.presentation.achievements

import app.cash.turbine.test
import com.cancleeric.dominoblockade.domain.model.Achievement
import com.cancleeric.dominoblockade.domain.model.AchievementType
import com.cancleeric.dominoblockade.domain.repository.AchievementRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AchievementsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val achievementsFlow = MutableStateFlow<List<Achievement>>(emptyList())
    private val repository: AchievementRepository = mockk {
        every { getAll() } returns achievementsFlow
    }

    private lateinit var viewModel: AchievementsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = AchievementsViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `achievements initial value is empty list`() = runTest(testDispatcher) {
        advanceUntilIdle()
        assertTrue(viewModel.achievements.value.isEmpty())
    }

    @Test
    fun `achievements emits list when repository emits`() = runTest(testDispatcher) {
        val list = listOf(
            Achievement(AchievementType.FIRST_WIN, isUnlocked = true, unlockedAt = 1000L)
        )
        achievementsFlow.value = list
        advanceUntilIdle()
        assertEquals(list, viewModel.achievements.value)
    }

    @Test
    fun `achievements updates when new achievements are added`() = runTest(testDispatcher) {
        viewModel.achievements.test {
            assertTrue(awaitItem().isEmpty())
            val achievement = Achievement(AchievementType.BLOCKADE_PLAYED, isUnlocked = true, unlockedAt = 2000L)
            achievementsFlow.value = listOf(achievement)
            assertEquals(1, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `achievements reflects all unlocked types from repository`() = runTest(testDispatcher) {
        val all = AchievementType.entries.map { type ->
            Achievement(type, isUnlocked = true, unlockedAt = 1L)
        }
        achievementsFlow.value = all
        advanceUntilIdle()
        assertEquals(AchievementType.entries.size, viewModel.achievements.value.size)
    }
}

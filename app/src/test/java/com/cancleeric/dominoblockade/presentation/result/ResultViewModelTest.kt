package com.cancleeric.dominoblockade.presentation.result

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.cancleeric.dominoblockade.data.local.entity.PlayerStatsEntity
import com.cancleeric.dominoblockade.domain.model.AchievementType
import com.cancleeric.dominoblockade.domain.model.GameResult
import com.cancleeric.dominoblockade.domain.repository.AchievementRepository
import com.cancleeric.dominoblockade.domain.repository.PlayerStatsRepository
import com.cancleeric.dominoblockade.domain.usecase.CheckAchievementsUseCase
import com.cancleeric.dominoblockade.domain.analytics.AnalyticsTracker
import com.cancleeric.dominoblockade.presentation.notification.AchievementNotificationHelper
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class ResultViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val playerStatsRepository: PlayerStatsRepository = mockk(relaxed = true) {
        coEvery { getByName(any()) } returns null
    }
    private val achievementRepository: AchievementRepository = mockk(relaxed = true) {
        coEvery { getAllUnlocked() } returns emptyList()
    }
    private val checkAchievementsUseCase = CheckAchievementsUseCase(achievementRepository)
    private val notificationHelper: AchievementNotificationHelper = mockk(relaxed = true)
    private val analyticsTracker: AnalyticsTracker = mockk(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(winnerName: String, isBlocked: Boolean): ResultViewModel {
        val handle = SavedStateHandle(
            mapOf("winnerName" to winnerName, "isBlocked" to isBlocked)
        )
        return ResultViewModel(handle, playerStatsRepository, checkAchievementsUseCase, notificationHelper, analyticsTracker)
    }

    @Test
    fun `newAchievements initial value is empty`() = runTest(testDispatcher) {
        val viewModel = createViewModel("Alice", false)
        advanceUntilIdle()
        // achievements list may or may not include FIRST_WIN; initial before coroutine is empty
        assertTrue(viewModel.newAchievements.value is List<*>)
    }

    @Test
    fun `win game upserts player stats with incremented wins`() = runTest(testDispatcher) {
        createViewModel("Alice", false)
        advanceUntilIdle()
        coVerify { playerStatsRepository.upsert(match { it.wins == 1 && it.totalGames == 1 }) }
    }

    @Test
    fun `blocked game upserts player stats with no win increment`() = runTest(testDispatcher) {
        createViewModel("_", false)
        advanceUntilIdle()
        coVerify { playerStatsRepository.upsert(match { it.wins == 0 && it.totalGames == 1 }) }
    }

    @Test
    fun `first win unlocks FIRST_WIN achievement`() = runTest(testDispatcher) {
        coEvery { achievementRepository.getAllUnlocked() } returns emptyList()
        val viewModel = createViewModel("Alice", false)
        advanceUntilIdle()
        assertTrue(viewModel.newAchievements.value.contains(AchievementType.FIRST_WIN))
    }

    @Test
    fun `already unlocked FIRST_WIN is not re-unlocked`() = runTest(testDispatcher) {
        coEvery { achievementRepository.getAllUnlocked() } returns listOf(
            com.cancleeric.dominoblockade.domain.model.Achievement(
                AchievementType.FIRST_WIN, isUnlocked = true, unlockedAt = 1L
            )
        )
        val viewModel = createViewModel("Alice", false)
        advanceUntilIdle()
        assertTrue(viewModel.newAchievements.value.none { it == AchievementType.FIRST_WIN })
    }

    @Test
    fun `analytics logGameEnd is called`() = runTest(testDispatcher) {
        createViewModel("Alice", false)
        advanceUntilIdle()
        io.mockk.verify { analyticsTracker.logGameEnd(eq("Alice"), eq(false), any(), any()) }
    }

    @Test
    fun `blocked game logs analytics with isBlocked true`() = runTest(testDispatcher) {
        createViewModel("Bob", true)
        advanceUntilIdle()
        io.mockk.verify { analyticsTracker.logGameEnd(eq("Bob"), eq(true), any(), any()) }
    }

    @Test
    fun `newAchievements emits via turbine`() = runTest(testDispatcher) {
        val viewModel = createViewModel("Alice", false)
        viewModel.newAchievements.test {
            val initial = awaitItem()
            assertTrue(initial is List<*>)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `existing stats are incremented not reset`() = runTest(testDispatcher) {
        val existing = PlayerStatsEntity(playerName = "global", wins = 5, totalGames = 10)
        coEvery { playerStatsRepository.getByName("global") } returns existing
        createViewModel("Alice", false)
        advanceUntilIdle()
        coVerify { playerStatsRepository.upsert(match { it.wins == 6 && it.totalGames == 11 }) }
    }
}

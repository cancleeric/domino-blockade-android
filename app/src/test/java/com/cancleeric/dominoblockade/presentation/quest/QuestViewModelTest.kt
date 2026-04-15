package com.cancleeric.dominoblockade.presentation.quest

import com.cancleeric.dominoblockade.domain.model.QuestDashboard
import com.cancleeric.dominoblockade.domain.model.QuestRewardResult
import com.cancleeric.dominoblockade.domain.model.QuestTask
import com.cancleeric.dominoblockade.domain.model.QuestType
import com.cancleeric.dominoblockade.domain.repository.QuestRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class QuestViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val dashboardFlow = MutableStateFlow(QuestDashboard())
    private var claimedTaskId: String? = null

    private val fakeRepository = object : QuestRepository {
        override fun observeDashboard(): Flow<QuestDashboard> = dashboardFlow
        override suspend fun refresh() = Unit
        override suspend fun recordGameResult(isWin: Boolean, isBlocked: Boolean) = Unit
        override suspend fun claimReward(taskId: String): QuestRewardResult? {
            claimedTaskId = taskId
            return QuestRewardResult(coinsAwarded = 25, xpAwarded = 15)
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `dashboard state mirrors repository flow`() = runTest(dispatcher) {
        val viewModel = QuestViewModel(fakeRepository)
        val task = QuestTask(
            id = "daily_win_1",
            title = "Win once",
            description = "Win 1 game today",
            type = QuestType.DAILY,
            target = 1,
            progress = 0,
            rewardCoins = 50,
            rewardXp = 20,
            rewardAchievement = null,
            isCompleted = false,
            isClaimed = false
        )
        dashboardFlow.value = QuestDashboard(dailyChallenges = listOf(task))
        advanceUntilIdle()
        assertEquals(1, viewModel.dashboard.value.dailyChallenges.size)
    }

    @Test
    fun `claimReward delegates to repository`() = runTest(dispatcher) {
        val viewModel = QuestViewModel(fakeRepository)
        viewModel.claimReward("daily_win_1")
        advanceUntilIdle()
        assertEquals("daily_win_1", claimedTaskId)
    }
}

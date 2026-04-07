package com.cancleeric.dominoblockade.presentation.stats

import app.cash.turbine.test
import com.cancleeric.dominoblockade.data.local.entity.PlayerStatsEntity
import com.cancleeric.dominoblockade.domain.repository.PlayerStatsRepository
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
class StatsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val statsFlow = MutableStateFlow<List<PlayerStatsEntity>>(emptyList())
    private val repository: PlayerStatsRepository = mockk {
        every { getAll() } returns statsFlow
    }

    private lateinit var viewModel: StatsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = StatsViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `playerStats initial value is empty`() = runTest(testDispatcher) {
        advanceUntilIdle()
        assertTrue(viewModel.playerStats.value.isEmpty())
    }

    @Test
    fun `playerStats reflects data from repository`() = runTest(testDispatcher) {
        val stats = listOf(
            PlayerStatsEntity(playerName = "Alice", wins = 5, totalGames = 10),
            PlayerStatsEntity(playerName = "Bob", wins = 3, totalGames = 8)
        )
        statsFlow.value = stats
        advanceUntilIdle()
        assertEquals(2, viewModel.playerStats.value.size)
        assertEquals("Alice", viewModel.playerStats.value[0].playerName)
    }

    @Test
    fun `playerStats updates via turbine`() = runTest(testDispatcher) {
        viewModel.playerStats.test {
            assertTrue(awaitItem().isEmpty())
            statsFlow.value = listOf(PlayerStatsEntity(playerName = "Carol", wins = 1, totalGames = 3))
            assertEquals(1, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `playerStats reflects win and game counts correctly`() = runTest(testDispatcher) {
        statsFlow.value = listOf(PlayerStatsEntity(playerName = "Dave", wins = 7, totalGames = 15))
        advanceUntilIdle()
        val stat = viewModel.playerStats.value.first()
        assertEquals(7, stat.wins)
        assertEquals(15, stat.totalGames)
    }
}

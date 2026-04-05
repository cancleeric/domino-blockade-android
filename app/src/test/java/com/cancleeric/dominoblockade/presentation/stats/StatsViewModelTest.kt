package com.cancleeric.dominoblockade.presentation.stats

import com.cancleeric.dominoblockade.data.local.entity.PlayerStatsEntity
import com.cancleeric.dominoblockade.domain.repository.PlayerStatsRepository
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModelTest {

    private val statsFlow = MutableStateFlow<List<PlayerStatsEntity>>(emptyList())

    private val fakeRepository = object : PlayerStatsRepository {
        override suspend fun upsert(stats: PlayerStatsEntity) {
            val current = statsFlow.value.toMutableList()
            val index = current.indexOfFirst { it.playerName == stats.playerName }
            if (index >= 0) current[index] = stats else current.add(stats)
            statsFlow.value = current
        }
        override suspend fun getByName(name: String): PlayerStatsEntity? =
            statsFlow.value.firstOrNull { it.playerName == name }
        override fun getAll(): Flow<List<PlayerStatsEntity>> = statsFlow
    }

    private lateinit var viewModel: StatsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = StatsViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial playerStats list is empty`() = runTest {
        assertTrue(viewModel.playerStats.value.isEmpty())
    }

    @Test
    fun `playerStats updates when repository emits new data`() = runTest {
        statsFlow.value = listOf(PlayerStatsEntity(playerName = "Alice", wins = 5, totalGames = 10))
        assertEquals(1, viewModel.playerStats.value.size)
        assertEquals("Alice", viewModel.playerStats.value.first().playerName)
    }

    @Test
    fun `playerStats reflects win count`() = runTest {
        statsFlow.value = listOf(PlayerStatsEntity(playerName = "Bob", wins = 3, totalGames = 7))
        assertEquals(3, viewModel.playerStats.value.first().wins)
    }

    @Test
    fun `playerStats updates incrementally with multiple entries`() = runTest {
        statsFlow.value = listOf(
            PlayerStatsEntity(playerName = "Alice", wins = 2),
            PlayerStatsEntity(playerName = "Bob", wins = 1)
        )
        assertEquals(2, viewModel.playerStats.value.size)
    }
}

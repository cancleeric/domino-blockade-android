package com.cancleeric.dominoblockade.presentation.history

import app.cash.turbine.test
import com.cancleeric.dominoblockade.data.local.entity.GameRecordEntity
import com.cancleeric.dominoblockade.domain.repository.GameRecordRepository
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
class HistoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val recordsFlow = MutableStateFlow<List<GameRecordEntity>>(emptyList())
    private val repository: GameRecordRepository = mockk {
        every { getRecent(any()) } returns recordsFlow
    }

    private lateinit var viewModel: HistoryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = HistoryViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun record(winner: String = "Player 1") = GameRecordEntity(
        playerCount = 2,
        winnerName = winner,
        winnerScore = 10,
        gameMode = "local_multiplayer",
        aiDifficulty = null,
        isBlocked = false,
        durationSeconds = 60
    )

    @Test
    fun `records initial value is empty`() = runTest(testDispatcher) {
        advanceUntilIdle()
        assertTrue(viewModel.records.value.isEmpty())
    }

    @Test
    fun `records reflects repository data after emit`() = runTest(testDispatcher) {
        val list = listOf(record("Alice"), record("Bob"))
        recordsFlow.value = list
        advanceUntilIdle()
        assertEquals(2, viewModel.records.value.size)
        assertEquals("Alice", viewModel.records.value[0].winnerName)
    }

    @Test
    fun `records updates via turbine when repository emits`() = runTest(testDispatcher) {
        viewModel.records.test {
            assertTrue(awaitItem().isEmpty())
            recordsFlow.value = listOf(record("Charlie"))
            assertEquals(1, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `records replaces old list when repository updates`() = runTest(testDispatcher) {
        recordsFlow.value = listOf(record("Alice"))
        advanceUntilIdle()
        assertEquals(1, viewModel.records.value.size)

        recordsFlow.value = listOf(record("Alice"), record("Bob"), record("Carol"))
        advanceUntilIdle()
        assertEquals(3, viewModel.records.value.size)
    }
}

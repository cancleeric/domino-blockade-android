package com.cancleeric.dominoblockade.presentation.history

import com.cancleeric.dominoblockade.data.local.entity.GameRecordEntity
import com.cancleeric.dominoblockade.domain.repository.GameRecordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
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
class HistoryViewModelTest {

    private val recordsFlow = MutableStateFlow<List<GameRecordEntity>>(emptyList())

    private val fakeRepository = object : GameRecordRepository {
        override suspend fun insert(record: GameRecordEntity) {
            recordsFlow.value = recordsFlow.value + record
        }
        override fun getAll(): Flow<List<GameRecordEntity>> = recordsFlow
        override fun getRecent(limit: Int): Flow<List<GameRecordEntity>> =
            recordsFlow.map { it.takeLast(limit) }
    }

    private lateinit var viewModel: HistoryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = HistoryViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun record(winnerName: String = "Alice") = GameRecordEntity(
        playerCount = 2,
        winnerName = winnerName,
        winnerScore = 10,
        gameMode = "local",
        aiDifficulty = null,
        isBlocked = false,
        durationSeconds = 60
    )

    @Test
    fun `initial records list is empty`() = runTest {
        assertTrue(viewModel.records.value.isEmpty())
    }

    @Test
    fun `records updates when repository emits new data`() = runTest {
        recordsFlow.value = listOf(record("Alice"), record("Bob"))
        assertEquals(2, viewModel.records.value.size)
    }

    @Test
    fun `records reflects correct winner names`() = runTest {
        recordsFlow.value = listOf(record("Carol"))
        assertEquals("Carol", viewModel.records.value.first().winnerName)
    }

    @Test
    fun `records updates incrementally`() = runTest {
        recordsFlow.value = listOf(record("Alice"))
        assertEquals(1, viewModel.records.value.size)
        recordsFlow.value = listOf(record("Alice"), record("Bob"))
        assertEquals(2, viewModel.records.value.size)
    }
}

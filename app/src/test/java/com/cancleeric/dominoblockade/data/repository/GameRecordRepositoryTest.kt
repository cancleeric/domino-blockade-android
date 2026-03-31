package com.cancleeric.dominoblockade.data.repository

import com.cancleeric.dominoblockade.data.local.entity.GameRecordEntity
import com.cancleeric.dominoblockade.domain.repository.GameRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameRecordRepositoryTest {

    private class FakeGameRecordRepository : GameRecordRepository {
        private val records = MutableStateFlow<List<GameRecordEntity>>(emptyList())

        override suspend fun insert(record: GameRecordEntity) {
            records.value = records.value + record
        }

        override fun getAll(): Flow<List<GameRecordEntity>> = records

        override fun getRecent(limit: Int): Flow<List<GameRecordEntity>> =
            records.map { it.takeLast(limit) }
    }

    private val repository = FakeGameRecordRepository()

    private fun createRecord(
        winnerName: String = "Player 1",
        winnerScore: Int = 10,
        isBlocked: Boolean = false
    ) = GameRecordEntity(
        playerCount = 2,
        winnerName = winnerName,
        winnerScore = winnerScore,
        gameMode = "local_multiplayer",
        aiDifficulty = null,
        isBlocked = isBlocked,
        durationSeconds = 60
    )

    @Test
    fun `insert adds record and getAll returns it`() = runTest {
        val record = createRecord()
        repository.insert(record)
        val result = repository.getAll().first()
        assertEquals(1, result.size)
        assertEquals("Player 1", result[0].winnerName)
    }

    @Test
    fun `insert multiple records returns all in order`() = runTest {
        repository.insert(createRecord(winnerName = "Alice"))
        repository.insert(createRecord(winnerName = "Bob"))
        val result = repository.getAll().first()
        assertEquals(2, result.size)
        assertEquals("Alice", result[0].winnerName)
        assertEquals("Bob", result[1].winnerName)
    }

    @Test
    fun `getAll returns empty list when no records`() = runTest {
        val result = repository.getAll().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `insert blocked game record stores isBlocked correctly`() = runTest {
        val record = createRecord(isBlocked = true)
        repository.insert(record)
        val result = repository.getAll().first()
        assertTrue(result[0].isBlocked)
    }
}

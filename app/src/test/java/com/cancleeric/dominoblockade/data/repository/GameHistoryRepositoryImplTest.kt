package com.cancleeric.dominoblockade.data.repository

import com.cancleeric.dominoblockade.data.local.GameRecord
import com.cancleeric.dominoblockade.data.local.GameRecordDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GameHistoryRepositoryImplTest {

    private lateinit var fakeDao: FakeGameRecordDao
    private lateinit var repository: GameHistoryRepositoryImpl

    @Before
    fun setUp() {
        fakeDao = FakeGameRecordDao()
        repository = GameHistoryRepositoryImpl(fakeDao)
    }

    @Test
    fun `getStats returns empty stats when no records`() = runTest {
        val stats = repository.getStats().first()
        assertEquals(0, stats.totalGames)
        assertEquals(0, stats.totalWins)
        assertEquals(0f, stats.winRate)
    }

    @Test
    fun `getStats calculates win rate correctly`() = runTest {
        fakeDao.insertRecord(GameRecord(id = 1, isWin = true, playerScore = 10, difficulty = "EASY"))
        fakeDao.insertRecord(GameRecord(id = 2, isWin = false, playerScore = 5, difficulty = "EASY"))
        fakeDao.insertRecord(GameRecord(id = 3, isWin = true, playerScore = 20, difficulty = "MEDIUM"))

        val stats = repository.getStats().first()
        assertEquals(3, stats.totalGames)
        assertEquals(2, stats.totalWins)
        assertEquals(2f / 3f, stats.winRate, 0.001f)
        assertEquals(20, stats.bestScore)
    }

    @Test
    fun `getStats calculates consecutive wins from most recent`() = runTest {
        fakeDao.insertRecord(GameRecord(id = 1, isWin = true, timestamp = 3000))
        fakeDao.insertRecord(GameRecord(id = 2, isWin = true, timestamp = 2000))
        fakeDao.insertRecord(GameRecord(id = 3, isWin = false, timestamp = 1000))

        val stats = repository.getStats().first()
        assertEquals(2, stats.consecutiveWins)
    }

    @Test
    fun `getStats builds per-difficulty stats`() = runTest {
        fakeDao.insertRecord(GameRecord(id = 1, isWin = true, playerScore = 15, difficulty = "EASY"))
        fakeDao.insertRecord(GameRecord(id = 2, isWin = false, playerScore = 8, difficulty = "HARD"))
        fakeDao.insertRecord(GameRecord(id = 3, isWin = true, playerScore = 20, difficulty = "HARD"))

        val stats = repository.getStats().first()
        assertEquals(1, stats.easyStats.totalGames)
        assertEquals(1, stats.easyStats.totalWins)
        assertEquals(0, stats.mediumStats.totalGames)
        assertEquals(2, stats.hardStats.totalGames)
        assertEquals(1, stats.hardStats.totalWins)
        assertEquals(20, stats.hardStats.bestScore)
    }

    @Test
    fun `clearAllRecords removes all records`() = runTest {
        fakeDao.insertRecord(GameRecord(id = 1))
        fakeDao.insertRecord(GameRecord(id = 2))
        repository.clearAllRecords()

        val records = repository.getAllRecords().first()
        assertEquals(0, records.size)
    }
}

private class FakeGameRecordDao : GameRecordDao {
    private val _records = MutableStateFlow<List<GameRecord>>(emptyList())

    override fun getAllRecords() = _records

    override fun getRecordById(id: Long) =
        MutableStateFlow(_records.value.find { it.id == id })

    override fun getRecordsByDifficulty(difficulty: String) =
        MutableStateFlow(_records.value.filter { it.difficulty == difficulty })

    override suspend fun insertRecord(record: GameRecord) {
        _records.value = (_records.value.filterNot { it.id == record.id } + record)
            .sortedByDescending { it.timestamp }
    }

    override suspend fun deleteAllRecords() {
        _records.value = emptyList()
    }
}


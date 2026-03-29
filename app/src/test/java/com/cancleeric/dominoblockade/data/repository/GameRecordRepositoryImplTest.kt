package com.cancleeric.dominoblockade.data.repository

import app.cash.turbine.test
import com.cancleeric.dominoblockade.data.local.dao.GameRecordDao
import com.cancleeric.dominoblockade.data.local.entity.GameRecordEntity
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GameRecordRepositoryImplTest {

    private lateinit var dao: GameRecordDao
    private lateinit var repository: GameRecordRepositoryImpl

    @Before
    fun setup() {
        dao = mockk(relaxed = true)
        repository = GameRecordRepositoryImpl(dao)
    }

    @Test
    fun `saveRecord calls dao insert`() = runTest {
        val record = GameRecordEntity(
            playerCount = 2,
            winnerName = "Player1",
            winnerScore = 10,
            gameMode = "vs_ai",
            aiDifficulty = "medium",
            isBlocked = false,
            durationSeconds = 300
        )

        repository.saveRecord(record)

        coVerify { dao.insert(record) }
    }

    @Test
    fun `getAllRecords returns flow from dao`() = runTest {
        val records = listOf(
            GameRecordEntity(
                id = 1,
                playerCount = 2,
                winnerName = "Player1",
                winnerScore = 10,
                gameMode = "vs_ai",
                aiDifficulty = "medium",
                isBlocked = false,
                durationSeconds = 300
            )
        )
        every { dao.getAll() } returns flowOf(records)

        repository.getAllRecords().test {
            assertEquals(records, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `getRecentRecords calls dao with correct limit`() = runTest {
        val limit = 5
        val records = emptyList<GameRecordEntity>()
        every { dao.getRecent(limit) } returns flowOf(records)

        repository.getRecentRecords(limit).test {
            assertEquals(records, awaitItem())
            awaitComplete()
        }
    }
}

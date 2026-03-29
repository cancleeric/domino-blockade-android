package com.cancleeric.dominoblockade.data.repository

import app.cash.turbine.test
import com.cancleeric.dominoblockade.data.local.dao.PlayerStatsDao
import com.cancleeric.dominoblockade.data.local.entity.PlayerStatsEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class PlayerStatsRepositoryImplTest {

    private lateinit var dao: PlayerStatsDao
    private lateinit var repository: PlayerStatsRepositoryImpl

    @Before
    fun setup() {
        dao = mockk(relaxed = true)
        repository = PlayerStatsRepositoryImpl(dao)
    }

    @Test
    fun `upsertStats calls dao upsert`() = runTest {
        val stats = PlayerStatsEntity(
            playerName = "Alice",
            totalGames = 5,
            wins = 3,
            losses = 2,
            totalScore = 50,
            highestScore = 20,
            blockedWins = 1
        )

        repository.upsertStats(stats)

        coVerify { dao.upsert(stats) }
    }

    @Test
    fun `getStatsByName returns stats from dao`() = runTest {
        val stats = PlayerStatsEntity(
            playerName = "Bob",
            totalGames = 10,
            wins = 7,
            losses = 3,
            totalScore = 100,
            highestScore = 30,
            blockedWins = 2
        )
        coEvery { dao.getByName("Bob") } returns stats

        val result = repository.getStatsByName("Bob")

        assertEquals(stats, result)
    }

    @Test
    fun `getStatsByName returns null when not found`() = runTest {
        coEvery { dao.getByName("Unknown") } returns null

        val result = repository.getStatsByName("Unknown")

        assertNull(result)
    }

    @Test
    fun `getAllStats returns flow from dao`() = runTest {
        val statsList = listOf(
            PlayerStatsEntity(playerName = "Alice", totalGames = 5, wins = 3, losses = 2,
                totalScore = 50, highestScore = 20, blockedWins = 1)
        )
        every { dao.getAll() } returns flowOf(statsList)

        repository.getAllStats().test {
            assertEquals(statsList, awaitItem())
            awaitComplete()
        }
    }
}

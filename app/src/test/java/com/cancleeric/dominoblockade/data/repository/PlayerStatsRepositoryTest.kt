package com.cancleeric.dominoblockade.data.repository

import com.cancleeric.dominoblockade.data.local.entity.PlayerStatsEntity
import com.cancleeric.dominoblockade.domain.repository.PlayerStatsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerStatsRepositoryTest {

    private class FakePlayerStatsRepository : PlayerStatsRepository {
        private val stats = MutableStateFlow<Map<String, PlayerStatsEntity>>(emptyMap())

        override suspend fun upsert(entity: PlayerStatsEntity) {
            stats.value = stats.value + (entity.playerName to entity)
        }

        override suspend fun getByName(name: String): PlayerStatsEntity? =
            stats.value[name]

        override fun getAll(): Flow<List<PlayerStatsEntity>> =
            MutableStateFlow(stats.value.values.sortedByDescending { it.wins })
    }

    private val repository = FakePlayerStatsRepository()

    @Test
    fun `upsert adds new player stats`() = runTest {
        val stats = PlayerStatsEntity(playerName = "Alice", wins = 3, totalGames = 5)
        repository.upsert(stats)
        val result = repository.getByName("Alice")
        assertEquals(3, result?.wins)
        assertEquals(5, result?.totalGames)
    }

    @Test
    fun `upsert replaces existing player stats`() = runTest {
        val stats = PlayerStatsEntity(playerName = "Bob", wins = 1, totalGames = 2)
        repository.upsert(stats)
        val updated = PlayerStatsEntity(playerName = "Bob", wins = 3, totalGames = 4)
        repository.upsert(updated)
        val result = repository.getByName("Bob")
        assertEquals(3, result?.wins)
        assertEquals(4, result?.totalGames)
    }

    @Test
    fun `getByName returns null for unknown player`() = runTest {
        val result = repository.getByName("Unknown")
        assertNull(result)
    }

    @Test
    fun `getAll returns empty list initially`() = runTest {
        val result = repository.getAll().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getAll returns all players after upsert`() = runTest {
        repository.upsert(PlayerStatsEntity(playerName = "Alice", wins = 2))
        repository.upsert(PlayerStatsEntity(playerName = "Bob", wins = 1))
        val result = repository.getAll().first()
        assertEquals(2, result.size)
    }
}

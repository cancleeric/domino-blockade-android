package com.dominoblockade.domain.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class GameConfigTest {

    @Test
    fun `GameConfig defaults are correct`() {
        val config = GameConfig()
        assertEquals(2, config.playerCount)
        assertEquals(7, config.dominoesPerPlayer)
        assertEquals(AiDifficulty.MEDIUM, config.aiDifficulty)
    }

    @Test
    fun `GameConfig for 2 players uses 7 dominoes each`() {
        val config = GameConfig(playerCount = 2, dominoesPerPlayer = 7)
        assertEquals(7, config.dominoesPerPlayer)
        // 2 players x 7 dominoes = 14, leaves 14 in draw pile from 28 total
        val totalDealt = config.playerCount * config.dominoesPerPlayer
        assertEquals(14, totalDealt)
        assertTrue(totalDealt <= 28)
    }

    @Test
    fun `GameConfig for 3 players uses 5 dominoes each`() {
        val config = GameConfig(playerCount = 3, dominoesPerPlayer = 5)
        assertEquals(5, config.dominoesPerPlayer)
        // 3 players x 5 dominoes = 15, leaves 13 in draw pile from 28 total
        val totalDealt = config.playerCount * config.dominoesPerPlayer
        assertEquals(15, totalDealt)
        assertTrue(totalDealt <= 28)
    }

    @Test
    fun `GameConfig for 4 players uses 5 dominoes each`() {
        val config = GameConfig(playerCount = 4, dominoesPerPlayer = 5)
        assertEquals(5, config.dominoesPerPlayer)
        // 4 players x 5 dominoes = 20, leaves 8 in draw pile from 28 total
        val totalDealt = config.playerCount * config.dominoesPerPlayer
        assertEquals(20, totalDealt)
        assertTrue(totalDealt <= 28)
    }

    @Test
    fun `AiDifficulty enum contains EASY, MEDIUM, HARD`() {
        val difficulties = AiDifficulty.values()
        assertTrue(difficulties.contains(AiDifficulty.EASY))
        assertTrue(difficulties.contains(AiDifficulty.MEDIUM))
        assertTrue(difficulties.contains(AiDifficulty.HARD))
    }

    @Test
    fun `GameConfig can set EASY difficulty`() {
        val config = GameConfig(aiDifficulty = AiDifficulty.EASY)
        assertEquals(AiDifficulty.EASY, config.aiDifficulty)
    }

    @Test
    fun `GameConfig can set HARD difficulty`() {
        val config = GameConfig(aiDifficulty = AiDifficulty.HARD)
        assertEquals(AiDifficulty.HARD, config.aiDifficulty)
    }
}

package com.cancleeric.dominoblockade.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class StartGameUseCaseTest {
    private val useCase = StartGameUseCase()

    @Test
    fun `invoke with two players creates correct hand sizes`() {
        val gameState = useCase(listOf("Alice", "Bob"))
        assertEquals(2, gameState.players.size)
        assertEquals(7, gameState.players[0].hand.size)
        assertEquals(7, gameState.players[1].hand.size)
        assertEquals(28 - 14, gameState.boneyard.size)
    }

    @Test
    fun `invoke with four players creates correct hand sizes`() {
        val gameState = useCase(listOf("Alice", "Bob", "Charlie", "Dave"))
        assertEquals(4, gameState.players.size)
        gameState.players.forEach { player ->
            assertEquals(5, player.hand.size)
        }
        assertEquals(28 - 20, gameState.boneyard.size)
    }

    @Test
    fun `invoke creates unique dominoes`() {
        val gameState = useCase(listOf("Alice", "Bob"))
        val allDominoes = gameState.players.flatMap { it.hand } + gameState.boneyard
        assertEquals(28, allDominoes.size)
        assertEquals(28, allDominoes.distinct().size)
    }

    @Test
    fun `first player index is zero`() {
        val gameState = useCase(listOf("Alice", "Bob"))
        assertEquals(0, gameState.currentPlayerIndex)
        assertFalse(gameState.isGameOver)
    }
}

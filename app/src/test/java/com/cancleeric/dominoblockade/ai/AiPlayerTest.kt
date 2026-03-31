package com.cancleeric.dominoblockade.ai

import com.cancleeric.dominoblockade.domain.model.Domino
import com.cancleeric.dominoblockade.domain.model.GameState
import com.cancleeric.dominoblockade.domain.model.Player
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AiPlayerTest {

    private val player = Player(id = "p0", name = "Player 1", hand = emptyList())
    private val state = GameState(
        players = listOf(player),
        board = emptyList(),
        boneyard = emptyList(),
        currentPlayerIndex = 0
    )

    @Test
    fun `makeMove delegates to the underlying strategy`() = runTest {
        val validMoves = listOf(
            ValidMove(Domino(1, 2), BoardEnd.RIGHT, false),
            ValidMove(Domino(3, 4), BoardEnd.LEFT, true)
        )
        val aiPlayer = AiPlayer(EasyAi(), thinkingDelayMs = 0)

        val result = aiPlayer.makeMove(state, player, validMoves)

        assertTrue(result in validMoves)
    }

    @Test
    fun `makeMove returns null when no valid moves are available`() = runTest {
        val aiPlayer = AiPlayer(EasyAi(), thinkingDelayMs = 0)

        val result = aiPlayer.makeMove(state, player, emptyList())

        assertNull(result)
    }

    @Test
    fun `makeMove advances virtual time by the configured thinking delay`() = runTest {
        val aiPlayer = AiPlayer(EasyAi(), thinkingDelayMs = 1000)
        val validMoves = listOf(ValidMove(Domino(1, 2), BoardEnd.RIGHT, false))

        aiPlayer.makeMove(state, player, validMoves)

        assertEquals(1000L, currentTime)
    }

    @Test
    fun `makeMove with zero delay completes without advancing virtual time`() = runTest {
        val aiPlayer = AiPlayer(EasyAi(), thinkingDelayMs = 0)
        val validMoves = listOf(ValidMove(Domino(1, 2), BoardEnd.RIGHT, false))

        aiPlayer.makeMove(state, player, validMoves)

        assertEquals(0L, currentTime)
    }
}

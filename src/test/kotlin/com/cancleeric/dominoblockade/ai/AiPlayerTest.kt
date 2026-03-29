package com.cancleeric.dominoblockade.ai

import com.cancleeric.dominoblockade.domain.model.Domino
import com.cancleeric.dominoblockade.domain.model.GameState
import com.cancleeric.dominoblockade.domain.model.GameStatus
import com.cancleeric.dominoblockade.domain.model.Player
import com.cancleeric.dominoblockade.domain.usecase.BoardEnd
import com.cancleeric.dominoblockade.domain.usecase.ValidMove
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AiPlayerTest {

    private fun makeState(hand: List<Domino>) = GameState(
        players = listOf(Player(id = 0, name = "AI", hand = hand, isAi = true)),
        status = GameStatus.PLAYING
    )

    @Test
    fun `makeMove returns null when no valid moves available`() = runTest {
        val aiPlayer = AiPlayer(EasyAi(), thinkingDelayMs = 0)
        val state = makeState(emptyList())
        val result = aiPlayer.makeMove(state, state.players[0], emptyList())
        assertNull(result)
    }

    @Test
    fun `makeMove returns a move from the valid moves list`() = runTest {
        val domino = Domino(left = 2, right = 4, id = 7)
        val move = ValidMove(domino, BoardEnd.RIGHT, false)
        val aiPlayer = AiPlayer(EasyAi(), thinkingDelayMs = 0)
        val state = makeState(listOf(domino))
        val result = aiPlayer.makeMove(state, state.players[0], listOf(move))
        assertNotNull(result)
        assertEquals(move, result)
    }

    @Test
    fun `makeMove respects thinkingDelayMs`() = runTest {
        val domino = Domino(left = 1, right = 3, id = 3)
        val move = ValidMove(domino, BoardEnd.RIGHT, false)
        val aiPlayer = AiPlayer(EasyAi(), thinkingDelayMs = 1000)
        val state = makeState(listOf(domino))

        val startTime = testScheduler.currentTime
        val result = aiPlayer.makeMove(state, state.players[0], listOf(move))
        val elapsedMs = testScheduler.currentTime - startTime

        assertNotNull(result)
        assertTrue(elapsedMs >= 1000L, "Expected at least 1000ms delay but got ${elapsedMs}ms")
    }

    @Test
    fun `makeMove delegates move selection to the underlying strategy`() = runTest {
        // MediumAi should prefer the double over the non-double
        val nonDouble = Domino(left = 5, right = 6, id = 0)
        val double = Domino(left = 4, right = 4, id = 1)
        val moves = listOf(
            ValidMove(nonDouble, BoardEnd.RIGHT, false),
            ValidMove(double, BoardEnd.RIGHT, false)
        )
        val aiPlayer = AiPlayer(MediumAi(), thinkingDelayMs = 0)
        val state = makeState(listOf(nonDouble, double))
        val result = aiPlayer.makeMove(state, state.players[0], moves)
        assertNotNull(result)
        assertEquals(double, result?.domino)
    }
}

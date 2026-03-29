package com.cancleeric.dominoblockade.ai

import com.cancleeric.dominoblockade.domain.model.Domino
import com.cancleeric.dominoblockade.domain.model.GameState
import com.cancleeric.dominoblockade.domain.model.GameStatus
import com.cancleeric.dominoblockade.domain.model.Player
import com.cancleeric.dominoblockade.domain.usecase.BoardEnd
import com.cancleeric.dominoblockade.domain.usecase.ValidMove
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MediumAiTest {

    private val ai = MediumAi()

    private fun makeState(hand: List<Domino>) = GameState(
        players = listOf(Player(id = 0, name = "AI", hand = hand, isAi = true)),
        status = GameStatus.PLAYING
    )

    @Test
    fun `chooseMove returns null when validMoves is empty`() {
        val state = makeState(emptyList())
        val result = ai.chooseMove(state, state.players[0], emptyList())
        assertNull(result)
    }

    @Test
    fun `chooseMove selects the highest pip non-double when no doubles available`() {
        val low = Domino(left = 1, right = 2, id = 0)  // totalPips = 3
        val mid = Domino(left = 3, right = 4, id = 1)  // totalPips = 7
        val high = Domino(left = 5, right = 6, id = 2) // totalPips = 11

        val moves = listOf(
            ValidMove(low, BoardEnd.RIGHT, false),
            ValidMove(mid, BoardEnd.RIGHT, false),
            ValidMove(high, BoardEnd.RIGHT, false)
        )
        val state = makeState(listOf(low, mid, high))
        val result = ai.chooseMove(state, state.players[0], moves)
        assertEquals(high, result?.domino)
    }

    @Test
    fun `chooseMove prefers doubles over non-doubles even when non-double has higher pips`() {
        val nonDouble = Domino(left = 5, right = 6, id = 0) // totalPips = 11, not double
        val double = Domino(left = 4, right = 4, id = 1)    // totalPips = 8, double

        val moves = listOf(
            ValidMove(nonDouble, BoardEnd.RIGHT, false),
            ValidMove(double, BoardEnd.RIGHT, false)
        )
        val state = makeState(listOf(nonDouble, double))
        val result = ai.chooseMove(state, state.players[0], moves)
        assertTrue(result?.domino?.isDouble == true)
        assertEquals(double, result?.domino)
    }

    @Test
    fun `chooseMove selects highest-pip double when multiple doubles are available`() {
        val d2 = Domino(left = 2, right = 2, id = 0) // totalPips = 4
        val d5 = Domino(left = 5, right = 5, id = 1) // totalPips = 10
        val d3 = Domino(left = 3, right = 3, id = 2) // totalPips = 6

        val moves = listOf(
            ValidMove(d2, BoardEnd.RIGHT, false),
            ValidMove(d5, BoardEnd.RIGHT, false),
            ValidMove(d3, BoardEnd.RIGHT, false)
        )
        val state = makeState(listOf(d2, d5, d3))
        val result = ai.chooseMove(state, state.players[0], moves)
        assertEquals(d5, result?.domino)
    }

    @Test
    fun `chooseMove with single valid move returns that move`() {
        val domino = Domino(left = 2, right = 3, id = 5)
        val move = ValidMove(domino, BoardEnd.LEFT, false)
        val state = makeState(listOf(domino))
        val result = ai.chooseMove(state, state.players[0], listOf(move))
        assertEquals(move, result)
    }
}

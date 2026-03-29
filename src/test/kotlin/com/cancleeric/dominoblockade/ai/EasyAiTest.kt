package com.cancleeric.dominoblockade.ai

import com.cancleeric.dominoblockade.domain.model.Domino
import com.cancleeric.dominoblockade.domain.model.GameState
import com.cancleeric.dominoblockade.domain.model.GameStatus
import com.cancleeric.dominoblockade.domain.model.Player
import com.cancleeric.dominoblockade.domain.usecase.BoardEnd
import com.cancleeric.dominoblockade.domain.usecase.ValidMove
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class EasyAiTest {

    private val ai = EasyAi()

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
    fun `chooseMove returns a move contained in validMoves`() {
        val domino = Domino(left = 3, right = 5, id = 10)
        val move = ValidMove(domino, BoardEnd.RIGHT, false)
        val state = makeState(listOf(domino))
        val result = ai.chooseMove(state, state.players[0], listOf(move))
        assertNotNull(result)
        assertTrue(result == move)
    }

    @Test
    fun `chooseMove with multiple moves always returns one from the list`() {
        val moves = listOf(
            ValidMove(Domino(1, 2, 0), BoardEnd.RIGHT, false),
            ValidMove(Domino(3, 4, 1), BoardEnd.LEFT, true),
            ValidMove(Domino(5, 6, 2), BoardEnd.RIGHT, false)
        )
        val state = makeState(moves.map { it.domino })
        repeat(20) {
            val result = ai.chooseMove(state, state.players[0], moves)
            assertNotNull(result)
            assertTrue(moves.contains(result))
        }
    }
}

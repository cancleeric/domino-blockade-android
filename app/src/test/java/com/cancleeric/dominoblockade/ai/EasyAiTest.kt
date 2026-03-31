package com.cancleeric.dominoblockade.ai

import com.cancleeric.dominoblockade.domain.model.Domino
import com.cancleeric.dominoblockade.domain.model.GameState
import com.cancleeric.dominoblockade.domain.model.Player
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EasyAiTest {

    private val ai = EasyAi()
    private val player = Player(id = "p0", name = "Player 1", hand = emptyList())
    private val state = GameState(
        players = listOf(player),
        board = emptyList(),
        boneyard = emptyList(),
        currentPlayerIndex = 0
    )

    @Test
    fun `chooseMove returns null when no valid moves are available`() {
        val result = ai.chooseMove(state, player, emptyList())
        assertNull(result)
    }

    @Test
    fun `chooseMove returns a move that is in the provided valid moves list`() {
        val moves = listOf(
            ValidMove(Domino(1, 2), BoardEnd.RIGHT, false),
            ValidMove(Domino(3, 4), BoardEnd.LEFT, true)
        )
        val result = ai.chooseMove(state, player, moves)
        assertTrue(result in moves)
    }

    @Test
    fun `chooseMove returns the only available move when list has one element`() {
        val only = ValidMove(Domino(0, 0), BoardEnd.LEFT, false)
        val result = ai.chooseMove(state, player, listOf(only))
        assertTrue(result == only)
    }
}

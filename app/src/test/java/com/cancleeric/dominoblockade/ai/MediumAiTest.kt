package com.cancleeric.dominoblockade.ai

import com.cancleeric.dominoblockade.domain.model.Domino
import com.cancleeric.dominoblockade.domain.model.GameState
import com.cancleeric.dominoblockade.domain.model.Player
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MediumAiTest {

    private val ai = MediumAi()
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
    fun `chooseMove prefers tile with highest pip count when no doubles available`() {
        val lowPip = ValidMove(Domino(1, 2), BoardEnd.RIGHT, false)  // total = 3
        val highPip = ValidMove(Domino(5, 6), BoardEnd.RIGHT, false)  // total = 11
        val midPip = ValidMove(Domino(3, 4), BoardEnd.RIGHT, false)   // total = 7

        val result = ai.chooseMove(state, player, listOf(lowPip, highPip, midPip))

        assertEquals(highPip, result)
    }

    @Test
    fun `chooseMove prefers double over higher-pip non-double`() {
        val nonDouble = ValidMove(Domino(5, 6), BoardEnd.RIGHT, false)  // total = 11, not double
        val double = ValidMove(Domino(4, 4), BoardEnd.LEFT, false)      // total = 8, double

        val result = ai.chooseMove(state, player, listOf(nonDouble, double))

        assertEquals(double, result)
    }

    @Test
    fun `chooseMove picks highest-pip double when multiple doubles exist`() {
        val lowDouble = ValidMove(Domino(2, 2), BoardEnd.LEFT, false)   // total = 4
        val highDouble = ValidMove(Domino(5, 5), BoardEnd.RIGHT, false) // total = 10

        val result = ai.chooseMove(state, player, listOf(lowDouble, highDouble))

        assertEquals(highDouble, result)
    }

    @Test
    fun `chooseMove picks sole valid move regardless of type`() {
        val only = ValidMove(Domino(0, 1), BoardEnd.RIGHT, false)

        val result = ai.chooseMove(state, player, listOf(only))

        assertEquals(only, result)
    }
}

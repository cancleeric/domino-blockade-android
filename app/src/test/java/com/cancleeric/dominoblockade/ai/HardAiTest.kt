package com.cancleeric.dominoblockade.ai

import com.cancleeric.dominoblockade.domain.model.Domino
import com.cancleeric.dominoblockade.domain.model.GameState
import com.cancleeric.dominoblockade.domain.model.Player
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HardAiTest {

    private val ai = HardAi()

    private fun makePlayer(hand: List<Domino>) =
        Player(id = "p0", name = "Player 1", hand = hand)

    private fun makeState(board: List<Domino>, playerHand: List<Domino>): GameState {
        val player = makePlayer(playerHand)
        val opponent = Player(id = "p1", name = "Player 2", hand = emptyList())
        return GameState(
            players = listOf(player, opponent),
            board = board,
            boneyard = emptyList(),
            currentPlayerIndex = 0
        )
    }

    @Test
    fun `chooseMove returns null when no valid moves are available`() {
        val state = makeState(board = emptyList(), playerHand = emptyList())
        val result = ai.chooseMove(state, makePlayer(emptyList()), emptyList())
        assertNull(result)
    }

    @Test
    fun `chooseMove prefers move that leaves board end opponents are least likely to match`() {
        // Board contains five of the seven tiles with pip value 6 (all but (5,6) and (6,6)).
        // Player holds (0,0) and (5,6).
        // Move A exposes value 6 → opponent can only match with (6,6): score = 1.
        // Move B exposes value 0 → opponent can match with (0,1),(0,2),(0,3),(0,4),(0,5): score = 5.
        // HardAi must choose Move A.
        val boardTiles = listOf(
            Domino(0, 6), Domino(1, 6), Domino(2, 6), Domino(3, 6), Domino(4, 6)
        )
        val playerHand = listOf(Domino(0, 0), Domino(5, 6))
        val state = makeState(board = boardTiles, playerHand = playerHand)
        val player = makePlayer(playerHand)

        val moveA = ValidMove(Domino(5, 6), BoardEnd.RIGHT, false) // newExposedEnd = 6
        val moveB = ValidMove(Domino(0, 0), BoardEnd.RIGHT, false) // newExposedEnd = 0

        val result = ai.chooseMove(state, player, listOf(moveA, moveB))

        assertEquals(moveA, result)
    }

    @Test
    fun `card counting excludes board tiles and own hand from opponent possible set`() {
        // All 28 tiles are on the board or in the player's hand, leaving nothing for the opponent.
        // Two moves with newExposedEnd = 0 and 1 respectively.
        // Since opponentPossible is empty (score = 0 for both), the tie-breaker (higher pip) wins.
        val allDominoes = buildList {
            for (i in 0..6) for (j in i..6) add(Domino(i, j))
        }
        // Give 2 tiles to the player; the rest fill the board.
        val playerHand = allDominoes.takeLast(2)           // (5,6) and (6,6)
        val boardTiles = allDominoes.dropLast(2)           // remaining 26 tiles

        val state = makeState(board = boardTiles, playerHand = playerHand)
        val player = makePlayer(playerHand)

        val moveA = ValidMove(Domino(5, 6), BoardEnd.RIGHT, false) // pip = 11, exposes 6
        val moveB = ValidMove(Domino(6, 6), BoardEnd.RIGHT, false) // pip = 12, exposes 6

        // Both have blocking score 0; tie-breaker: highest pip → moveB wins.
        val result = ai.chooseMove(state, player, listOf(moveA, moveB))

        assertEquals(moveB, result)
    }

    @Test
    fun `chooseMove returns only available move when list has one element`() {
        val state = makeState(board = emptyList(), playerHand = listOf(Domino(1, 2)))
        val player = makePlayer(listOf(Domino(1, 2)))
        val only = ValidMove(Domino(1, 2), BoardEnd.LEFT, false)

        val result = ai.chooseMove(state, player, listOf(only))

        assertEquals(only, result)
    }
}

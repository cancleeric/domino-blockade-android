package com.dominoblockade.domain.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class GameStateTest {

    private val players = listOf(
        Player(id = 0, name = "Alice"),
        Player(id = 1, name = "Bob")
    )

    @Test
    fun `GameState initial defaults are correct`() {
        val state = GameState(players = players)
        assertTrue(state.board.isEmpty())
        assertTrue(state.drawPile.isEmpty())
        assertEquals(0, state.currentPlayerIndex)
        assertNull(state.leftEnd)
        assertNull(state.rightEnd)
        assertEquals(GameStatus.WAITING, state.status)
    }

    @Test
    fun `GameState stores players correctly`() {
        val state = GameState(players = players)
        assertEquals(2, state.players.size)
        assertEquals("Alice", state.players[0].name)
        assertEquals("Bob", state.players[1].name)
    }

    @Test
    fun `GameState can transition to PLAYING status`() {
        val state = GameState(players = players, status = GameStatus.PLAYING)
        assertEquals(GameStatus.PLAYING, state.status)
    }

    @Test
    fun `GameState can transition to BLOCKED status`() {
        val state = GameState(players = players, status = GameStatus.BLOCKED)
        assertEquals(GameStatus.BLOCKED, state.status)
    }

    @Test
    fun `GameState can transition to FINISHED status`() {
        val state = GameState(players = players, status = GameStatus.FINISHED)
        assertEquals(GameStatus.FINISHED, state.status)
    }

    @Test
    fun `GameState tracks board dominoes`() {
        val board = listOf(
            Domino(left = 6, right = 6, id = 0),
            Domino(left = 6, right = 4, id = 1)
        )
        val state = GameState(players = players, board = board, leftEnd = 6, rightEnd = 4)
        assertEquals(2, state.board.size)
        assertEquals(6, state.leftEnd)
        assertEquals(4, state.rightEnd)
    }

    @Test
    fun `GameState tracks draw pile`() {
        val drawPile = DominoSet.createFullSet()
        val state = GameState(players = players, drawPile = drawPile)
        assertEquals(28, state.drawPile.size)
    }

    @Test
    fun `GameState currentPlayerIndex can advance`() {
        val state = GameState(players = players, currentPlayerIndex = 1)
        assertEquals(1, state.currentPlayerIndex)
    }

    @Test
    fun `GameStatus enum contains all required values`() {
        val statuses = GameStatus.values()
        assertTrue(statuses.contains(GameStatus.WAITING))
        assertTrue(statuses.contains(GameStatus.PLAYING))
        assertTrue(statuses.contains(GameStatus.BLOCKED))
        assertTrue(statuses.contains(GameStatus.FINISHED))
    }
}

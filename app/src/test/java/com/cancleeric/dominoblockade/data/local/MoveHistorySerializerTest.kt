package com.cancleeric.dominoblockade.data.local

import com.cancleeric.dominoblockade.domain.model.Domino
import com.cancleeric.dominoblockade.domain.model.GameMove
import com.cancleeric.dominoblockade.domain.model.Player
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MoveHistorySerializerTest {

    // ── GameMove round-trip ──────────────────────────────────────────────────

    @Test
    fun `serializeMoves with empty list produces empty JSON array`() {
        val result = MoveHistorySerializer.serializeMoves(emptyList())
        assertEquals("[]", result)
    }

    @Test
    fun `deserializeMoves with empty JSON array returns empty list`() {
        val result = MoveHistorySerializer.deserializeMoves("[]")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `serializeMoves and deserializeMoves round-trip preserves single move`() {
        val move = GameMove(
            playerId = "p1",
            playerName = "Player 1",
            dominoLeft = 3,
            dominoRight = 5,
            boardEnd = "RIGHT",
            timestamp = 1000L
        )
        val json = MoveHistorySerializer.serializeMoves(listOf(move))
        val result = MoveHistorySerializer.deserializeMoves(json)

        assertEquals(1, result.size)
        assertEquals(move, result[0])
    }

    @Test
    fun `round-trip preserves multiple moves with all board-end types`() {
        val moves = listOf(
            GameMove("p1", "Player 1", 6, 6, "INITIAL", 1000L),
            GameMove("p2", "Player 2", 4, 6, "RIGHT", 2000L),
            GameMove("p1", "Player 1", 1, 4, "LEFT", 3000L)
        )
        val json = MoveHistorySerializer.serializeMoves(moves)
        val result = MoveHistorySerializer.deserializeMoves(json)

        assertEquals(moves.size, result.size)
        moves.zip(result).forEach { (expected, actual) ->
            assertEquals(expected, actual)
        }
    }

    @Test
    fun `round-trip handles player name with spaces`() {
        val move = GameMove("id_1", "Alice Smith", 0, 1, "LEFT", 5000L)
        val json = MoveHistorySerializer.serializeMoves(listOf(move))
        val result = MoveHistorySerializer.deserializeMoves(json)

        assertEquals(1, result.size)
        assertEquals("Alice Smith", result[0].playerName)
    }

    @Test
    fun `round-trip handles zero pip domino`() {
        val move = GameMove("p1", "Player 1", 0, 0, "INITIAL", 100L)
        val json = MoveHistorySerializer.serializeMoves(listOf(move))
        val result = MoveHistorySerializer.deserializeMoves(json)

        assertEquals(0, result[0].dominoLeft)
        assertEquals(0, result[0].dominoRight)
    }

    // ── Player hand round-trip ───────────────────────────────────────────────

    @Test
    fun `serializePlayers with empty list produces empty JSON array`() {
        val result = MoveHistorySerializer.serializePlayers(emptyList())
        assertEquals("[]", result)
    }

    @Test
    fun `serializePlayers and deserializePlayers round-trip preserves players with hands`() {
        val players = listOf(
            Player("p1", "Player 1", listOf(Domino(1, 2), Domino(3, 4))),
            Player("p2", "Player 2", listOf(Domino(5, 6)))
        )
        val json = MoveHistorySerializer.serializePlayers(players)
        val result = MoveHistorySerializer.deserializePlayers(json)

        assertEquals(2, result.size)
        assertEquals("p1", result[0].id)
        assertEquals("Player 1", result[0].name)
        assertEquals(2, result[0].hand.size)
        assertEquals(Domino(1, 2), result[0].hand[0])
        assertEquals(Domino(3, 4), result[0].hand[1])
        assertEquals("p2", result[1].id)
        assertEquals(1, result[1].hand.size)
        assertEquals(Domino(5, 6), result[1].hand[0])
    }

    @Test
    fun `round-trip preserves player with empty hand`() {
        val players = listOf(Player("p1", "Player 1", emptyList()))
        val json = MoveHistorySerializer.serializePlayers(players)
        val result = MoveHistorySerializer.deserializePlayers(json)

        assertEquals(1, result.size)
        assertTrue(result[0].hand.isEmpty())
    }

    @Test
    fun `deserializeMoves returns empty list for blank string`() {
        assertTrue(MoveHistorySerializer.deserializeMoves("").isEmpty())
        assertTrue(MoveHistorySerializer.deserializeMoves("   ").isEmpty())
    }

    @Test
    fun `deserializePlayers returns empty list for blank string`() {
        assertTrue(MoveHistorySerializer.deserializePlayers("").isEmpty())
    }
}

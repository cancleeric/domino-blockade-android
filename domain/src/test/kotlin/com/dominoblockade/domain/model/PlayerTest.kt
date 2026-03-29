package com.dominoblockade.domain.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PlayerTest {

    @Test
    fun `Player creation stores id, name, and defaults`() {
        val player = Player(id = 1, name = "Alice")
        assertEquals(1, player.id)
        assertEquals("Alice", player.name)
        assertTrue(player.hand.isEmpty())
        assertFalse(player.isAi)
        assertEquals(0, player.score)
    }

    @Test
    fun `Player can be created as AI`() {
        val ai = Player(id = 2, name = "Bot", isAi = true)
        assertTrue(ai.isAi)
    }

    @Test
    fun `Player stores initial hand`() {
        val hand = listOf(
            Domino(left = 1, right = 2, id = 0),
            Domino(left = 3, right = 4, id = 1)
        )
        val player = Player(id = 1, name = "Alice", hand = hand)
        assertEquals(2, player.hand.size)
        assertEquals(hand, player.hand)
    }

    @Test
    fun `Player with score reflects correct value`() {
        val player = Player(id = 1, name = "Alice", score = 42)
        assertEquals(42, player.score)
    }

    @Test
    fun `Player copy adds domino to hand`() {
        val player = Player(id = 1, name = "Alice")
        val newDomino = Domino(left = 2, right = 5, id = 10)
        val updated = player.copy(hand = player.hand + newDomino)
        assertEquals(1, updated.hand.size)
        assertEquals(newDomino, updated.hand.first())
    }

    @Test
    fun `Player copy removes domino from hand`() {
        val domino1 = Domino(left = 1, right = 2, id = 0)
        val domino2 = Domino(left = 3, right = 4, id = 1)
        val player = Player(id = 1, name = "Alice", hand = listOf(domino1, domino2))
        val updated = player.copy(hand = player.hand - domino1)
        assertEquals(1, updated.hand.size)
        assertEquals(domino2, updated.hand.first())
    }

    @Test
    fun `Player equality based on data class fields`() {
        val p1 = Player(id = 1, name = "Alice")
        val p2 = Player(id = 1, name = "Alice")
        assertEquals(p1, p2)
    }
}

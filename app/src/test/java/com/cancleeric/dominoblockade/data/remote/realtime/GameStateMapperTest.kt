package com.cancleeric.dominoblockade.data.remote.realtime

import com.cancleeric.dominoblockade.domain.model.Domino
import com.cancleeric.dominoblockade.domain.model.GameState
import com.cancleeric.dominoblockade.domain.model.Player
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class GameStateMapperTest {

    @Test
    fun `dominoToMap serializes left and right values`() {
        val domino = Domino(3, 5)
        val map = dominoToMap(domino)
        assertEquals(3, map["left"])
        assertEquals(5, map["right"])
    }

    @Test
    fun `mapToDomino deserializes correctly`() {
        val map = mapOf("left" to 2L, "right" to 6L)
        val domino = mapToDomino(map)
        assertEquals(2, domino.left)
        assertEquals(6, domino.right)
    }

    @Test
    fun `mapToDomino with missing keys returns zero values`() {
        val domino = mapToDomino(emptyMap<String, Any>())
        assertEquals(0, domino.left)
        assertEquals(0, domino.right)
    }

    @Test
    fun `playerToMap serializes id name and hand`() {
        val player = Player("p1", "Alice", listOf(Domino(1, 2), Domino(3, 4)))
        val map = playerToMap(player)
        assertEquals("p1", map["id"])
        assertEquals("Alice", map["name"])
        @Suppress("UNCHECKED_CAST")
        val hand = map["hand"] as List<Map<String, Int>>
        assertEquals(2, hand.size)
        assertEquals(1, hand[0]["left"])
    }

    @Test
    fun `mapToPlayer deserializes correctly`() {
        val handMaps = listOf(mapOf("left" to 1L, "right" to 2L))
        val map = mapOf("id" to "p1", "name" to "Bob", "hand" to handMaps)
        val player = mapToPlayer(map)
        assertEquals("p1", player.id)
        assertEquals("Bob", player.name)
        assertEquals(1, player.hand.size)
        assertEquals(Domino(1, 2), player.hand[0])
    }

    @Test
    fun `gameStateToMap round-trip preserves currentPlayerIndex`() {
        val state = buildSimpleGameState()
        val map = gameStateToMap(state)
        assertEquals(1, map["currentPlayerIndex"])
    }

    @Test
    fun `gameStateToMap stores board and boneyard lists`() {
        val state = buildSimpleGameState()
        val map = gameStateToMap(state)
        @Suppress("UNCHECKED_CAST")
        val board = map["board"] as List<*>
        @Suppress("UNCHECKED_CAST")
        val boneyard = map["boneyard"] as List<*>
        assertEquals(1, board.size)
        assertEquals(2, boneyard.size)
    }

    @Test
    fun `gameStateToMap stores players list`() {
        val state = buildSimpleGameState()
        val map = gameStateToMap(state)
        @Suppress("UNCHECKED_CAST")
        val players = map["players"] as List<*>
        assertEquals(2, players.size)
    }

    private fun buildSimpleGameState(): GameState {
        val p1 = Player("p0", "Alice", listOf(Domino(0, 1)))
        val p2 = Player("p1", "Bob", listOf(Domino(2, 3)))
        return GameState(
            players = listOf(p1, p2),
            board = listOf(Domino(4, 5)),
            boneyard = listOf(Domino(0, 0), Domino(6, 6)),
            currentPlayerIndex = 1,
            leftEnd = 4,
            rightEnd = 5
        )
    }
}

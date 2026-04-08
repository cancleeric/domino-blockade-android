package com.cancleeric.dominoblockade.domain.usecase

import com.cancleeric.dominoblockade.domain.model.Domino
import com.cancleeric.dominoblockade.domain.model.GameState
import com.cancleeric.dominoblockade.domain.model.Player
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class BlockadeDetectorTest {

    private val detector = BlockadeDetector()

    private fun stateWith(
        playerHands: List<List<Domino>>,
        board: List<Domino> = emptyList(),
        boneyard: List<Domino> = emptyList()
    ): GameState {
        val players = playerHands.mapIndexed { i, hand -> Player("p${i + 1}", "P${i + 1}", hand) }
        return GameState(
            players = players,
            board = board,
            boneyard = boneyard,
            currentPlayerIndex = 0
        )
    }

    @Test
    fun `not blocked when boneyard is non-empty`() {
        val state = stateWith(
            playerHands = listOf(listOf(Domino(1, 2)), listOf(Domino(3, 4))),
            board = listOf(Domino(5, 5)),
            boneyard = listOf(Domino(0, 1))
        )
        assertFalse(detector.isBlocked(state))
    }

    @Test
    fun `not blocked when a player has a valid move`() {
        val board = listOf(Domino(3, 5))
        val state = stateWith(
            playerHands = listOf(listOf(Domino(5, 2)), listOf(Domino(1, 1))),
            board = board,
            boneyard = emptyList()
        )
        assertFalse(detector.isBlocked(state))
    }

    @Test
    fun `blocked when boneyard empty and no player has valid move`() {
        val board = listOf(Domino(2, 4))
        val state = stateWith(
            playerHands = listOf(listOf(Domino(1, 3)), listOf(Domino(0, 0))),
            board = board,
            boneyard = emptyList()
        )
        assertTrue(detector.isBlocked(state))
    }

    @Test
    fun `not blocked when board is empty and players have tiles`() {
        val state = stateWith(
            playerHands = listOf(listOf(Domino(1, 2)), listOf(Domino(3, 4))),
            board = emptyList(),
            boneyard = emptyList()
        )
        assertFalse(detector.isBlocked(state))
    }

    @Test
    fun `blocked when all hands empty and boneyard empty`() {
        val board = listOf(Domino(1, 2))
        val state = stateWith(
            playerHands = listOf(emptyList(), emptyList()),
            board = board,
            boneyard = emptyList()
        )
        // All players have empty hands → no valid moves → blocked
        assertTrue(detector.isBlocked(state))
    }

    @Test
    fun `not blocked with boneyard tiles even if no valid moves`() {
        val board = listOf(Domino(6, 6))
        val state = stateWith(
            playerHands = listOf(listOf(Domino(0, 0)), listOf(Domino(1, 1))),
            board = board,
            boneyard = listOf(Domino(5, 6))
        )
        assertFalse(detector.isBlocked(state))
    }
}

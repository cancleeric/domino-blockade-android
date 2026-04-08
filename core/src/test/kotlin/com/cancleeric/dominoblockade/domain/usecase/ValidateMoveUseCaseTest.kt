package com.cancleeric.dominoblockade.domain.usecase

import com.cancleeric.dominoblockade.domain.model.Domino
import com.cancleeric.dominoblockade.domain.model.Player
import com.cancleeric.dominoblockade.domain.model.GameState
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ValidateMoveUseCaseTest {

    private val useCase = ValidateMoveUseCase()

    private fun stateWith(
        board: List<Domino> = emptyList(),
        boneyard: List<Domino> = emptyList(),
        hand: List<Domino> = emptyList(),
        currentPlayerIndex: Int = 0
    ): GameState {
        val player = Player("p1", "Player 1", hand)
        return GameState(
            players = listOf(player),
            board = board,
            boneyard = boneyard,
            currentPlayerIndex = currentPlayerIndex
        )
    }

    @Nested
    inner class Validate {

        @Test
        fun `any domino is valid on empty board for LEFT`() {
            val state = stateWith()
            assertTrue(useCase.validate(state, Domino(3, 4), BoardEnd.LEFT))
        }

        @Test
        fun `any domino is valid on empty board for RIGHT`() {
            val state = stateWith()
            assertTrue(useCase.validate(state, Domino(1, 5), BoardEnd.RIGHT))
        }

        @Test
        fun `domino matching left end is valid on LEFT`() {
            val board = listOf(Domino(5, 3))
            val state = stateWith(board = board)
            assertTrue(useCase.validate(state, Domino(2, 5), BoardEnd.LEFT))
        }

        @Test
        fun `domino with right matching left end is valid on LEFT`() {
            val board = listOf(Domino(5, 3))
            val state = stateWith(board = board)
            assertTrue(useCase.validate(state, Domino(5, 2), BoardEnd.LEFT))
        }

        @Test
        fun `domino not matching left end is invalid on LEFT`() {
            val board = listOf(Domino(5, 3))
            val state = stateWith(board = board)
            assertFalse(useCase.validate(state, Domino(2, 4), BoardEnd.LEFT))
        }

        @Test
        fun `domino matching right end is valid on RIGHT`() {
            val board = listOf(Domino(1, 6))
            val state = stateWith(board = board)
            assertTrue(useCase.validate(state, Domino(6, 2), BoardEnd.RIGHT))
        }

        @Test
        fun `domino not matching right end is invalid on RIGHT`() {
            val board = listOf(Domino(1, 6))
            val state = stateWith(board = board)
            assertFalse(useCase.validate(state, Domino(2, 3), BoardEnd.RIGHT))
        }
    }

    @Nested
    inner class GetValidMoves {

        @Test
        fun `all tiles are valid moves on empty board`() {
            val hand = listOf(Domino(1, 2), Domino(3, 4), Domino(5, 6))
            val state = stateWith(hand = hand)
            val moves = useCase.getValidMoves(state, state.currentPlayer)
            assertEquals(3, moves.size)
            moves.forEach { assertEquals(BoardEnd.LEFT, it.end) }
        }

        @Test
        fun `no valid moves when hand is empty`() {
            val board = listOf(Domino(3, 5))
            val state = stateWith(board = board)
            val moves = useCase.getValidMoves(state, state.currentPlayer)
            assertTrue(moves.isEmpty())
        }

        @Test
        fun `domino matching left end generates LEFT move`() {
            val board = listOf(Domino(5, 3))
            val hand = listOf(Domino(2, 5))
            val state = stateWith(board = board, hand = hand)
            val moves = useCase.getValidMoves(state, state.currentPlayer)
            assertTrue(moves.any { it.end == BoardEnd.LEFT })
        }

        @Test
        fun `domino matching right end generates RIGHT move`() {
            val board = listOf(Domino(1, 4))
            val hand = listOf(Domino(4, 6))
            val state = stateWith(board = board, hand = hand)
            val moves = useCase.getValidMoves(state, state.currentPlayer)
            assertTrue(moves.any { it.end == BoardEnd.RIGHT })
        }

        @Test
        fun `flipped domino generates move with needsFlip=true`() {
            val board = listOf(Domino(5, 3))
            val hand = listOf(Domino(5, 2))
            val state = stateWith(board = board, hand = hand)
            val moves = useCase.getValidMoves(state, state.currentPlayer)
            assertTrue(moves.any { it.end == BoardEnd.LEFT && it.needsFlip })
        }

        @Test
        fun `double domino generates single LEFT move when matching left end`() {
            val board = listOf(Domino(6, 3))
            val hand = listOf(Domino(6, 6))
            val state = stateWith(board = board, hand = hand)
            val moves = useCase.getValidMoves(state, state.currentPlayer)
            assertEquals(1, moves.count { it.end == BoardEnd.LEFT })
        }
    }
}

package com.cancleeric.dominoblockade.ai

import com.cancleeric.dominoblockade.domain.model.Domino
import com.cancleeric.dominoblockade.domain.model.DominoSet
import com.cancleeric.dominoblockade.domain.model.GameState
import com.cancleeric.dominoblockade.domain.model.GameStatus
import com.cancleeric.dominoblockade.domain.model.Player
import com.cancleeric.dominoblockade.domain.usecase.BoardEnd
import com.cancleeric.dominoblockade.domain.usecase.ValidMove
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class HardAiTest {

    private lateinit var ai: HardAi

    @BeforeEach
    fun setUp() {
        ai = HardAi()
    }

    private fun makeState(hand: List<Domino>, board: List<Domino> = emptyList()): GameState {
        val leftEnd = board.firstOrNull()?.left
        val rightEnd = board.lastOrNull()?.right
        return GameState(
            players = listOf(Player(id = 0, name = "AI", hand = hand, isAi = true)),
            board = board,
            leftEnd = leftEnd,
            rightEnd = rightEnd,
            status = GameStatus.PLAYING
        )
    }

    @Test
    fun `chooseMove returns null when validMoves is empty`() {
        val state = makeState(emptyList())
        val result = ai.chooseMove(state, state.players[0], emptyList())
        assertNull(result)
    }

    @Test
    fun `chooseMove returns a move from validMoves`() {
        val domino = Domino(left = 3, right = 5, id = 10)
        val move = ValidMove(domino, BoardEnd.RIGHT, false)
        val state = makeState(listOf(domino))
        val result = ai.chooseMove(state, state.players[0], listOf(move))
        assertNotNull(result)
        assertTrue(listOf(move).contains(result))
    }

    @Test
    fun `trackedBoardSize increases after chooseMove is called with a non-empty board`() {
        // Build a board with two dominoes already placed
        val boardDomino1 = Domino(left = 1, right = 2, id = 0)
        val boardDomino2 = Domino(left = 2, right = 3, id = 1)
        val handDomino = Domino(left = 3, right = 4, id = 2)

        val state = makeState(
            hand = listOf(handDomino),
            board = listOf(boardDomino1, boardDomino2)
        )
        val move = ValidMove(handDomino, BoardEnd.RIGHT, false)

        assertEquals(0, ai.trackedBoardSize())
        ai.chooseMove(state, state.players[0], listOf(move))
        assertEquals(2, ai.trackedBoardSize())
    }

    @Test
    fun `card tracking accumulates across multiple chooseMove calls`() {
        val board1 = listOf(Domino(left = 1, right = 2, id = 0))
        val board2 = listOf(Domino(left = 1, right = 2, id = 0), Domino(left = 2, right = 3, id = 1))

        val hand = listOf(Domino(left = 3, right = 4, id = 2))
        val move = ValidMove(hand[0], BoardEnd.RIGHT, false)

        val state1 = makeState(hand, board1)
        ai.chooseMove(state1, state1.players[0], listOf(move))
        assertEquals(1, ai.trackedBoardSize())

        val state2 = makeState(hand, board2)
        ai.chooseMove(state2, state2.players[0], listOf(move))
        assertEquals(2, ai.trackedBoardSize())
    }

    @Test
    fun `blocking strategy prefers move that exposes a rare end value`() {
        // All 28 dominoes known. AI holds (0,1) and (0,6).
        // Only one unknown domino contains value 6 (apart from AI's own (0,6)).
        // Many unknown dominoes contain value 0.
        // Exposing a 6 at the new end is a stronger block than exposing a 0.
        val fullSet = DominoSet.createFullSet()

        // AI hand: (0,1) id=1  and  (0,6) id=6
        val d01 = fullSet.first { it.left == 0 && it.right == 1 } // id=1, pips=1
        val d06 = fullSet.first { it.left == 0 && it.right == 6 } // id=6, pips=6

        // Board with rightEnd = 0 so both dominoes can be played at RIGHT end
        val boardDomino = Domino(left = 5, right = 0, id = 99) // synthetic board domino
        val state = GameState(
            players = listOf(Player(id = 0, name = "AI", hand = listOf(d01, d06), isAi = true)),
            board = listOf(boardDomino),
            leftEnd = 5,
            rightEnd = 0,
            status = GameStatus.PLAYING
        )

        // Both moves play at RIGHT (no flip), connecting to rightEnd=0
        // d01 placed at RIGHT no-flip: left(=0) connects → new rightEnd = right(=1)
        // d06 placed at RIGHT no-flip: left(=0) connects → new rightEnd = right(=6)
        val moveD01 = ValidMove(d01, BoardEnd.RIGHT, false) // exposes 1
        val moveD06 = ValidMove(d06, BoardEnd.RIGHT, false) // exposes 6

        val result = ai.chooseMove(state, state.players[0], listOf(moveD01, moveD06))
        // Exposing 6 is a stronger block because very few remaining dominoes have value 6
        assertNotNull(result)
        assertEquals(d06, result?.domino)
    }
}

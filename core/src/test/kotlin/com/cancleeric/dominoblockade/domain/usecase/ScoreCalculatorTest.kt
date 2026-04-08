package com.cancleeric.dominoblockade.domain.usecase

import com.cancleeric.dominoblockade.domain.model.Domino
import com.cancleeric.dominoblockade.domain.model.GameState
import com.cancleeric.dominoblockade.domain.model.Player
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ScoreCalculatorTest {

    private val calculator = ScoreCalculator()

    private fun stateWith(playerHands: List<List<Domino>>): GameState {
        val players = playerHands.mapIndexed { i, hand -> Player("p${i + 1}", "P${i + 1}", hand) }
        return GameState(
            players = players,
            board = listOf(Domino(1, 1)),
            boneyard = emptyList(),
            currentPlayerIndex = 0
        )
    }

    @Test
    fun `normal win winner scores sum of other players pips`() {
        val winner = listOf<Domino>()
        val loser = listOf(Domino(3, 4), Domino(2, 1))
        val state = stateWith(listOf(winner, loser))
        val scores = calculator.calculateScores(state)
        assertEquals(10, scores[state.players[0]])
        assertEquals(0, scores[state.players[1]])
    }

    @Test
    fun `normal win with multiple losers scores all pip sums`() {
        val winner = listOf<Domino>()
        val loser1 = listOf(Domino(1, 2))
        val loser2 = listOf(Domino(3, 3))
        val state = stateWith(listOf(winner, loser1, loser2))
        val scores = calculator.calculateScores(state)
        assertEquals(9, scores[state.players[0]])  // 3 + 6
        assertEquals(0, scores[state.players[1]])
        assertEquals(0, scores[state.players[2]])
    }

    @Test
    fun `blockade win player with fewest pips wins`() {
        val low = listOf(Domino(1, 1))       // pips = 2
        val high = listOf(Domino(5, 6))      // pips = 11
        val state = stateWith(listOf(low, high))
        val scores = calculator.calculateScores(state)
        assertEquals(9, scores[state.players[0]])  // 11 - 2 = 9
        assertEquals(0, scores[state.players[1]])
    }

    @Test
    fun `blockade win with tie picks first player`() {
        val hand = listOf(Domino(1, 1))
        val state = stateWith(listOf(hand, hand))
        val scores = calculator.calculateScores(state)
        // winner score = (2 - 2) = 0, loser = 0
        assertEquals(0, scores[state.players[0]])
        assertEquals(0, scores[state.players[1]])
    }

    @Test
    fun `winner with empty hand scores double zeros for losers`() {
        val state = stateWith(listOf(emptyList(), emptyList()))
        val scores = calculator.calculateScores(state)
        // First player has empty hand → wins with 0 pips from the other empty hand
        assertEquals(0, scores[state.players[0]])
        assertEquals(0, scores[state.players[1]])
    }

    @Test
    fun `double domino contributes double pips to loser score`() {
        val winner = listOf<Domino>()
        val loser = listOf(Domino(6, 6))
        val state = stateWith(listOf(winner, loser))
        val scores = calculator.calculateScores(state)
        assertEquals(12, scores[state.players[0]])
    }

    @Test
    fun `scores map contains all players`() {
        val state = stateWith(listOf(listOf(Domino(1, 2)), listOf(Domino(3, 4)), listOf(Domino(0, 0))))
        val scores = calculator.calculateScores(state)
        assertEquals(3, scores.size)
        state.players.forEach { assertTrue(scores.containsKey(it)) }
    }
}

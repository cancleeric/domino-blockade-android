package com.cancleeric.dominoblockade.domain.usecase

import com.cancleeric.dominoblockade.domain.model.Domino
import com.cancleeric.dominoblockade.domain.model.GameState
import com.cancleeric.dominoblockade.domain.model.Player
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class DrawUseCaseTest {

    private val useCase = DrawUseCase()

    private fun stateWith(
        hand: List<Domino> = emptyList(),
        boneyard: List<Domino> = emptyList(),
        players: List<Player>? = null,
        currentPlayerIndex: Int = 0
    ): GameState {
        val ps = players ?: listOf(Player("p1", "Player 1", hand), Player("p2", "Player 2"))
        return GameState(
            players = ps,
            board = emptyList(),
            boneyard = boneyard,
            currentPlayerIndex = currentPlayerIndex
        )
    }

    @Test
    fun `drawing from non-empty boneyard adds tile to current player hand`() {
        val boneyard = listOf(Domino(1, 2), Domino(3, 4))
        val state = stateWith(boneyard = boneyard)
        val newState = useCase.draw(state)
        assertEquals(1, newState.players[0].hand.size)
        assertEquals(Domino(1, 2), newState.players[0].hand.first())
    }

    @Test
    fun `drawing reduces boneyard size by one`() {
        val boneyard = listOf(Domino(1, 2), Domino(3, 4))
        val state = stateWith(boneyard = boneyard)
        val newState = useCase.draw(state)
        assertEquals(1, newState.boneyard.size)
    }

    @Test
    fun `drawing does not advance turn`() {
        val boneyard = listOf(Domino(1, 2))
        val state = stateWith(boneyard = boneyard)
        val newState = useCase.draw(state)
        assertEquals(0, newState.currentPlayerIndex)
    }

    @Test
    fun `drawing from empty boneyard skips turn`() {
        val state = stateWith(boneyard = emptyList())
        val newState = useCase.draw(state)
        assertEquals(1, newState.currentPlayerIndex)
    }

    @Test
    fun `drawing from empty boneyard wraps turn around`() {
        val players = listOf(Player("p1", "P1"), Player("p2", "P2"))
        val state = stateWith(players = players, currentPlayerIndex = 1, boneyard = emptyList())
        val newState = useCase.draw(state)
        assertEquals(0, newState.currentPlayerIndex)
    }

    @Test
    fun `drawing from empty boneyard does not change hands`() {
        val hand = listOf(Domino(5, 6))
        val state = stateWith(hand = hand, boneyard = emptyList())
        val newState = useCase.draw(state)
        assertEquals(1, newState.players[0].hand.size)
    }

    @Test
    fun `drawn tile is appended to existing hand`() {
        val existingHand = listOf(Domino(3, 3))
        val boneyard = listOf(Domino(6, 6))
        val players = listOf(Player("p1", "P1", existingHand), Player("p2", "P2"))
        val state = stateWith(players = players, boneyard = boneyard)
        val newState = useCase.draw(state)
        assertEquals(2, newState.players[0].hand.size)
        assertTrue(newState.players[0].hand.contains(Domino(6, 6)))
    }

    @Test
    fun `other players hand is unchanged after draw`() {
        val player2Hand = listOf(Domino(2, 3))
        val players = listOf(
            Player("p1", "P1", emptyList()),
            Player("p2", "P2", player2Hand)
        )
        val boneyard = listOf(Domino(1, 1))
        val state = stateWith(players = players, boneyard = boneyard)
        val newState = useCase.draw(state)
        assertEquals(player2Hand, newState.players[1].hand)
    }
}

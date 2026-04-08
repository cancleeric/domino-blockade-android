package com.cancleeric.dominoblockade.domain.usecase

import com.cancleeric.dominoblockade.domain.model.Domino
import com.cancleeric.dominoblockade.domain.model.GameConfig
import com.cancleeric.dominoblockade.domain.model.Player
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class DealUseCaseTest {

    private val useCase = DealUseCase()

    private fun players(count: Int) = (1..count).map { Player("p$it", "Player $it") }

    @Test
    fun `two-player game deals 7 tiles each`() {
        val state = useCase.deal(GameConfig(players = players(2), randomSeed = 1L))
        state.players.forEach { assertEquals(7, it.hand.size) }
    }

    @Test
    fun `two-player game leaves 14 tiles in boneyard`() {
        val state = useCase.deal(GameConfig(players = players(2), randomSeed = 1L))
        assertEquals(14, state.boneyard.size)
    }

    @Test
    fun `three-player game deals 5 tiles each`() {
        val state = useCase.deal(GameConfig(players = players(3), randomSeed = 2L))
        state.players.forEach { assertEquals(5, it.hand.size) }
    }

    @Test
    fun `three-player game leaves 13 tiles in boneyard`() {
        val state = useCase.deal(GameConfig(players = players(3), randomSeed = 2L))
        assertEquals(13, state.boneyard.size)
    }

    @Test
    fun `four-player game deals 5 tiles each`() {
        val state = useCase.deal(GameConfig(players = players(4), randomSeed = 3L))
        state.players.forEach { assertEquals(5, it.hand.size) }
    }

    @Test
    fun `four-player game leaves 8 tiles in boneyard`() {
        val state = useCase.deal(GameConfig(players = players(4), randomSeed = 3L))
        assertEquals(8, state.boneyard.size)
    }

    @Test
    fun `total tiles dealt plus boneyard equals 28`() {
        val state = useCase.deal(GameConfig(players = players(2), randomSeed = 42L))
        val total = state.players.sumOf { it.hand.size } + state.boneyard.size
        assertEquals(28, total)
    }

    @Test
    fun `no duplicate tiles in dealt game`() {
        val state = useCase.deal(GameConfig(players = players(2), randomSeed = 42L))
        val all: List<Domino> = state.players.flatMap { it.hand } + state.boneyard
        assertEquals(28, all.toSet().size)
    }

    @Test
    fun `different seeds produce different deals`() {
        val state1 = useCase.deal(GameConfig(players = players(2), randomSeed = 1L))
        val state2 = useCase.deal(GameConfig(players = players(2), randomSeed = 2L))
        assertNotEquals(state1.players[0].hand, state2.players[0].hand)
    }

    @Test
    fun `same seed produces same deal`() {
        val state1 = useCase.deal(GameConfig(players = players(2), randomSeed = 99L))
        val state2 = useCase.deal(GameConfig(players = players(2), randomSeed = 99L))
        assertEquals(state1.players[0].hand, state2.players[0].hand)
    }

    @Test
    fun `board is empty after deal`() {
        val state = useCase.deal(GameConfig(players = players(2), randomSeed = 1L))
        assertTrue(state.board.isEmpty())
    }

    @Test
    fun `currentPlayerIndex starts at 0`() {
        val state = useCase.deal(GameConfig(players = players(2), randomSeed = 1L))
        assertEquals(0, state.currentPlayerIndex)
    }
}

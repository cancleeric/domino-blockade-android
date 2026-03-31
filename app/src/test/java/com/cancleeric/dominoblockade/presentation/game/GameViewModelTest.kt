package com.cancleeric.dominoblockade.presentation.game

import com.cancleeric.dominoblockade.domain.usecase.StartGameUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class GameViewModelTest {

    private val viewModel = GameViewModel(StartGameUseCase())

    @Test
    fun `startGame initializes game state with correct player count`() {
        viewModel.startGame(2)
        val state = viewModel.uiState.value.gameState
        assertNotNull(state)
        assertEquals(2, state?.players?.size)
        assertFalse(viewModel.uiState.value.isGameOver)
    }

    @Test
    fun `startGame with three players deals correct hand sizes`() {
        viewModel.startGame(3)
        val state = viewModel.uiState.value.gameState
        assertNotNull(state)
        assertEquals(3, state?.players?.size)
        state?.players?.forEach { player -> assertEquals(5, player.hand.size) }
    }

    @Test
    fun `selectDomino sets selectedDomino in state`() {
        viewModel.startGame(2)
        val domino = viewModel.uiState.value.gameState?.currentPlayer?.hand?.first() ?: return
        viewModel.selectDomino(domino)
        assertEquals(domino, viewModel.uiState.value.selectedDomino)
    }

    @Test
    fun `selectDomino twice deselects the domino`() {
        viewModel.startGame(2)
        val domino = viewModel.uiState.value.gameState?.currentPlayer?.hand?.first() ?: return
        viewModel.selectDomino(domino)
        viewModel.selectDomino(domino)
        assertNull(viewModel.uiState.value.selectedDomino)
    }

    @Test
    fun `placeDomino with no selection does nothing`() {
        viewModel.startGame(2)
        val stateBefore = viewModel.uiState.value.gameState
        viewModel.placeDomino()
        assertEquals(stateBefore, viewModel.uiState.value.gameState)
    }

    @Test
    fun `drawFromBoneyard adds tile to current player hand when boneyard is not empty`() {
        viewModel.startGame(2)
        val stateBefore = viewModel.uiState.value.gameState ?: return
        val handSizeBefore = stateBefore.currentPlayer.hand.size
        val boneyardSizeBefore = stateBefore.boneyard.size
        if (boneyardSizeBefore > 0) {
            viewModel.drawFromBoneyard()
            val stateAfter = viewModel.uiState.value.gameState
            assertEquals(handSizeBefore + 1, stateAfter?.players?.get(stateBefore.currentPlayerIndex)?.hand?.size)
            assertEquals(boneyardSizeBefore - 1, stateAfter?.boneyard?.size)
        }
    }

    @Test
    fun `placeDomino on empty board places first tile and advances turn`() {
        viewModel.startGame(2)
        val state = viewModel.uiState.value.gameState ?: return
        val domino = state.currentPlayer.hand.first()
        viewModel.selectDomino(domino)
        viewModel.placeDomino()
        val afterState = viewModel.uiState.value.gameState ?: return
        assertEquals(1, afterState.board.size)
        assertEquals(1, afterState.currentPlayerIndex)
    }

    @Test
    fun `initial ui state has no game over and no selection`() {
        assertFalse(viewModel.uiState.value.isGameOver)
        assertNull(viewModel.uiState.value.gameState)
        assertNull(viewModel.uiState.value.selectedDomino)
    }

    @Test
    fun `placeDomino removes selected tile from player hand`() {
        viewModel.startGame(2)
        val state = viewModel.uiState.value.gameState ?: return
        val handSizeBefore = state.currentPlayer.hand.size
        viewModel.selectDomino(state.currentPlayer.hand.first())
        viewModel.placeDomino()
        val afterState = viewModel.uiState.value.gameState ?: return
        assertEquals(handSizeBefore - 1, afterState.players[0].hand.size)
    }

    @Test
    fun `selectDomino with different domino switches selection`() {
        viewModel.startGame(2)
        val state = viewModel.uiState.value.gameState ?: return
        if (state.currentPlayer.hand.size < 2) return
        val first = state.currentPlayer.hand[0]
        val second = state.currentPlayer.hand[1]
        viewModel.selectDomino(first)
        viewModel.selectDomino(second)
        assertEquals(second, viewModel.uiState.value.selectedDomino)
    }
}

package com.cancleeric.dominoblockade.presentation.localmultiplayer

import com.cancleeric.dominoblockade.domain.usecase.StartGameUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalMultiplayerViewModelTest {

    private val viewModel = LocalMultiplayerViewModel(StartGameUseCase())

    @Test
    fun `initial state has default player count and no game`() {
        assertEquals(2, viewModel.uiState.value.playerCount)
        assertNull(viewModel.uiState.value.gameState)
        assertFalse(viewModel.uiState.value.isGameOver)
    }

    @Test
    fun `updatePlayerCount changes count and resizes names list`() {
        viewModel.updatePlayerCount(3)
        assertEquals(3, viewModel.uiState.value.playerCount)
        assertEquals(3, viewModel.uiState.value.playerNames.size)
    }

    @Test
    fun `updatePlayerName updates name at given index`() {
        viewModel.updatePlayerName(0, "Alice")
        assertEquals("Alice", viewModel.uiState.value.playerNames[0])
    }

    @Test
    fun `startGame initializes game state with correct player names`() {
        viewModel.updatePlayerCount(2)
        viewModel.updatePlayerName(0, "Alice")
        viewModel.updatePlayerName(1, "Bob")
        viewModel.startGame()
        val state = viewModel.uiState.value.gameState
        assertNotNull(state)
        assertEquals("Alice", state?.players?.get(0)?.name)
        assertEquals("Bob", state?.players?.get(1)?.name)
    }

    @Test
    fun `selectDomino sets selectedDomino`() {
        viewModel.startGame()
        val domino = viewModel.uiState.value.gameState?.currentPlayer?.hand?.first() ?: return
        viewModel.selectDomino(domino)
        assertEquals(domino, viewModel.uiState.value.selectedDomino)
    }

    @Test
    fun `selectDomino twice deselects`() {
        viewModel.startGame()
        val domino = viewModel.uiState.value.gameState?.currentPlayer?.hand?.first() ?: return
        viewModel.selectDomino(domino)
        viewModel.selectDomino(domino)
        assertNull(viewModel.uiState.value.selectedDomino)
    }

    @Test
    fun `placeDomino on empty board sets passAndPlay and advances turn`() {
        viewModel.startGame()
        val domino = viewModel.uiState.value.gameState?.currentPlayer?.hand?.first() ?: return
        viewModel.selectDomino(domino)
        viewModel.placeDomino()
        val state = viewModel.uiState.value
        assertEquals(1, state.gameState?.board?.size)
        assertTrue(state.isPassingDevice)
    }

    @Test
    fun `confirmDevicePassed clears isPassingDevice`() {
        viewModel.startGame()
        val domino = viewModel.uiState.value.gameState?.currentPlayer?.hand?.first() ?: return
        viewModel.selectDomino(domino)
        viewModel.placeDomino()
        viewModel.confirmDevicePassed()
        assertFalse(viewModel.uiState.value.isPassingDevice)
    }

    @Test
    fun `drawFromBoneyard adds tile when boneyard not empty`() {
        viewModel.startGame()
        val stateBefore = viewModel.uiState.value.gameState ?: return
        if (stateBefore.boneyard.isEmpty()) return
        val handSize = stateBefore.currentPlayer.hand.size
        viewModel.drawFromBoneyard()
        assertEquals(handSize + 1, viewModel.uiState.value.gameState?.currentPlayer?.hand?.size)
    }

    @Test
    fun `updatePlayerCount clamps to valid range`() {
        viewModel.updatePlayerCount(5)
        assertEquals(4, viewModel.uiState.value.playerCount)
        viewModel.updatePlayerCount(1)
        assertEquals(2, viewModel.uiState.value.playerCount)
    }

    @Test
    fun `startGame uses blank name fallback`() {
        viewModel.updatePlayerName(0, "")
        viewModel.startGame()
        assertEquals("Player 1", viewModel.uiState.value.gameState?.players?.get(0)?.name)
    }
}

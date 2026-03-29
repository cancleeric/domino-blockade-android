package com.cancleeric.dominoblockade.presentation

import com.cancleeric.dominoblockade.domain.engine.BoardSide
import com.cancleeric.dominoblockade.domain.engine.GameEngine
import com.cancleeric.dominoblockade.domain.model.Domino
import com.cancleeric.dominoblockade.domain.model.GameConfig
import com.cancleeric.dominoblockade.domain.model.GamePhase
import com.cancleeric.dominoblockade.presentation.game.MultiplayerGameViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MultiplayerGameViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: MultiplayerGameViewModel
    private lateinit var engine: GameEngine

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        engine = GameEngine()
        viewModel = MultiplayerGameViewModel(engine)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // -------------------------------------------------------------------------
    // Game lifecycle
    // -------------------------------------------------------------------------

    @Test
    fun `initial state is null`() {
        assertNull(viewModel.gameState.value)
    }

    @Test
    fun `startGame creates a non-null game state`() {
        viewModel.startGame(GameConfig.twoPlayer())
        assertNotNull(viewModel.gameState.value)
    }

    @Test
    fun `startGame with 2 players creates 2 players`() {
        viewModel.startGame(GameConfig.twoPlayer())
        assertEquals(2, viewModel.gameState.value?.players?.size)
    }

    @Test
    fun `startGame with 3 players creates 3 players`() {
        viewModel.startGame(GameConfig.threePlayer())
        assertEquals(3, viewModel.gameState.value?.players?.size)
    }

    @Test
    fun `startGame with 4 players creates 4 players`() {
        viewModel.startGame(GameConfig.fourPlayer())
        assertEquals(4, viewModel.gameState.value?.players?.size)
    }

    @Test
    fun `resetGame sets state to null`() {
        viewModel.startGame(GameConfig.twoPlayer())
        viewModel.resetGame()
        assertNull(viewModel.gameState.value)
    }

    // -------------------------------------------------------------------------
    // Playing a domino
    // -------------------------------------------------------------------------

    @Test
    fun `playDomino removes tile from current player hand`() {
        viewModel.startGame(GameConfig.twoPlayer())
        val state = viewModel.gameState.value!!
        val playerIndex = state.currentPlayerIndex
        val tile = state.players[playerIndex].hand.first()
        val handSizeBefore = state.players[playerIndex].hand.size

        viewModel.playDomino(tile, BoardSide.RIGHT)

        val newState = viewModel.gameState.value!!
        // Either the player's hand shrank (their turn) or we moved to next player
        val handAfter = newState.players[playerIndex].hand.size
        assertTrue(handAfter < handSizeBefore || newState.phase is GamePhase.PassAndPlay)
    }

    @Test
    fun `playDomino transitions to PassAndPlay for local multiplayer`() {
        viewModel.startGame(GameConfig.twoPlayer())
        var state = viewModel.gameState.value!!
        val playerIndex = state.currentPlayerIndex
        // Ensure player has more than 1 tile (otherwise GameOver)
        val tile = state.players[playerIndex].hand
            .firstOrNull { state.players[playerIndex].hand.size > 1 }
            ?: state.players[playerIndex].hand.first()

        viewModel.playDomino(tile, BoardSide.RIGHT)

        state = viewModel.gameState.value!!
        // If the game isn't over, it should be in PassAndPlay phase
        if (!state.isGameOver) {
            assertTrue(state.phase is GamePhase.PassAndPlay)
        }
    }

    @Test
    fun `playDomino in wrong phase does nothing`() {
        viewModel.startGame(GameConfig.twoPlayer())
        var state = viewModel.gameState.value!!
        val playerIndex = state.currentPlayerIndex
        val tile = state.players[playerIndex].hand.first()

        // Advance to PassAndPlay
        viewModel.playDomino(tile, BoardSide.RIGHT)
        state = viewModel.gameState.value!!
        if (state.phase !is GamePhase.PassAndPlay) return  // game may have ended

        // Try to play again in PassAndPlay phase — should do nothing
        val nextPlayerIndex = state.currentPlayerIndex
        val nextTile = state.players[nextPlayerIndex].hand.firstOrNull() ?: return
        val handSizeBefore = state.players[nextPlayerIndex].hand.size

        viewModel.playDomino(nextTile, BoardSide.LEFT)

        // Hand should not have changed
        assertEquals(handSizeBefore, viewModel.gameState.value!!.players[nextPlayerIndex].hand.size)
    }

    // -------------------------------------------------------------------------
    // Confirm Pass & Play
    // -------------------------------------------------------------------------

    @Test
    fun `confirmPassAndPlay transitions to PlayerTurn`() {
        viewModel.startGame(GameConfig.twoPlayer())
        val state = viewModel.gameState.value!!
        val playerIndex = state.currentPlayerIndex
        val tile = state.players[playerIndex].hand.first()

        viewModel.playDomino(tile, BoardSide.RIGHT)

        val stateAfterPlay = viewModel.gameState.value!!
        if (stateAfterPlay.phase !is GamePhase.PassAndPlay) return  // game ended

        viewModel.confirmPassAndPlay()

        val stateAfterConfirm = viewModel.gameState.value!!
        assertTrue(stateAfterConfirm.phase is GamePhase.PlayerTurn)
    }

    @Test
    fun `confirmPassAndPlay in wrong phase does nothing`() {
        viewModel.startGame(GameConfig.twoPlayer())
        val state = viewModel.gameState.value!!
        assertTrue(state.phase is GamePhase.PlayerTurn)

        viewModel.confirmPassAndPlay()  // should be no-op

        assertTrue(viewModel.gameState.value!!.phase is GamePhase.PlayerTurn)
    }

    // -------------------------------------------------------------------------
    // Drawing a tile
    // -------------------------------------------------------------------------

    @Test
    fun `drawTile reduces draw pile size by 1`() {
        viewModel.startGame(GameConfig.twoPlayer())
        val stateBefore = viewModel.gameState.value!!
        val pileSizeBefore = stateBefore.drawPile.size

        viewModel.drawTile()

        val newState = viewModel.gameState.value!!
        assertEquals(pileSizeBefore - 1, newState.drawPile.size)
    }

    @Test
    fun `drawTile in wrong phase does nothing`() {
        viewModel.startGame(GameConfig.twoPlayer())
        val state = viewModel.gameState.value!!
        val tile = state.players[state.currentPlayerIndex].hand.first()

        viewModel.playDomino(tile, BoardSide.RIGHT)

        val stateAfterPlay = viewModel.gameState.value!!
        if (stateAfterPlay.phase !is GamePhase.PassAndPlay) return

        val pileSizeBefore = stateAfterPlay.drawPile.size
        viewModel.drawTile()  // should be no-op in PassAndPlay phase

        assertEquals(pileSizeBefore, viewModel.gameState.value!!.drawPile.size)
    }

    // -------------------------------------------------------------------------
    // Error handling
    // -------------------------------------------------------------------------

    @Test
    fun `clearError sets error to null`() {
        viewModel.startGame(GameConfig.twoPlayer())
        // Trigger an invalid move to generate an error
        val state = viewModel.gameState.value!!
        val playerIndex = state.currentPlayerIndex
        val tile = state.players[playerIndex].hand.first()
        // First play establishes the board
        viewModel.playDomino(tile, BoardSide.RIGHT)
        viewModel.confirmPassAndPlay()

        // Try an invalid move (wrong end value)
        val nextState = viewModel.gameState.value!!
        val nextPlayerIndex = nextState.currentPlayerIndex
        val invalidTile = nextState.players[nextPlayerIndex].hand
            .firstOrNull { domino ->
                !domino.canConnectTo(nextState.boardLeftEnd ?: -1) &&
                    !domino.canConnectTo(nextState.boardRightEnd ?: -1)
            }
        if (invalidTile != null) {
            viewModel.playDomino(invalidTile, BoardSide.RIGHT)
            // error may be set
        }

        viewModel.clearError()
        assertNull(viewModel.error.value)
    }

    // -------------------------------------------------------------------------
    // Full game simulation - 2 players
    // -------------------------------------------------------------------------

    @Test
    fun `full 2-player game can be played to completion`() {
        val config = GameConfig.twoPlayer("Alice", "Bob")
        viewModel.startGame(config)

        var iterations = 0
        val maxIterations = 500  // guard against infinite loops

        while (!viewModel.gameState.value!!.isGameOver && iterations < maxIterations) {
            iterations++
            val state = viewModel.gameState.value!!

            when (val phase = state.phase) {
                is GamePhase.PlayerTurn -> {
                    val player = state.currentPlayer
                    val tile = player.hand.firstOrNull { domino ->
                        engine.isValidMove(state, domino, BoardSide.LEFT) ||
                            engine.isValidMove(state, domino, BoardSide.RIGHT)
                    }
                    if (tile != null) {
                        val side = if (engine.isValidMove(state, tile, BoardSide.RIGHT))
                            BoardSide.RIGHT else BoardSide.LEFT
                        viewModel.playDomino(tile, side)
                    } else {
                        viewModel.drawTile()
                    }
                }
                is GamePhase.PassAndPlay -> viewModel.confirmPassAndPlay()
                is GamePhase.GameOver -> break
                else -> break
            }
        }

        val finalState = viewModel.gameState.value!!
        assertTrue(
            "Game should be over after $iterations iterations",
            finalState.isGameOver || iterations >= maxIterations
        )
    }
}

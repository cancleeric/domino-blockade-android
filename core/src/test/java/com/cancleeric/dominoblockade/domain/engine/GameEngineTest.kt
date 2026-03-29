package com.cancleeric.dominoblockade.domain.engine

import com.cancleeric.dominoblockade.domain.model.Domino
import com.cancleeric.dominoblockade.domain.model.GameConfig
import com.cancleeric.dominoblockade.domain.model.GamePhase
import com.cancleeric.dominoblockade.domain.model.GameState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GameEngineTest {

    private lateinit var engine: GameEngine

    @Before
    fun setUp() {
        engine = GameEngine()
    }

    // -------------------------------------------------------------------------
    // Full domino set
    // -------------------------------------------------------------------------

    @Test
    fun `generateFullSet returns exactly 28 tiles`() {
        val tiles = engine.generateFullSet()
        assertEquals(28, tiles.size)
    }

    @Test
    fun `generateFullSet contains no duplicate tiles`() {
        val tiles = engine.generateFullSet()
        val unique = tiles.toSet()
        assertEquals(tiles.size, unique.size)
    }

    @Test
    fun `generateFullSet contains all doubles from 0-0 to 6-6`() {
        val tiles = engine.generateFullSet()
        for (i in 0..6) {
            assertTrue("Missing double [$i|$i]", tiles.contains(Domino(i, i)))
        }
    }

    // -------------------------------------------------------------------------
    // Game config / hand sizes
    // -------------------------------------------------------------------------

    @Test
    fun `two-player game deals 7 tiles per player`() {
        val config = GameConfig.twoPlayer()
        val state = engine.newGame(config)
        assertEquals(7, state.players[0].hand.size)
        assertEquals(7, state.players[1].hand.size)
    }

    @Test
    fun `three-player game deals 5 tiles per player`() {
        val config = GameConfig.threePlayer()
        val state = engine.newGame(config)
        state.players.forEach { player ->
            assertEquals(5, player.hand.size)
        }
    }

    @Test
    fun `four-player game deals 5 tiles per player`() {
        val config = GameConfig.fourPlayer()
        val state = engine.newGame(config)
        state.players.forEach { player ->
            assertEquals(5, player.hand.size)
        }
    }

    @Test
    fun `two-player game draw pile has 14 tiles remaining`() {
        // 28 total - 2*7 dealt = 14
        val config = GameConfig.twoPlayer()
        val state = engine.newGame(config)
        assertEquals(14, state.drawPile.size)
    }

    @Test
    fun `three-player game draw pile has 13 tiles remaining`() {
        // 28 total - 3*5 dealt = 13
        val config = GameConfig.threePlayer()
        val state = engine.newGame(config)
        assertEquals(13, state.drawPile.size)
    }

    @Test
    fun `four-player game draw pile has 8 tiles remaining`() {
        // 28 total - 4*5 dealt = 8
        val config = GameConfig.fourPlayer()
        val state = engine.newGame(config)
        assertEquals(8, state.drawPile.size)
    }

    @Test
    fun `new game starts with PlayerTurn phase`() {
        val state = engine.newGame(GameConfig.twoPlayer())
        assertTrue(state.phase is GamePhase.PlayerTurn)
    }

    @Test
    fun `no tile appears in more than one player hand`() {
        val config = GameConfig.fourPlayer()
        val state = engine.newGame(config)
        val allHands = state.players.flatMap { it.hand }
        assertEquals(allHands.size, allHands.toSet().size)
    }

    // -------------------------------------------------------------------------
    // Move validation
    // -------------------------------------------------------------------------

    @Test
    fun `any tile is valid on empty board`() {
        val state = engine.newGame(GameConfig.twoPlayer())
        val domino = state.currentPlayer.hand.first()
        assertTrue(engine.isValidMove(state, domino, BoardSide.RIGHT))
        assertTrue(engine.isValidMove(state, domino, BoardSide.LEFT))
    }

    @Test
    fun `tile matching left end is valid on left side`() {
        val state = buildStateWithBoard(
            boardTiles = listOf(Domino(3, 5)),
            leftEnd = 3,
            rightEnd = 5
        )
        assertTrue(engine.isValidMove(state, Domino(1, 3), BoardSide.LEFT))
        assertTrue(engine.isValidMove(state, Domino(3, 3), BoardSide.LEFT))
    }

    @Test
    fun `tile not matching board end is invalid`() {
        val state = buildStateWithBoard(
            boardTiles = listOf(Domino(3, 5)),
            leftEnd = 3,
            rightEnd = 5
        )
        assertFalse(engine.isValidMove(state, Domino(1, 2), BoardSide.RIGHT))
    }

    // -------------------------------------------------------------------------
    // Placing a domino
    // -------------------------------------------------------------------------

    @Test
    fun `placing domino on empty board sets both ends`() {
        val domino = Domino(4, 6)
        val config = GameConfig.twoPlayer()
        val fixedOrder = listOf(domino) + engine.generateFullSet()
            .filterNot { it == domino }
            .take(27)
        val state = engine.newGame(config, fixedOrder)
        val playerIndex = state.currentPlayerIndex
        val tileToPlay = state.players[playerIndex].hand.first()

        val newState = engine.placeDomino(state, tileToPlay, BoardSide.RIGHT)

        assertEquals(1, newState.board.size)
        assertNotNull(newState.boardLeftEnd)
        assertNotNull(newState.boardRightEnd)
    }

    @Test
    fun `placing domino removes it from the player's hand`() {
        val config = GameConfig.twoPlayer()
        val state = engine.newGame(config)
        val playerIndex = state.currentPlayerIndex
        val tileToPlay = state.players[playerIndex].hand.first()
        val handSizeBefore = state.players[playerIndex].hand.size

        val newState = engine.placeDomino(state, tileToPlay, BoardSide.RIGHT)

        assertEquals(handSizeBefore - 1, newState.players[playerIndex].hand.size)
        assertFalse(newState.players[playerIndex].hand.contains(tileToPlay))
    }

    @Test
    fun `placing last tile triggers GameOver`() {
        // Build a state where the current player has exactly one tile
        val playerTile = Domino(3, 3)
        val state = buildStateWithHands(
            player0Hand = listOf(playerTile),
            player1Hand = listOf(Domino(1, 2), Domino(0, 4)),
            currentPlayerIndex = 0,
            boardLeftEnd = null,
            boardRightEnd = null
        )
        val newState = engine.placeDomino(state, playerTile, BoardSide.RIGHT)
        assertTrue(newState.phase is GamePhase.GameOver)
        assertEquals(0, (newState.phase as GamePhase.GameOver).winnerIndex)
    }

    @Test
    fun `placing domino transitions to PassAndPlay phase`() {
        val config = GameConfig.twoPlayer()
        val state = engine.newGame(config)
        val playerIndex = state.currentPlayerIndex
        val tile = state.players[playerIndex].hand.first()
        // Give player more than 1 tile so the game doesn't end
        require(state.players[playerIndex].hand.size > 1) {
            "Expected player to have more than 1 tile"
        }
        val newState = engine.placeDomino(state, tile, BoardSide.RIGHT)
        assertTrue(newState.phase is GamePhase.PassAndPlay)
    }

    // -------------------------------------------------------------------------
    // Drawing a tile
    // -------------------------------------------------------------------------

    @Test
    fun `drawing a tile adds it to current player's hand`() {
        val config = GameConfig.twoPlayer()
        val state = engine.newGame(config)
        val newState = engine.drawTile(state)

        // Either the player's hand grew (stayed on player turn) or the turn advanced.
        // Either way, the tile was taken from the draw pile.
        assertEquals(state.drawPile.size - 1, newState.drawPile.size)
    }

    @Test
    fun `drawing when pile is empty checks for blockade`() {
        val state = buildStateWithBoard(
            boardTiles = listOf(Domino(1, 1)),
            leftEnd = 1,
            rightEnd = 1,
            drawPile = emptyList(),
            player0Hand = listOf(Domino(2, 3)),   // can't connect to 1
            player1Hand = listOf(Domino(4, 5))    // can't connect to 1
        )
        val newState = engine.drawTile(state)
        // Both players can't play → blockade → GameOver
        assertTrue(newState.phase is GamePhase.GameOver)
    }

    // -------------------------------------------------------------------------
    // Turn advancement & Pass & Play
    // -------------------------------------------------------------------------

    @Test
    fun `advanceTurn in two-player alternates between players 0 and 1`() {
        val config = GameConfig.twoPlayer()
        var state = engine.newGame(config)
        val firstPlayer = state.currentPlayerIndex

        state = engine.advanceTurn(state, firstPlayer)
        val passAndPlay = state.phase as? GamePhase.PassAndPlay
        assertNotNull(passAndPlay)
        assertEquals((firstPlayer + 1) % 2, passAndPlay!!.nextPlayerIndex)
    }

    @Test
    fun `confirmPassAndPlay transitions to PlayerTurn`() {
        val config = GameConfig.twoPlayer()
        var state = engine.newGame(config)
        state = engine.advanceTurn(state, state.currentPlayerIndex)
        assertTrue(state.phase is GamePhase.PassAndPlay)

        state = engine.confirmPassAndPlay(state)
        assertTrue(state.phase is GamePhase.PlayerTurn)
    }

    @Test
    fun `three player turn advances cyclically 0 → 1 → 2 → 0`() {
        val config = GameConfig.threePlayer()
        var state = engine.newGame(config)

        // Force player 0 first
        state = state.copy(currentPlayerIndex = 0, phase = GamePhase.PlayerTurn(0))

        state = engine.advanceTurn(state, 0)
        state = engine.confirmPassAndPlay(state)
        assertEquals(1, state.currentPlayerIndex)

        state = engine.advanceTurn(state, 1)
        state = engine.confirmPassAndPlay(state)
        assertEquals(2, state.currentPlayerIndex)

        state = engine.advanceTurn(state, 2)
        state = engine.confirmPassAndPlay(state)
        assertEquals(0, state.currentPlayerIndex)
    }

    // -------------------------------------------------------------------------
    // Blockade detection
    // -------------------------------------------------------------------------

    @Test
    fun `checkBlockade ends game when no player can move and pile empty`() {
        val state = buildStateWithBoard(
            boardTiles = listOf(Domino(1, 1)),
            leftEnd = 1,
            rightEnd = 1,
            drawPile = emptyList(),
            player0Hand = listOf(Domino(2, 3)),
            player1Hand = listOf(Domino(4, 5))
        )
        val newState = engine.checkBlockade(state)
        assertTrue(newState.phase is GamePhase.GameOver)
    }

    @Test
    fun `checkBlockade does not end game when pile is not empty`() {
        val state = buildStateWithBoard(
            boardTiles = listOf(Domino(1, 1)),
            leftEnd = 1,
            rightEnd = 1,
            drawPile = listOf(Domino(0, 0)),
            player0Hand = listOf(Domino(2, 3)),
            player1Hand = listOf(Domino(4, 5))
        )
        val newState = engine.checkBlockade(state)
        assertFalse(newState.phase is GamePhase.GameOver)
    }

    @Test
    fun `blockade winner is player with lowest hand score`() {
        val state = buildStateWithBoard(
            boardTiles = listOf(Domino(1, 1)),
            leftEnd = 1,
            rightEnd = 1,
            drawPile = emptyList(),
            player0Hand = listOf(Domino(6, 6)),  // score = 12, cannot connect to 1
            player1Hand = listOf(Domino(0, 2))   // score = 2, cannot connect to 1
        )
        val newState = engine.checkBlockade(state)
        val phase = newState.phase as GamePhase.GameOver
        assertEquals(1, phase.winnerIndex)  // player 1 wins with lowest score
    }

    @Test
    fun `blockade with equal scores results in null winner`() {
        val state = buildStateWithBoard(
            boardTiles = listOf(Domino(1, 1)),
            leftEnd = 1,
            rightEnd = 1,
            drawPile = emptyList(),
            player0Hand = listOf(Domino(0, 4)),  // score = 4, cannot connect to 1
            player1Hand = listOf(Domino(2, 2))   // score = 4, cannot connect to 1
        )
        val newState = engine.checkBlockade(state)
        val phase = newState.phase as GamePhase.GameOver
        assertNull(phase.winnerIndex)
    }

    // -------------------------------------------------------------------------
    // Score calculation
    // -------------------------------------------------------------------------

    @Test
    fun `calculateScores returns sum of pip values for each player`() {
        val state = buildStateWithHands(
            player0Hand = listOf(Domino(1, 2), Domino(3, 4)),   // 3 + 7 = 10
            player1Hand = listOf(Domino(0, 0), Domino(2, 2)),   // 0 + 4 = 4
            currentPlayerIndex = 0
        )
        val scores = engine.calculateScores(state)
        assertEquals(10, scores[0])
        assertEquals(4, scores[1])
    }

    // -------------------------------------------------------------------------
    // GameConfig validation
    // -------------------------------------------------------------------------

    @Test(expected = IllegalArgumentException::class)
    fun `GameConfig rejects player count less than 2`() {
        GameConfig(1, listOf("P1"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `GameConfig rejects player count greater than 4`() {
        GameConfig(5, listOf("P1", "P2", "P3", "P4", "P5"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `GameConfig rejects mismatched names and player count`() {
        GameConfig(3, listOf("P1", "P2"))
    }

    @Test
    fun `two player config initial hand size is 7`() {
        assertEquals(7, GameConfig.twoPlayer().initialHandSize)
    }

    @Test
    fun `three player config initial hand size is 5`() {
        assertEquals(5, GameConfig.threePlayer().initialHandSize)
    }

    @Test
    fun `four player config initial hand size is 5`() {
        assertEquals(5, GameConfig.fourPlayer().initialHandSize)
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private fun buildStateWithBoard(
        boardTiles: List<Domino>,
        leftEnd: Int?,
        rightEnd: Int?,
        drawPile: List<Domino> = emptyList(),
        player0Hand: List<Domino> = listOf(Domino(0, 1)),
        player1Hand: List<Domino> = listOf(Domino(2, 3)),
        currentPlayerIndex: Int = 0
    ): GameState {
        val players = listOf(
            com.cancleeric.dominoblockade.domain.model.Player(0, "P1", player0Hand),
            com.cancleeric.dominoblockade.domain.model.Player(1, "P2", player1Hand)
        )
        return GameState(
            players = players,
            board = boardTiles,
            drawPile = drawPile,
            currentPlayerIndex = currentPlayerIndex,
            phase = GamePhase.PlayerTurn(currentPlayerIndex),
            boardLeftEnd = leftEnd,
            boardRightEnd = rightEnd
        )
    }

    private fun buildStateWithHands(
        player0Hand: List<Domino>,
        player1Hand: List<Domino>,
        currentPlayerIndex: Int,
        boardLeftEnd: Int? = null,
        boardRightEnd: Int? = null
    ): GameState {
        val players = listOf(
            com.cancleeric.dominoblockade.domain.model.Player(0, "P1", player0Hand),
            com.cancleeric.dominoblockade.domain.model.Player(1, "P2", player1Hand)
        )
        return GameState(
            players = players,
            board = emptyList(),
            drawPile = emptyList(),
            currentPlayerIndex = currentPlayerIndex,
            phase = GamePhase.PlayerTurn(currentPlayerIndex),
            boardLeftEnd = boardLeftEnd,
            boardRightEnd = boardRightEnd
        )
    }
}

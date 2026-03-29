package com.cancleeric.dominoblockade.domain.engine

import com.cancleeric.dominoblockade.domain.model.Domino
import com.cancleeric.dominoblockade.domain.model.GameConfig
import com.cancleeric.dominoblockade.domain.model.GamePhase
import com.cancleeric.dominoblockade.domain.model.GameState
import com.cancleeric.dominoblockade.domain.model.Player

/**
 * Core game engine for Domino Blockade.
 *
 * Handles all game logic:
 * - Generating and dealing tiles
 * - Validating and placing moves
 * - Drawing from the pile
 * - Detecting blockades
 * - Calculating final scores
 *
 * The engine is stateless: every method takes a [GameState] and returns a new [GameState].
 */
class GameEngine {

    // -------------------------------------------------------------------------
    // Initialisation
    // -------------------------------------------------------------------------

    /** Creates the full 28-tile double-six set. */
    fun generateFullSet(): List<Domino> {
        val tiles = mutableListOf<Domino>()
        for (i in 0..6) {
            for (j in i..6) {
                tiles.add(Domino(i, j))
            }
        }
        return tiles
    }

    /**
     * Creates a new [GameState] from [config]:
     * - Shuffles the full domino set.
     * - Deals [GameConfig.initialHandSize] tiles to each player.
     * - Remaining tiles form the draw pile.
     * - Determines the first player (holder of the highest double, or index 0).
     * - Phase is set to [GamePhase.PlayerTurn].
     */
    fun newGame(config: GameConfig, shuffled: List<Domino>? = null): GameState {
        val allTiles = (shuffled ?: generateFullSet().shuffled()).toMutableList()
        val handSize = config.initialHandSize

        val players = config.playerNames.mapIndexed { index, name ->
            val hand = allTiles.subList(index * handSize, (index + 1) * handSize).toList()
            Player(id = index, name = name, hand = hand)
        }

        // Remove dealt tiles from the pile
        repeat(config.playerCount * handSize) { allTiles.removeAt(0) }

        val firstPlayerIndex = determineFirstPlayer(players)

        return GameState(
            players = players,
            board = emptyList(),
            drawPile = allTiles,
            currentPlayerIndex = firstPlayerIndex,
            phase = GamePhase.PlayerTurn(firstPlayerIndex),
            boardLeftEnd = null,
            boardRightEnd = null
        )
    }

    /** First player is the one holding the highest double; ties broken by player index. */
    private fun determineFirstPlayer(players: List<Player>): Int {
        var bestDouble = -1
        var bestPlayerIndex = 0
        players.forEachIndexed { idx, player ->
            val highDouble = player.hand
                .filter { it.isDouble() }
                .maxOfOrNull { it.left } ?: -1
            if (highDouble > bestDouble) {
                bestDouble = highDouble
                bestPlayerIndex = idx
            }
        }
        return bestPlayerIndex
    }

    // -------------------------------------------------------------------------
    // Move validation
    // -------------------------------------------------------------------------

    /**
     * Returns true if [domino] can be legally placed on [side] of the board.
     *
     * When the board is empty, any domino is valid.
     */
    fun isValidMove(state: GameState, domino: Domino, side: BoardSide): Boolean {
        if (state.board.isEmpty()) return true
        val endValue = when (side) {
            BoardSide.LEFT  -> state.boardLeftEnd!!
            BoardSide.RIGHT -> state.boardRightEnd!!
        }
        return domino.canConnectTo(endValue)
    }

    /** Returns all valid placements for a given domino in the current state. */
    fun validPlacements(state: GameState, domino: Domino): List<Placement> {
        if (state.board.isEmpty()) {
            return listOf(Placement(domino, BoardSide.RIGHT))
        }
        val placements = mutableListOf<Placement>()
        if (domino.canConnectTo(state.boardLeftEnd!!)) {
            placements.add(Placement(domino, BoardSide.LEFT))
        }
        if (domino.canConnectTo(state.boardRightEnd!!)) {
            placements.add(Placement(domino, BoardSide.RIGHT))
        }
        return placements
    }

    /** True if the current player has at least one legal move. */
    fun canCurrentPlayerPlay(state: GameState): Boolean {
        val player = state.currentPlayer
        if (state.board.isEmpty()) return player.hand.isNotEmpty()
        return player.hand.any { domino ->
            domino.canConnectTo(state.boardLeftEnd!!) ||
                domino.canConnectTo(state.boardRightEnd!!)
        }
    }

    // -------------------------------------------------------------------------
    // Playing a tile
    // -------------------------------------------------------------------------

    /**
     * Places [domino] on [side] of the board for the current player.
     *
     * Returns the updated [GameState].
     * After a successful placement, advances the turn:
     * - If the player's hand is now empty → GameOver (winner found).
     * - Otherwise → next player's turn (with PassAndPlay phase for local multiplayer).
     *
     * @throws IllegalArgumentException if the move is not valid.
     */
    fun placeDomino(state: GameState, domino: Domino, side: BoardSide): GameState {
        require(isValidMove(state, domino, side)) {
            "Invalid move: $domino cannot be placed on the $side side"
        }
        val playerIndex = state.currentPlayerIndex
        val player = state.players[playerIndex]
        require(player.hand.contains(domino)) { "Player does not hold $domino" }

        // Orient the domino correctly before appending
        val oriented = orientDomino(domino, side, state)

        // Update hand
        val newHand = player.hand - domino
        val updatedPlayer = player.copy(hand = newHand)
        val updatedPlayers = state.players.toMutableList()
            .also { it[playerIndex] = updatedPlayer }

        // Update board
        val newBoard = when (side) {
            BoardSide.LEFT  -> listOf(oriented) + state.board
            BoardSide.RIGHT -> state.board + oriented
        }
        val newLeftEnd  = newBoard.first().left
        val newRightEnd = newBoard.last().right

        val stateAfterPlace = state.copy(
            players = updatedPlayers,
            board = newBoard,
            boardLeftEnd = newLeftEnd,
            boardRightEnd = newRightEnd
        )

        // Check win condition
        if (newHand.isEmpty()) {
            return endGame(stateAfterPlace, winnerIndex = playerIndex)
        }

        return advanceTurn(stateAfterPlace, playerIndex)
    }

    /**
     * Orient a domino so that the matching pip faces the open end of the board.
     */
    private fun orientDomino(domino: Domino, side: BoardSide, state: GameState): Domino {
        if (state.board.isEmpty()) return domino
        return when (side) {
            BoardSide.LEFT -> {
                val endValue = state.boardLeftEnd!!
                if (domino.right == endValue) domino else domino.flipped()
            }
            BoardSide.RIGHT -> {
                val endValue = state.boardRightEnd!!
                if (domino.left == endValue) domino else domino.flipped()
            }
        }
    }

    // -------------------------------------------------------------------------
    // Drawing a tile
    // -------------------------------------------------------------------------

    /**
     * Draws a single tile from the pile for the current player.
     *
     * - If the draw pile is empty, does nothing and checks for blockade.
     * - After drawing, if the player can now play, stays on [GamePhase.PlayerTurn].
     * - If the player still cannot play after drawing, the turn advances automatically.
     */
    fun drawTile(state: GameState): GameState {
        if (state.drawPile.isEmpty()) {
            return checkBlockade(state)
        }

        val drawnTile = state.drawPile.first()
        val newDrawPile = state.drawPile.drop(1)

        val playerIndex = state.currentPlayerIndex
        val player = state.players[playerIndex]
        val updatedPlayer = player.copy(hand = player.hand + drawnTile)
        val updatedPlayers = state.players.toMutableList()
            .also { it[playerIndex] = updatedPlayer }

        val newState = state.copy(
            players = updatedPlayers,
            drawPile = newDrawPile,
            phase = GamePhase.PlayerTurn(playerIndex)
        )

        // If the player can now play, let them
        return if (canCurrentPlayerPlay(newState)) newState
        else advanceTurn(newState, playerIndex)
    }

    // -------------------------------------------------------------------------
    // Turn management
    // -------------------------------------------------------------------------

    /**
     * Advances the game to the next player's turn.
     *
     * For local multiplayer (Pass & Play), the phase is set to [GamePhase.PassAndPlay]
     * so the UI can show a handoff screen before revealing the next player's hand.
     */
    fun advanceTurn(state: GameState, previousPlayerIndex: Int): GameState {
        val nextIndex = (previousPlayerIndex + 1) % state.players.size
        return state.copy(
            currentPlayerIndex = nextIndex,
            phase = GamePhase.PassAndPlay(
                previousPlayerIndex = previousPlayerIndex,
                nextPlayerIndex = nextIndex
            )
        )
    }

    /**
     * Confirms the Pass & Play handoff — transitions from [GamePhase.PassAndPlay]
     * to [GamePhase.PlayerTurn] for the next player.
     *
     * Call this when the next player presses "I'm ready" on the handoff screen.
     */
    fun confirmPassAndPlay(state: GameState): GameState {
        val phase = state.phase as? GamePhase.PassAndPlay
            ?: return state   // no-op if called in wrong phase
        return state.copy(phase = GamePhase.PlayerTurn(phase.nextPlayerIndex))
    }

    // -------------------------------------------------------------------------
    // Blockade detection
    // -------------------------------------------------------------------------

    /**
     * Checks whether the game has reached a blockade state:
     * draw pile is empty and no player can make a move.
     */
    fun checkBlockade(state: GameState): GameState {
        if (state.drawPile.isNotEmpty()) return state
        val anyCanPlay = state.players.any { player ->
            if (state.board.isEmpty()) return@any player.hand.isNotEmpty()
            player.hand.any { domino ->
                domino.canConnectTo(state.boardLeftEnd!!) ||
                    domino.canConnectTo(state.boardRightEnd!!)
            }
        }
        return if (anyCanPlay) state else endGame(state, winnerIndex = null)
    }

    // -------------------------------------------------------------------------
    // Scoring / Game over
    // -------------------------------------------------------------------------

    /**
     * Ends the game.
     *
     * @param winnerIndex the index of the winning player, or null for a blockade-determined winner.
     *
     * When [winnerIndex] is null (blockade), the winner is the player with the
     * lowest remaining hand score; ties result in a null winner.
     */
    fun endGame(state: GameState, winnerIndex: Int?): GameState {
        val scores = state.players.associate { it.id to it.handScore }

        val resolvedWinner = winnerIndex ?: run {
            val minScore = scores.values.minOrNull() ?: 0
            val candidates = scores.entries.filter { it.value == minScore }
            if (candidates.size == 1) candidates.first().key else null
        }

        return state.copy(
            phase = GamePhase.GameOver(
                winnerIndex = resolvedWinner,
                scores = scores
            )
        )
    }

    /**
     * Calculates how many points each player has remaining (lower is better).
     * Used for display purposes.
     */
    fun calculateScores(state: GameState): Map<Int, Int> =
        state.players.associate { it.id to it.handScore }
}

/** Which end of the board to place a domino on. */
enum class BoardSide { LEFT, RIGHT }

/** A legal placement for a specific domino on a specific side. */
data class Placement(val domino: Domino, val side: BoardSide)

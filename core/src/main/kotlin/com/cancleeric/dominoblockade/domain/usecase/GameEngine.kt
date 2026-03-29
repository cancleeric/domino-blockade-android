package com.cancleeric.dominoblockade.domain.usecase

import com.cancleeric.dominoblockade.domain.model.Domino
import com.cancleeric.dominoblockade.domain.model.GameConfig
import com.cancleeric.dominoblockade.domain.model.GameState
import com.cancleeric.dominoblockade.domain.model.Player

/**
 * Central game engine that orchestrates all game logic by delegating to specialised use-cases.
 *
 * Typical game loop:
 * 1. [startGame] — shuffle and deal tiles.
 * 2. Each turn: call [getValidMoves] for the current player.
 *    - If moves exist → call [playDomino], then [nextTurn].
 *    - If no moves and boneyard not empty → call [drawDomino] (adds tile, does not advance turn).
 *    - If no moves and boneyard empty → call [drawDomino] (skips / advances turn automatically).
 * 3. After each action check [GameState.isGameOver] or [isBlocked].
 * 4. When the game ends call [calculateScores].
 */
class GameEngine {

    private val dealUseCase = DealUseCase()
    private val validateMoveUseCase = ValidateMoveUseCase()
    private val drawUseCase = DrawUseCase()
    private val blockadeDetector = BlockadeDetector()
    private val scoreCalculator = ScoreCalculator()

    /** Initialises a new game: shuffles the full domino set and deals tiles to all players. */
    fun startGame(config: GameConfig): GameState = dealUseCase.deal(config)

    /**
     * Places [domino] onto [end] of the board for the current player.
     *
     * The domino is automatically oriented so its connecting side faces the board end.
     * The turn is NOT automatically advanced; call [nextTurn] after a successful play.
     *
     * @throws IllegalArgumentException if the move is invalid or the game is already over.
     */
    fun playDomino(state: GameState, domino: Domino, end: BoardEnd): GameState {
        require(!state.isGameOver) { "Game is already over" }
        require(state.currentPlayer.hand.contains(domino)) { "Domino $domino is not in the current player's hand" }
        require(validateMoveUseCase.validate(state, domino, end)) { "Invalid move: $domino onto $end end" }

        val orientedDomino = orientDomino(domino, end, state)

        val newBoard = when {
            state.board.isEmpty() -> listOf(orientedDomino)
            end == BoardEnd.LEFT -> listOf(orientedDomino) + state.board
            else -> state.board + orientedDomino
        }

        val newHand = state.currentPlayer.hand - domino
        val updatedPlayer = state.currentPlayer.copy(hand = newHand)
        val updatedPlayers = state.players.toMutableList().also {
            it[state.currentPlayerIndex] = updatedPlayer
        }

        val isGameOver = newHand.isEmpty()
        return state.copy(
            players = updatedPlayers,
            board = newBoard,
            isGameOver = isGameOver,
            winner = if (isGameOver) updatedPlayer else null
        )
    }

    /**
     * Draws one tile from the boneyard for the current player.
     *
     * - If the boneyard has tiles: adds a tile to the current player's hand
     *   (turn does NOT advance — the player may now be able to play).
     * - If the boneyard is empty: skips the current player's turn and advances to the next player.
     */
    fun drawDomino(state: GameState): GameState = drawUseCase.draw(state)

    /** Returns all legal moves available to [player] in the given [state]. */
    fun getValidMoves(state: GameState, player: Player): List<ValidMove> =
        validateMoveUseCase.getValidMoves(state, player)

    /**
     * Returns true when the game is in a blockade:
     * the boneyard is empty and no player has any legal move.
     */
    fun isBlocked(state: GameState): Boolean = blockadeDetector.isBlocked(state)

    /**
     * Computes the final score for each player.
     * Should be called after [GameState.isGameOver] is true or [isBlocked] returns true.
     */
    fun calculateScores(state: GameState): Map<Player, Int> = scoreCalculator.calculateScores(state)

    /** Advances the turn to the next player (round-robin). */
    fun nextTurn(state: GameState): GameState {
        val nextIndex = (state.currentPlayerIndex + 1) % state.players.size
        return state.copy(currentPlayerIndex = nextIndex)
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * Returns [domino] oriented so its connecting side faces the chosen [end]:
     * - LEFT: domino.right must equal the current left end of the board.
     * - RIGHT: domino.left must equal the current right end of the board.
     *
     * Flips the domino if necessary.
     */
    private fun orientDomino(domino: Domino, end: BoardEnd, state: GameState): Domino {
        if (state.board.isEmpty()) return domino
        return when (end) {
            BoardEnd.LEFT -> {
                val leftEnd = state.leftEnd!!
                if (domino.right == leftEnd) domino else domino.flipped()
            }
            BoardEnd.RIGHT -> {
                val rightEnd = state.rightEnd!!
                if (domino.left == rightEnd) domino else domino.flipped()
            }
        }
    }
}

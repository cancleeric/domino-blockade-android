package com.cancleeric.dominoblockade.domain.usecase

import com.cancleeric.dominoblockade.domain.model.Domino
import com.cancleeric.dominoblockade.domain.model.DominoSet
import com.cancleeric.dominoblockade.domain.model.GameConfig
import com.cancleeric.dominoblockade.domain.model.GameState
import com.cancleeric.dominoblockade.domain.model.Player
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GameEngineTest {

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private val engine = GameEngine()

    private fun players(count: Int) = (1..count).map { Player("p$it", "Player $it") }

    private fun twoPlayerConfig(seed: Long = 42L) =
        GameConfig(players = players(2), randomSeed = seed)

    private fun threePlayerConfig(seed: Long = 42L) =
        GameConfig(players = players(3), randomSeed = seed)

    private fun fourPlayerConfig(seed: Long = 42L) =
        GameConfig(players = players(4), randomSeed = seed)

    /** Builds a minimal GameState with no boneyard and an empty board. */
    private fun stateWith(
        playerHands: List<List<Domino>>,
        board: List<Domino> = emptyList(),
        boneyard: List<Domino> = emptyList(),
        currentPlayerIndex: Int = 0
    ): GameState {
        val ps = playerHands.mapIndexed { i, hand -> Player("p${i + 1}", "Player ${i + 1}", hand) }
        return GameState(
            players = ps,
            board = board,
            boneyard = boneyard,
            currentPlayerIndex = currentPlayerIndex
        )
    }

    // =========================================================================
    // startGame
    // =========================================================================

    @Nested
    inner class StartGame {

        @Test
        fun `two-player game deals 7 tiles per player`() {
            val state = engine.startGame(twoPlayerConfig())
            assertEquals(2, state.players.size)
            state.players.forEach { assertEquals(7, it.hand.size) }
        }

        @Test
        fun `two-player game leaves 14 tiles in boneyard`() {
            val state = engine.startGame(twoPlayerConfig())
            assertEquals(14, state.boneyard.size)
        }

        @Test
        fun `three-player game deals 5 tiles per player`() {
            val state = engine.startGame(threePlayerConfig())
            state.players.forEach { assertEquals(5, it.hand.size) }
        }

        @Test
        fun `three-player game leaves 13 tiles in boneyard`() {
            // 28 - (3 * 5) = 13
            val state = engine.startGame(threePlayerConfig())
            assertEquals(13, state.boneyard.size)
        }

        @Test
        fun `four-player game deals 5 tiles per player`() {
            val state = engine.startGame(fourPlayerConfig())
            state.players.forEach { assertEquals(5, it.hand.size) }
        }

        @Test
        fun `four-player game leaves 8 tiles in boneyard`() {
            // 28 - (4 * 5) = 8
            val state = engine.startGame(fourPlayerConfig())
            assertEquals(8, state.boneyard.size)
        }

        @Test
        fun `all 28 tiles are dealt with no duplicates`() {
            val state = engine.startGame(twoPlayerConfig())
            val all = state.players.flatMap { it.hand } + state.boneyard
            assertEquals(28, all.size)
            val fullSet = DominoSet.createFullSet()
            // Every tile in the full set appears exactly once (accounting for flip equivalence)
            val allNormalised = all.map { if (it.left <= it.right) it else it.flipped() }.toSet()
            val fullNormalised = fullSet.toSet()
            assertEquals(fullNormalised, allNormalised)
        }

        @Test
        fun `game starts with empty board and first player's turn`() {
            val state = engine.startGame(twoPlayerConfig())
            assertTrue(state.board.isEmpty())
            assertEquals(0, state.currentPlayerIndex)
            assertFalse(state.isGameOver)
            assertNull(state.winner)
        }

        @Test
        fun `same seed produces same deal`() {
            val state1 = engine.startGame(twoPlayerConfig(seed = 99L))
            val state2 = engine.startGame(twoPlayerConfig(seed = 99L))
            assertEquals(state1.players.map { it.hand }, state2.players.map { it.hand })
        }

        @Test
        fun `different seeds produce different deals`() {
            val state1 = engine.startGame(twoPlayerConfig(seed = 1L))
            val state2 = engine.startGame(twoPlayerConfig(seed = 2L))
            assertNotEquals(
                state1.players.map { it.hand },
                state2.players.map { it.hand }
            )
        }
    }

    // =========================================================================
    // playDomino — valid moves
    // =========================================================================

    @Nested
    inner class PlayDominoValid {

        @Test
        fun `first play on empty board accepts any domino`() {
            val domino = Domino(3, 5)
            val state = stateWith(listOf(listOf(domino), listOf(Domino(1, 2))))
            val next = engine.playDomino(state, domino, BoardEnd.LEFT)

            assertEquals(listOf(domino), next.board)
            assertFalse(next.currentPlayer.hand.contains(domino))
        }

        @Test
        fun `play on right end extends board correctly`() {
            // Board: [2|4], right end = 4
            val board = listOf(Domino(2, 4))
            val domino = Domino(4, 6)
            val state = stateWith(
                playerHands = listOf(listOf(domino), emptyList()),
                board = board
            )
            val next = engine.playDomino(state, domino, BoardEnd.RIGHT)

            assertEquals(2, next.board.size)
            assertEquals(Domino(2, 4), next.board[0])
            assertEquals(Domino(4, 6), next.board[1])
            assertEquals(2, next.leftEnd)
            assertEquals(6, next.rightEnd)
        }

        @Test
        fun `play on left end prepends domino correctly`() {
            // Board: [2|4], left end = 2
            val board = listOf(Domino(2, 4))
            val domino = Domino(0, 2)
            val state = stateWith(
                playerHands = listOf(listOf(domino), emptyList()),
                board = board
            )
            val next = engine.playDomino(state, domino, BoardEnd.LEFT)

            // Domino (0,2) must have its right side (2) connect to the left end (2)
            // So domino placed as-is: [0|2] prepended → [0|2][2|4], left end = 0
            assertEquals(2, next.board.size)
            assertEquals(Domino(0, 2), next.board[0])
            assertEquals(0, next.leftEnd)
            assertEquals(4, next.rightEnd)
        }

        @Test
        fun `domino is automatically flipped when needed for right end`() {
            // Board: [2|4], right end = 4
            // Domino (6,4) must be flipped to (4,6) to connect left side to right end 4
            val board = listOf(Domino(2, 4))
            val domino = Domino(6, 4)
            val state = stateWith(
                playerHands = listOf(listOf(domino), emptyList()),
                board = board
            )
            val next = engine.playDomino(state, domino, BoardEnd.RIGHT)

            assertEquals(2, next.board.size)
            // Oriented domino: (4,6)
            assertEquals(Domino(4, 6), next.board[1])
            assertEquals(6, next.rightEnd)
        }

        @Test
        fun `domino is automatically flipped when needed for left end`() {
            // Board: [2|4], left end = 2
            // Domino (2,5) must be flipped to (5,2) so its right side (2) connects to left end 2
            val board = listOf(Domino(2, 4))
            val domino = Domino(2, 5)
            val state = stateWith(
                playerHands = listOf(listOf(domino), emptyList()),
                board = board
            )
            val next = engine.playDomino(state, domino, BoardEnd.LEFT)

            assertEquals(Domino(5, 2), next.board[0])
            assertEquals(5, next.leftEnd)
        }

        @Test
        fun `playing last tile marks game over and sets winner`() {
            val domino = Domino(3, 5)
            val state = stateWith(
                playerHands = listOf(listOf(domino), listOf(Domino(0, 0))),
                board = emptyList()
            )
            val next = engine.playDomino(state, domino, BoardEnd.LEFT)

            assertTrue(next.isGameOver)
            assertNotNull(next.winner)
            assertEquals("p1", next.winner!!.id)
            assertTrue(next.winner!!.hand.isEmpty())
        }

        @Test
        fun `playing a non-last tile does not end game`() {
            val domino = Domino(3, 5)
            val state = stateWith(
                playerHands = listOf(listOf(domino, Domino(1, 1)), listOf(Domino(0, 0))),
                board = emptyList()
            )
            val next = engine.playDomino(state, domino, BoardEnd.LEFT)

            assertFalse(next.isGameOver)
            assertNull(next.winner)
        }

        @Test
        fun `double domino placed on left end preserves orientation`() {
            // Board: [3|5], left end = 3
            // Domino [3|3]: right side is 3, matches → no flip
            val board = listOf(Domino(3, 5))
            val domino = Domino(3, 3)
            val state = stateWith(
                playerHands = listOf(listOf(domino), emptyList()),
                board = board
            )
            val next = engine.playDomino(state, domino, BoardEnd.LEFT)

            assertEquals(Domino(3, 3), next.board[0])
            assertEquals(3, next.leftEnd)
        }
    }

    // =========================================================================
    // playDomino — invalid moves
    // =========================================================================

    @Nested
    inner class PlayDominoInvalid {

        @Test
        fun `playing domino not in hand throws IllegalArgumentException`() {
            val state = stateWith(
                playerHands = listOf(listOf(Domino(1, 2)), emptyList())
            )
            assertThrows<IllegalArgumentException> {
                engine.playDomino(state, Domino(3, 4), BoardEnd.LEFT)
            }
        }

        @Test
        fun `playing domino that does not match board end throws IllegalArgumentException`() {
            // Board right end = 4; trying to play (1,2) on RIGHT
            val board = listOf(Domino(2, 4))
            val state = stateWith(
                playerHands = listOf(listOf(Domino(1, 2)), emptyList()),
                board = board
            )
            assertThrows<IllegalArgumentException> {
                engine.playDomino(state, Domino(1, 2), BoardEnd.RIGHT)
            }
        }

        @Test
        fun `playing a domino after game is over throws IllegalArgumentException`() {
            val domino = Domino(1, 2)
            val winner = Player("p1", "Player 1", emptyList())
            val state = GameState(
                players = listOf(winner, Player("p2", "Player 2", listOf(domino))),
                board = listOf(Domino(0, 1)),
                boneyard = emptyList(),
                currentPlayerIndex = 1,
                isGameOver = true,
                winner = winner
            )
            assertThrows<IllegalArgumentException> {
                engine.playDomino(state, domino, BoardEnd.RIGHT)
            }
        }
    }

    // =========================================================================
    // drawDomino
    // =========================================================================

    @Nested
    inner class DrawDomino {

        @Test
        fun `draw adds tile to current player's hand`() {
            val drawnTile = Domino(5, 6)
            val state = stateWith(
                playerHands = listOf(listOf(Domino(1, 1)), listOf(Domino(2, 2))),
                boneyard = listOf(drawnTile, Domino(0, 0))
            )
            val next = engine.drawDomino(state)

            assertEquals(2, next.players[0].hand.size)
            assertTrue(next.players[0].hand.contains(drawnTile))
            assertEquals(1, next.boneyard.size)
        }

        @Test
        fun `draw does not advance the turn`() {
            val state = stateWith(
                playerHands = listOf(listOf(Domino(1, 1)), listOf(Domino(2, 2))),
                boneyard = listOf(Domino(5, 6))
            )
            val next = engine.drawDomino(state)

            assertEquals(0, next.currentPlayerIndex)
        }

        @Test
        fun `draw when boneyard empty skips turn to next player`() {
            val state = stateWith(
                playerHands = listOf(listOf(Domino(1, 1)), listOf(Domino(2, 2))),
                boneyard = emptyList()
            )
            val next = engine.drawDomino(state)

            assertEquals(1, next.currentPlayerIndex)
            assertEquals(1, next.players[0].hand.size)  // hand unchanged
        }

        @Test
        fun `draw when boneyard empty with 3 players cycles to next`() {
            val state = stateWith(
                playerHands = listOf(
                    listOf(Domino(1, 1)),
                    listOf(Domino(2, 2)),
                    listOf(Domino(3, 3))
                ),
                boneyard = emptyList(),
                currentPlayerIndex = 2
            )
            val next = engine.drawDomino(state)

            assertEquals(0, next.currentPlayerIndex)  // wraps around
        }

        @Test
        fun `draw removes top tile from boneyard`() {
            val top = Domino(5, 6)
            val second = Domino(0, 0)
            val state = stateWith(
                playerHands = listOf(emptyList(), emptyList()),
                boneyard = listOf(top, second)
            )
            val next = engine.drawDomino(state)

            assertEquals(listOf(second), next.boneyard)
            assertTrue(next.players[0].hand.contains(top))
        }
    }

    // =========================================================================
    // getValidMoves
    // =========================================================================

    @Nested
    inner class GetValidMoves {

        @Test
        fun `on empty board every tile is a valid move`() {
            val hand = listOf(Domino(1, 2), Domino(3, 4), Domino(5, 6))
            val state = stateWith(playerHands = listOf(hand, emptyList()))
            val moves = engine.getValidMoves(state, state.currentPlayer)

            assertEquals(3, moves.size)
            hand.forEach { d ->
                assertTrue(moves.any { it.domino == d && it.end == BoardEnd.LEFT })
            }
        }

        @Test
        fun `returns no moves when no tile matches board ends`() {
            // Board: [2|4], ends are 2 and 4
            // Hand has only (5,6) — matches neither
            val state = stateWith(
                playerHands = listOf(listOf(Domino(5, 6)), emptyList()),
                board = listOf(Domino(2, 4))
            )
            val moves = engine.getValidMoves(state, state.currentPlayer)
            assertTrue(moves.isEmpty())
        }

        @Test
        fun `returns correct moves matching right end`() {
            // Board: [2|4], right end = 4
            val state = stateWith(
                playerHands = listOf(listOf(Domino(4, 6)), emptyList()),
                board = listOf(Domino(2, 4))
            )
            val moves = engine.getValidMoves(state, state.currentPlayer)
            assertTrue(moves.any { it.domino == Domino(4, 6) && it.end == BoardEnd.RIGHT && !it.needsFlip })
        }

        @Test
        fun `returns correct moves matching left end with flip`() {
            // Board: [2|4], left end = 2
            // Domino (2,5): left matches leftEnd → needsFlip = true
            val state = stateWith(
                playerHands = listOf(listOf(Domino(2, 5)), emptyList()),
                board = listOf(Domino(2, 4))
            )
            val moves = engine.getValidMoves(state, state.currentPlayer)
            assertTrue(moves.any { it.domino == Domino(2, 5) && it.end == BoardEnd.LEFT && it.needsFlip })
        }

        @Test
        fun `returns correct moves matching right end with flip`() {
            // Board: [2|4], right end = 4
            // Domino (6,4): right matches rightEnd → needsFlip = true
            val state = stateWith(
                playerHands = listOf(listOf(Domino(6, 4)), emptyList()),
                board = listOf(Domino(2, 4))
            )
            val moves = engine.getValidMoves(state, state.currentPlayer)
            assertTrue(moves.any { it.domino == Domino(6, 4) && it.end == BoardEnd.RIGHT && it.needsFlip })
        }

        @Test
        fun `double tile matching end listed once per end`() {
            // Board: [2|4], ends 2 and 4
            // Domino [4|4]: matches rightEnd (no flip); also left is 4 which matches... no wait
            // Actually leftEnd=2, so [4|4] only matches RIGHT end
            val state = stateWith(
                playerHands = listOf(listOf(Domino(4, 4)), emptyList()),
                board = listOf(Domino(2, 4))
            )
            val moves = engine.getValidMoves(state, state.currentPlayer)
            assertEquals(1, moves.size)
            assertTrue(moves[0].end == BoardEnd.RIGHT)
        }

        @Test
        fun `tile matching both ends appears twice`() {
            // Board: [3|3], leftEnd=3, rightEnd=3
            // Domino [3|5]: left matches leftEnd (needsFlip on LEFT), right doesn't match leftEnd;
            // left matches rightEnd (no flip on RIGHT)
            val state = stateWith(
                playerHands = listOf(listOf(Domino(3, 5)), emptyList()),
                board = listOf(Domino(3, 3))
            )
            val moves = engine.getValidMoves(state, state.currentPlayer)
            assertEquals(2, moves.size)
        }
    }

    // =========================================================================
    // isBlocked
    // =========================================================================

    @Nested
    inner class IsBlocked {

        @Test
        fun `not blocked when boneyard is not empty`() {
            // Even if no player can play, boneyard not empty → not blocked
            val state = stateWith(
                playerHands = listOf(listOf(Domino(5, 6)), listOf(Domino(5, 6))),
                board = listOf(Domino(1, 2)),
                boneyard = listOf(Domino(0, 0))
            )
            assertFalse(engine.isBlocked(state))
        }

        @Test
        fun `not blocked when at least one player can play`() {
            // Board ends 1 and 2; player 1 has (1,3) which matches left end
            val state = stateWith(
                playerHands = listOf(listOf(Domino(1, 3)), listOf(Domino(5, 6))),
                board = listOf(Domino(1, 2)),
                boneyard = emptyList()
            )
            assertFalse(engine.isBlocked(state))
        }

        @Test
        fun `blocked when boneyard empty and no player has valid move`() {
            // Board [1|2], ends 1 and 2; all hands contain only (5,6)
            val state = stateWith(
                playerHands = listOf(listOf(Domino(5, 6)), listOf(Domino(5, 6))),
                board = listOf(Domino(1, 2)),
                boneyard = emptyList()
            )
            assertTrue(engine.isBlocked(state))
        }

        @Test
        fun `not blocked when board is empty even with empty boneyard`() {
            // An empty board means any tile is valid
            val state = stateWith(
                playerHands = listOf(listOf(Domino(1, 1)), listOf(Domino(2, 2))),
                board = emptyList(),
                boneyard = emptyList()
            )
            assertFalse(engine.isBlocked(state))
        }
    }

    // =========================================================================
    // calculateScores — normal win
    // =========================================================================

    @Nested
    inner class CalculateScoresNormalWin {

        @Test
        fun `winner with empty hand scores sum of other players pips`() {
            // Winner (p1) has empty hand; p2 has (3,5)=8 pips; p3 has (1,2)=3 pips → winner gets 11
            val winner = Player("p1", "Player 1", emptyList())
            val p2 = Player("p2", "Player 2", listOf(Domino(3, 5)))
            val p3 = Player("p3", "Player 3", listOf(Domino(1, 2)))
            val state = GameState(
                players = listOf(winner, p2, p3),
                board = listOf(Domino(0, 1)),
                boneyard = emptyList(),
                currentPlayerIndex = 0,
                isGameOver = true,
                winner = winner
            )

            val scores = engine.calculateScores(state)
            assertEquals(11, scores[winner])
            assertEquals(0, scores[p2])
            assertEquals(0, scores[p3])
        }

        @Test
        fun `two-player normal win scores correctly`() {
            val winner = Player("p1", "Player 1", emptyList())
            val loser = Player("p2", "Player 2", listOf(Domino(6, 6), Domino(5, 5)))
            val state = GameState(
                players = listOf(winner, loser),
                board = listOf(Domino(0, 0)),
                boneyard = emptyList(),
                currentPlayerIndex = 0,
                isGameOver = true,
                winner = winner
            )

            val scores = engine.calculateScores(state)
            assertEquals(22, scores[winner])  // 12 + 10
            assertEquals(0, scores[loser])
        }
    }

    // =========================================================================
    // calculateScores — blockade win
    // =========================================================================

    @Nested
    inner class CalculateScoresBlockadeWin {

        @Test
        fun `blockade winner scores difference against each other player`() {
            // p1 = 5 pips, p2 = 12 pips, p3 = 8 pips → winner is p1
            // score = (12-5) + (8-5) = 7 + 3 = 10
            val p1 = Player("p1", "Player 1", listOf(Domino(2, 3)))          // 5 pips
            val p2 = Player("p2", "Player 2", listOf(Domino(6, 6)))          // 12 pips
            val p3 = Player("p3", "Player 3", listOf(Domino(3, 5)))          // 8 pips
            val state = GameState(
                players = listOf(p1, p2, p3),
                board = listOf(Domino(0, 0)),
                boneyard = emptyList(),
                currentPlayerIndex = 0
            )

            val scores = engine.calculateScores(state)
            assertEquals(10, scores[p1])
            assertEquals(0, scores[p2])
            assertEquals(0, scores[p3])
        }

        @Test
        fun `blockade win two players`() {
            // p1 = 3 pips, p2 = 10 pips → winner p1 scores (10-3) = 7
            val p1 = Player("p1", "Player 1", listOf(Domino(1, 2)))          // 3 pips
            val p2 = Player("p2", "Player 2", listOf(Domino(4, 6)))          // 10 pips
            val state = GameState(
                players = listOf(p1, p2),
                board = listOf(Domino(0, 0)),
                boneyard = emptyList(),
                currentPlayerIndex = 0
            )

            val scores = engine.calculateScores(state)
            assertEquals(7, scores[p1])
            assertEquals(0, scores[p2])
        }

        @Test
        fun `blockade with tied pip counts — first player in list wins`() {
            // p1 = 5 pips, p2 = 5 pips; p1 wins (min by picks first), score = max(5-5,0) = 0
            val p1 = Player("p1", "Player 1", listOf(Domino(2, 3)))          // 5 pips
            val p2 = Player("p2", "Player 2", listOf(Domino(1, 4)))          // 5 pips
            val state = GameState(
                players = listOf(p1, p2),
                board = listOf(Domino(0, 0)),
                boneyard = emptyList(),
                currentPlayerIndex = 0
            )

            val scores = engine.calculateScores(state)
            // Winner scores 0 because difference is 0; others also 0
            val winnerScore = scores.values.maxOrNull() ?: 0
            assertEquals(0, winnerScore)
        }
    }

    // =========================================================================
    // nextTurn
    // =========================================================================

    @Nested
    inner class NextTurn {

        @Test
        fun `next turn advances to second player`() {
            val state = stateWith(
                playerHands = listOf(listOf(Domino(1, 1)), listOf(Domino(2, 2))),
                currentPlayerIndex = 0
            )
            val next = engine.nextTurn(state)
            assertEquals(1, next.currentPlayerIndex)
        }

        @Test
        fun `next turn wraps around after last player`() {
            val state = stateWith(
                playerHands = listOf(
                    listOf(Domino(1, 1)),
                    listOf(Domino(2, 2)),
                    listOf(Domino(3, 3))
                ),
                currentPlayerIndex = 2
            )
            val next = engine.nextTurn(state)
            assertEquals(0, next.currentPlayerIndex)
        }
    }

    // =========================================================================
    // Integration test — full game flow
    // =========================================================================

    @Nested
    inner class FullGameIntegration {

        /**
         * Simulates a complete two-player game with a fixed seed, playing greedily
         * (always pick the first valid move) until the game ends.
         */
        @Test
        fun `complete game plays to conclusion without errors`() {
            var state = engine.startGame(twoPlayerConfig(seed = 123L))
            var maxTurns = 500  // guard against infinite loops in tests

            while (!state.isGameOver && !engine.isBlocked(state) && maxTurns-- > 0) {
                val player = state.currentPlayer
                val validMoves = engine.getValidMoves(state, player)

                state = if (validMoves.isNotEmpty()) {
                    val move = validMoves.first()
                    val afterPlay = engine.playDomino(state, move.domino, move.end)
                    if (afterPlay.isGameOver) afterPlay else engine.nextTurn(afterPlay)
                } else {
                    engine.drawDomino(state)
                }
            }

            assertTrue(state.isGameOver || engine.isBlocked(state),
                "Game must end either by normal win or blockade")
        }

        @Test
        fun `scores are computed after normal win`() {
            // Manually construct a near-end state and play the last tile
            val p1 = Player("p1", "Player 1", listOf(Domino(3, 4)))
            val p2 = Player("p2", "Player 2", listOf(Domino(2, 2), Domino(0, 1)))
            val state = GameState(
                players = listOf(p1, p2),
                board = listOf(Domino(1, 3)),          // left=1, right=3 → p1's (3,4) fits RIGHT
                boneyard = emptyList(),
                currentPlayerIndex = 0
            )

            val afterPlay = engine.playDomino(state, Domino(3, 4), BoardEnd.RIGHT)
            assertTrue(afterPlay.isGameOver)

            val scores = engine.calculateScores(afterPlay)
            val winnerScore = scores.getValue(afterPlay.winner!!)
            assertEquals(5, winnerScore)  // p2 has (2,2)=4 + (0,1)=1 = 5
        }

        @Test
        fun `scores are computed after blockade`() {
            val p1 = Player("p1", "Player 1", listOf(Domino(5, 6)))  // 11 pips
            val p2 = Player("p2", "Player 2", listOf(Domino(5, 6)))  // 11 pips — tied
            val state = GameState(
                players = listOf(p1, p2),
                board = listOf(Domino(1, 2)),
                boneyard = emptyList(),
                currentPlayerIndex = 0
            )

            assertTrue(engine.isBlocked(state))
            val scores = engine.calculateScores(state)
            assertNotNull(scores)
            assertEquals(2, scores.size)
        }

        @Test
        fun `four-player game deals and runs without errors`() {
            var state = engine.startGame(fourPlayerConfig(seed = 7L))
            var maxTurns = 1000

            while (!state.isGameOver && !engine.isBlocked(state) && maxTurns-- > 0) {
                val player = state.currentPlayer
                val validMoves = engine.getValidMoves(state, player)

                state = if (validMoves.isNotEmpty()) {
                    val move = validMoves.first()
                    val afterPlay = engine.playDomino(state, move.domino, move.end)
                    if (afterPlay.isGameOver) afterPlay else engine.nextTurn(afterPlay)
                } else {
                    engine.drawDomino(state)
                }
            }

            assertTrue(state.isGameOver || engine.isBlocked(state))
        }
    }
}

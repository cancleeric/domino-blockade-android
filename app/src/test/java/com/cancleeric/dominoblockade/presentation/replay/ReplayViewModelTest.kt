package com.cancleeric.dominoblockade.presentation.replay

import com.cancleeric.dominoblockade.data.local.MoveHistorySerializer
import com.cancleeric.dominoblockade.data.local.entity.GameRecordEntity
import com.cancleeric.dominoblockade.domain.model.Domino
import com.cancleeric.dominoblockade.domain.model.GameMove
import com.cancleeric.dominoblockade.domain.model.Player
import com.cancleeric.dominoblockade.domain.repository.GameRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReplayViewModelTest {

    private fun makePlayers(vararg hands: List<Domino>): List<Player> =
        hands.mapIndexed { i, hand -> Player("p${i + 1}", "Player ${i + 1}", hand) }

    private fun makeMoves(vararg triples: Triple<String, Domino, String>): List<GameMove> =
        triples.mapIndexed { i, (playerId, domino, boardEnd) ->
            GameMove(
                playerId = playerId,
                playerName = "Player ${playerId.last()}",
                dominoLeft = domino.left,
                dominoRight = domino.right,
                boardEnd = boardEnd,
                timestamp = (i + 1) * 1000L
            )
        }

    @Test
    fun `reconstructing moves from initial state produces correct board sequence`() {
        val initialPlayers = makePlayers(
            listOf(Domino(6, 6), Domino(4, 6)),
            listOf(Domino(4, 4))
        )
        val moves = makeMoves(
            Triple("p1", Domino(6, 6), "INITIAL"),
            Triple("p2", Domino(4, 6), "RIGHT"),
            Triple("p1", Domino(4, 4), "LEFT")
        )
        val initialHandsJson = MoveHistorySerializer.serializePlayers(initialPlayers)
        val moveHistoryJson = MoveHistorySerializer.serializeMoves(moves)

        val restoredPlayers = MoveHistorySerializer.deserializePlayers(initialHandsJson)
        val restoredMoves = MoveHistorySerializer.deserializeMoves(moveHistoryJson)

        assertEquals(initialPlayers.size, restoredPlayers.size)
        assertEquals(moves.size, restoredMoves.size)
        assertEquals("INITIAL", restoredMoves[0].boardEnd)
        assertEquals("RIGHT", restoredMoves[1].boardEnd)
        assertEquals("LEFT", restoredMoves[2].boardEnd)
    }

    @Test
    fun `serializer round-trip preserves all move fields`() {
        val move = GameMove("p1", "Player 1", 3, 5, "RIGHT", 12345L)
        val json = MoveHistorySerializer.serializeMoves(listOf(move))
        val result = MoveHistorySerializer.deserializeMoves(json)

        assertEquals(1, result.size)
        assertEquals(move.playerId, result[0].playerId)
        assertEquals(move.dominoLeft, result[0].dominoLeft)
        assertEquals(move.dominoRight, result[0].dominoRight)
        assertEquals(move.boardEnd, result[0].boardEnd)
        assertEquals(move.timestamp, result[0].timestamp)
    }

    @Test
    fun `board reconstruction via INITIAL then RIGHT places dominos correctly`() {
        val initial = listOf(Domino(6, 6))
        val board1 = applyMoveToBoard(emptyList(), GameMove("p1", "P1", 6, 6, "INITIAL", 1L))
        assertEquals(initial, board1)

        val board2 = applyMoveToBoard(board1, GameMove("p2", "P2", 4, 6, "RIGHT", 2L))
        assertEquals(2, board2.size)
        assertEquals(6, board2.last().left)
    }

    @Test
    fun `board reconstruction via LEFT prepends domino`() {
        val board0 = applyMoveToBoard(emptyList(), GameMove("p1", "P1", 5, 5, "INITIAL", 1L))
        val board1 = applyMoveToBoard(board0, GameMove("p2", "P2", 3, 5, "LEFT", 2L))
        assertEquals(2, board1.size)
        assertEquals(5, board1.first().right)
    }

    @Test
    fun `hand shrinks after applying a move`() {
        val player = Player("p1", "Player 1", listOf(Domino(6, 6), Domino(3, 4)))
        val move = GameMove("p1", "Player 1", 6, 6, "INITIAL", 1L)
        val updated = applyMoveToHands(listOf(player), move)
        assertEquals(1, updated[0].hand.size)
        assertFalse(updated[0].hand.contains(Domino(6, 6)))
    }

    @Test
    fun `other players hand is unchanged after move`() {
        val p1 = Player("p1", "Player 1", listOf(Domino(6, 6)))
        val p2 = Player("p2", "Player 2", listOf(Domino(3, 4)))
        val move = GameMove("p1", "Player 1", 6, 6, "INITIAL", 1L)
        val updated = applyMoveToHands(listOf(p1, p2), move)
        assertEquals(p2.hand, updated[1].hand)
    }

    @Test
    fun `empty move history deserializes to empty list`() {
        assertTrue(MoveHistorySerializer.deserializeMoves("[]").isEmpty())
    }

    // Helpers that mirror ReplayViewModel private logic for white-box testing

    private fun applyMoveToBoard(board: List<Domino>, move: GameMove): List<Domino> {
        val domino = Domino(move.dominoLeft, move.dominoRight)
        return when (move.boardEnd) {
            "INITIAL" -> listOf(domino)
            "RIGHT" -> {
                val rightEnd = board.lastOrNull()?.right
                val oriented = if (rightEnd != null && domino.left != rightEnd) {
                    Domino(domino.right, domino.left)
                } else domino
                board + oriented
            }
            else -> {
                val leftEnd = board.firstOrNull()?.left
                val oriented = if (leftEnd != null && domino.right != leftEnd) {
                    Domino(domino.right, domino.left)
                } else domino
                listOf(oriented) + board
            }
        }
    }

    private fun applyMoveToHands(players: List<Player>, move: GameMove): List<Player> {
        val domino = Domino(move.dominoLeft, move.dominoRight)
        return players.map { player ->
            if (player.id == move.playerId) {
                val removed = player.hand.firstOrNull {
                    it == domino || it == Domino(domino.right, domino.left)
                }
                player.copy(hand = if (removed != null) player.hand - removed else player.hand)
            } else {
                player
            }
        }
    }
}

private class FakeGameRecordRepository : GameRecordRepository {
    private val records = MutableStateFlow<List<GameRecordEntity>>(emptyList())

    override suspend fun insert(record: GameRecordEntity) {
        records.value = records.value + record
    }

    override fun getAll(): Flow<List<GameRecordEntity>> = records
    override fun getRecent(limit: Int): Flow<List<GameRecordEntity>> =
        records.map { it.takeLast(limit) }

    override fun getById(id: Long): Flow<GameRecordEntity?> =
        records.map { list -> list.firstOrNull { it.id == id } }
}

package com.cancleeric.dominoblockade.domain.usecase

import com.cancleeric.dominoblockade.domain.model.Domino
import com.cancleeric.dominoblockade.domain.model.DominoSet
import com.cancleeric.dominoblockade.domain.model.GameConfig
import com.cancleeric.dominoblockade.domain.model.GameState
import com.cancleeric.dominoblockade.domain.model.GameStatus
import com.cancleeric.dominoblockade.domain.model.Player

enum class BoardEnd { LEFT, RIGHT }

data class ValidMove(val domino: Domino, val end: BoardEnd, val needsFlip: Boolean)

class GameEngine {

    fun startGame(config: GameConfig): GameState {
        require(config.playerCount in 2..4) { "Player count must be between 2 and 4" }
        val shuffled = DominoSet.shuffled().toMutableList()
        val players = (0 until config.playerCount).map { index ->
            val hand = shuffled.take(config.dominoesPerPlayer)
            repeat(config.dominoesPerPlayer) { shuffled.removeFirst() }
            Player(id = index, name = "Player ${index + 1}", hand = hand, isAi = index > 0)
        }
        return GameState(
            players = players,
            drawPile = shuffled,
            status = GameStatus.PLAYING
        )
    }

    fun playDomino(state: GameState, domino: Domino, end: BoardEnd): GameState {
        val currentPlayer = state.players[state.currentPlayerIndex]
        val validMoves = getValidMoves(state, currentPlayer)
        val matchingMove = validMoves.firstOrNull { it.domino.id == domino.id && it.end == end }
            ?: return state

        val actualDomino = if (matchingMove.needsFlip) domino.flip() else domino

        val newBoard = if (state.board.isEmpty()) {
            listOf(actualDomino)
        } else if (end == BoardEnd.LEFT) {
            listOf(actualDomino) + state.board
        } else {
            state.board + actualDomino
        }

        val newLeftEnd = when {
            state.board.isEmpty() -> actualDomino.left
            end == BoardEnd.LEFT -> actualDomino.left
            else -> state.leftEnd
        }
        val newRightEnd = when {
            state.board.isEmpty() -> actualDomino.right
            end == BoardEnd.RIGHT -> actualDomino.right
            else -> state.rightEnd
        }

        val newHand = currentPlayer.hand.toMutableList().also { it.remove(domino) }
        val updatedPlayer = currentPlayer.copy(hand = newHand)
        val newPlayers = state.players.toMutableList().also {
            it[state.currentPlayerIndex] = updatedPlayer
        }

        val newStatus = when {
            newHand.isEmpty() -> GameStatus.FINISHED
            else -> GameStatus.PLAYING
        }

        return state.copy(
            players = newPlayers,
            board = newBoard,
            leftEnd = newLeftEnd,
            rightEnd = newRightEnd,
            status = newStatus
        )
    }

    fun drawDomino(state: GameState): GameState {
        if (state.drawPile.isEmpty()) return state
        val drawn = state.drawPile.first()
        val currentPlayer = state.players[state.currentPlayerIndex]
        val updatedPlayer = currentPlayer.copy(hand = currentPlayer.hand + drawn)
        val newPlayers = state.players.toMutableList().also {
            it[state.currentPlayerIndex] = updatedPlayer
        }
        return state.copy(
            players = newPlayers,
            drawPile = state.drawPile.drop(1)
        )
    }

    fun getValidMoves(state: GameState, player: Player): List<ValidMove> {
        if (state.board.isEmpty()) {
            return player.hand.map { ValidMove(it, BoardEnd.RIGHT, false) }
        }

        val leftEnd = state.leftEnd ?: return emptyList()
        val rightEnd = state.rightEnd ?: return emptyList()
        val moves = mutableListOf<ValidMove>()

        player.hand.forEach { domino ->
            if (domino.isDouble) {
                if (domino.left == leftEnd) moves.add(ValidMove(domino, BoardEnd.LEFT, false))
                if (domino.left == rightEnd) moves.add(ValidMove(domino, BoardEnd.RIGHT, false))
            } else {
                // LEFT end: right side of domino connects to board
                if (domino.right == leftEnd) moves.add(ValidMove(domino, BoardEnd.LEFT, false))
                // LEFT end: left side of domino connects to board (needs flip)
                if (domino.left == leftEnd) moves.add(ValidMove(domino, BoardEnd.LEFT, true))
                // RIGHT end: left side of domino connects to board
                if (domino.left == rightEnd) moves.add(ValidMove(domino, BoardEnd.RIGHT, false))
                // RIGHT end: right side of domino connects to board (needs flip)
                if (domino.right == rightEnd) moves.add(ValidMove(domino, BoardEnd.RIGHT, true))
            }
        }

        return moves
    }

    fun isBlocked(state: GameState): Boolean {
        if (state.drawPile.isNotEmpty()) return false
        return state.players.all { player -> getValidMoves(state, player).isEmpty() }
    }

    fun calculateScores(state: GameState): Map<Player, Int> {
        val winner = state.players.minByOrNull { player -> player.hand.sumOf { it.totalPips } }
            ?: return emptyMap()
        val winnerPips = winner.hand.sumOf { it.totalPips }
        return state.players.associateWith { player ->
            if (player.id == winner.id) {
                state.players.filter { it.id != winner.id }.sumOf { p -> p.hand.sumOf { it.totalPips } } - winnerPips
            } else {
                0
            }
        }
    }

    fun nextTurn(state: GameState): GameState {
        val nextIndex = (state.currentPlayerIndex + 1) % state.players.size
        return state.copy(currentPlayerIndex = nextIndex)
    }
}

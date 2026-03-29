package com.cancleeric.dominoblockade.domain

import com.cancleeric.dominoblockade.domain.model.*

class GameEngine {

    fun createFullSet(): List<Domino> {
        val set = mutableListOf<Domino>()
        for (i in 0..6) {
            for (j in i..6) {
                set.add(Domino(i, j))
            }
        }
        return set.shuffled()
    }

    fun deal(numPlayers: Int): GameState {
        val fullSet = createFullSet().toMutableList()
        val tilesPerPlayer = if (numPlayers <= 2) 7 else 5
        val players = (0 until numPlayers).map { idx ->
            val hand = fullSet.take(tilesPerPlayer).toList()
            repeat(tilesPerPlayer) { fullSet.removeAt(0) }
            Player(
                id = idx,
                name = if (idx == 0) "You" else "AI $idx",
                hand = hand,
                isAi = idx != 0
            )
        }
        val startingPlayerIndex = findStartingPlayer(players)
        return GameState(
            players = players,
            board = emptyList(),
            drawPile = fullSet.toList(),
            currentPlayerIndex = startingPlayerIndex,
            phase = GamePhase.PLAYING
        )
    }

    private fun findStartingPlayer(players: List<Player>): Int {
        var bestDouble = -1
        var startIdx = 0
        for ((idx, player) in players.withIndex()) {
            val highestDouble = player.hand.filter { it.isDouble }.maxByOrNull { it.left }
            if (highestDouble != null && highestDouble.left > bestDouble) {
                bestDouble = highestDouble.left
                startIdx = idx
            }
        }
        return startIdx
    }

    fun placeDomino(state: GameState, domino: Domino, end: BoardEnd): GameState {
        val player = state.currentPlayer
        val newHand = player.hand - domino
        val newBoard = state.board.toMutableList()

        val placed = if (state.board.isEmpty()) {
            PlacedDomino(domino)
        } else {
            when (end) {
                BoardEnd.LEFT -> {
                    val leftVal = state.leftEnd!!
                    val oriented = if (domino.right == leftVal) domino else domino.flipped()
                    PlacedDomino(oriented)
                }
                BoardEnd.RIGHT -> {
                    val rightVal = state.rightEnd!!
                    val oriented = if (domino.left == rightVal) domino else domino.flipped()
                    PlacedDomino(oriented)
                }
            }
        }

        when (end) {
            BoardEnd.LEFT -> newBoard.add(0, placed)
            BoardEnd.RIGHT -> newBoard.add(placed)
        }

        val updatedPlayers = state.players.toMutableList()
        updatedPlayers[state.currentPlayerIndex] = player.copy(hand = newHand)

        if (newHand.isEmpty()) {
            return state.copy(
                players = updatedPlayers,
                board = newBoard,
                phase = GamePhase.FINISHED,
                winnerIndex = state.currentPlayerIndex
            )
        }

        val nextPlayerIndex = (state.currentPlayerIndex + 1) % state.players.size
        val newState = state.copy(
            players = updatedPlayers,
            board = newBoard,
            currentPlayerIndex = nextPlayerIndex,
            turnNumber = state.turnNumber + 1
        )

        return checkBlockade(newState)
    }

    fun drawTile(state: GameState): GameState {
        if (state.drawPile.isEmpty()) return state
        val drawn = state.drawPile.first()
        val newPile = state.drawPile.drop(1)
        val player = state.currentPlayer
        val updatedPlayers = state.players.toMutableList()
        updatedPlayers[state.currentPlayerIndex] = player.copy(hand = player.hand + drawn)
        return state.copy(players = updatedPlayers, drawPile = newPile)
    }

    fun skipTurn(state: GameState): GameState {
        val nextPlayerIndex = (state.currentPlayerIndex + 1) % state.players.size
        val newState = state.copy(
            currentPlayerIndex = nextPlayerIndex,
            turnNumber = state.turnNumber + 1
        )
        return checkBlockade(newState)
    }

    private fun checkBlockade(state: GameState): GameState {
        val allBlocked = state.players.all { player ->
            state.validMovesFor(player).isEmpty()
        }
        return if (allBlocked && state.drawPile.isEmpty()) {
            val winnerIdx = state.players.withIndex().minByOrNull { (_, p) -> p.handPipCount }?.index
            state.copy(phase = GamePhase.BLOCKED, winnerIndex = winnerIdx)
        } else state
    }

    fun calculateScores(state: GameState): List<Int> {
        val winner = state.winnerIndex ?: return state.players.map { 0 }
        val totalPips = state.players.sumOf { it.handPipCount }
        return state.players.mapIndexed { idx, _ ->
            if (idx == winner) totalPips else 0
        }
    }
}

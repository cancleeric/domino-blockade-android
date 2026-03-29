package com.cancleeric.dominoblockade.ai

import com.cancleeric.dominoblockade.domain.model.*

class AiPlayer(private val difficulty: AiDifficulty) {

    data class AiMove(val domino: Domino, val end: BoardEnd)

    fun chooseMove(state: GameState): AiMove? {
        val validMoves = state.validMovesFor(state.currentPlayer)
        if (validMoves.isEmpty()) return null

        return when (difficulty) {
            AiDifficulty.EASY -> validMoves.random().let { AiMove(it.first, it.second) }
            AiDifficulty.MEDIUM -> chooseMediumMove(validMoves)
            AiDifficulty.HARD -> chooseHardMove(validMoves)
        }
    }

    private fun chooseMediumMove(moves: List<Pair<Domino, BoardEnd>>): AiMove {
        val best = moves.maxByOrNull { it.first.totalPips }!!
        return AiMove(best.first, best.second)
    }

    private fun chooseHardMove(moves: List<Pair<Domino, BoardEnd>>): AiMove {
        val doubleMoves = moves.filter { it.first.isDouble }
        if (doubleMoves.isNotEmpty()) {
            val best = doubleMoves.maxByOrNull { it.first.totalPips }!!
            return AiMove(best.first, best.second)
        }
        return chooseMediumMove(moves)
    }
}

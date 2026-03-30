package com.cancleeric.dominoblockade.domain.usecase

import com.cancleeric.dominoblockade.domain.model.GameState
import com.cancleeric.dominoblockade.domain.model.Player

/**
 * Computes the score for each player at the end of a game.
 *
 * **Normal win** (a player empties their hand):
 * - The winner scores the sum of all pip values remaining in every other player's hand.
 * - All other players score 0.
 *
 * **Blockade win** (game ends in deadlock; the player with the fewest pips wins):
 * - The winner scores the sum of (otherPlayerPips − winnerPips) for every other player.
 * - All other players score 0.
 */
class ScoreCalculator {

    fun calculateScores(state: GameState): Map<Player, Int> {
        val normalWinner = state.players.firstOrNull { it.hand.isEmpty() }
        if (normalWinner != null) {
            val totalPips = state.players
                .filter { it.id != normalWinner.id }
                .sumOf { player -> player.hand.sumOf { it.pips } }
            return state.players.associate { player ->
                player to if (player.id == normalWinner.id) totalPips else 0
            }
        }

        // Blockade win: the player with the fewest pips wins
        val playerPips = state.players.associateWith { player -> player.hand.sumOf { it.pips } }
        val blockadeWinner = state.players.minByOrNull { playerPips.getValue(it) }
            ?: return state.players.associateWith { 0 }
        val winnerPips = playerPips.getValue(blockadeWinner)

        return state.players.associate { player ->
            player to if (player.id == blockadeWinner.id) {
                state.players
                    .filter { it.id != blockadeWinner.id }
                    .sumOf { other -> (playerPips.getValue(other) - winnerPips).coerceAtLeast(0) }
            } else {
                0
            }
        }
    }
}

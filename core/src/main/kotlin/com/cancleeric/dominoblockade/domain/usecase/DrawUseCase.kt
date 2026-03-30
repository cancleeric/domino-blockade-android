package com.cancleeric.dominoblockade.domain.usecase

import com.cancleeric.dominoblockade.domain.model.GameState

/**
 * Handles drawing a tile from the boneyard.
 *
 * - If the boneyard is not empty: draws the top tile and adds it to the current player's hand.
 *   The turn does NOT automatically advance so the player may attempt to play the drawn tile.
 * - If the boneyard is empty: the current player's turn is skipped and the turn advances
 *   to the next player.
 */
class DrawUseCase {

    fun draw(state: GameState): GameState {
        if (state.boneyard.isEmpty()) {
            val nextIndex = (state.currentPlayerIndex + 1) % state.players.size
            return state.copy(currentPlayerIndex = nextIndex)
        }

        val drawnDomino = state.boneyard.first()
        val newBoneyard = state.boneyard.drop(1)

        val updatedHand = state.currentPlayer.hand + drawnDomino
        val updatedPlayer = state.currentPlayer.copy(hand = updatedHand)
        val updatedPlayers = state.players.toMutableList().also {
            it[state.currentPlayerIndex] = updatedPlayer
        }

        return state.copy(
            players = updatedPlayers,
            boneyard = newBoneyard
        )
    }
}

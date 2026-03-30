package com.cancleeric.dominoblockade.domain.usecase

import com.cancleeric.dominoblockade.domain.model.DominoSet
import com.cancleeric.dominoblockade.domain.model.GameConfig
import com.cancleeric.dominoblockade.domain.model.GameState
import kotlin.random.Random

/**
 * Handles the initial dealing of domino tiles to all players.
 *
 * - 2-player game: 7 tiles per player
 * - 3–4-player game: 5 tiles per player
 *
 * Remaining tiles become the boneyard (draw pile).
 */
class DealUseCase {

    fun deal(config: GameConfig): GameState {
        val tilesPerPlayer = if (config.players.size == 2) 7 else 5
        val random = config.randomSeed?.let { Random(it) } ?: Random.Default
        val shuffled = DominoSet.createFullSet().shuffledWith(random)

        var offset = 0
        val players = config.players.map { player ->
            val hand = shuffled.subList(offset, offset + tilesPerPlayer).toList()
            offset += tilesPerPlayer
            player.copy(hand = hand)
        }

        val boneyard = shuffled.drop(offset)

        return GameState(
            players = players,
            board = emptyList(),
            boneyard = boneyard,
            currentPlayerIndex = 0
        )
    }

    /** Fisher-Yates shuffle using the provided [Random] instance. */
    private fun <T> List<T>.shuffledWith(random: Random): List<T> {
        val list = toMutableList()
        for (i in list.size - 1 downTo 1) {
            val j = random.nextInt(i + 1)
            val tmp = list[i]
            list[i] = list[j]
            list[j] = tmp
        }
        return list
    }
}

package com.cancleeric.dominoblockade.domain.usecase

import com.cancleeric.dominoblockade.domain.model.Domino
import com.cancleeric.dominoblockade.domain.model.GameState
import com.cancleeric.dominoblockade.domain.model.Player

class StartGameUseCase {
    operator fun invoke(playerNames: List<String>): GameState {
        val allDominoes = generateDominoSet().shuffled()
        val handSize = if (playerNames.size <= 2) 7 else 5
        val players = playerNames.mapIndexed { index, name ->
            Player(
                id = "player_$index",
                name = name,
                hand = allDominoes.subList(index * handSize, (index + 1) * handSize)
            )
        }
        val boneyard = allDominoes.subList(playerNames.size * handSize, allDominoes.size)
        return GameState(
            players = players,
            boneyard = boneyard
        )
    }

    private fun generateDominoSet(): List<Domino> {
        val dominoes = mutableListOf<Domino>()
        for (i in 0..6) {
            for (j in i..6) {
                dominoes.add(Domino(i, j))
            }
        }
        return dominoes
    }
}

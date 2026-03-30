package com.cancleeric.dominoblockade.domain.model

import kotlin.random.Random

object DominoSet {

    private const val MAX_PIP = 6

    fun createFullSet(): List<Domino> {
        val dominoes = mutableListOf<Domino>()
        var id = 0
        for (i in 0..MAX_PIP) {
            for (j in i..MAX_PIP) {
                dominoes.add(Domino(left = i, right = j, id = id++))
            }
        }
        return dominoes
    }

    fun shuffled(random: Random = Random.Default): List<Domino> = createFullSet().shuffled(random)
}

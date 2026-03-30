package com.dominoblockade.domain.model

object DominoSet {
    fun createFullSet(): List<Domino> {
        val dominoes = mutableListOf<Domino>()
        var id = 0
        for (left in 0..6) {
            for (right in left..6) {
                dominoes.add(Domino(left = left, right = right, id = id++))
            }
        }
        return dominoes
    }

    fun shuffled(): List<Domino> = createFullSet().shuffled()
}

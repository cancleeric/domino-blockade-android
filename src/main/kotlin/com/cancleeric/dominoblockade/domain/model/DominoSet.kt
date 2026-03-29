package com.cancleeric.dominoblockade.domain.model

object DominoSet {
    fun createFullSet(): List<Domino> {
        val dominoes = mutableListOf<Domino>()
        var id = 0
        for (i in 0..6) {
            for (j in i..6) {
                dominoes.add(Domino(left = i, right = j, id = id++))
            }
        }
        return dominoes
    }

    fun shuffled(): List<Domino> = createFullSet().shuffled()
}

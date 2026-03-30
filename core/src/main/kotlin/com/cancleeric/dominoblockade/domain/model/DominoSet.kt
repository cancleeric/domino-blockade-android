package com.cancleeric.dominoblockade.domain.model

/**
 * Factory for generating the full standard set of 28 domino tiles (0–6).
 */
object DominoSet {
    /**
     * Creates the complete set of 28 dominoes: every combination of values 0–6
     * where the left value is ≤ the right value.
     */
    fun createFullSet(): List<Domino> {
        val dominoes = mutableListOf<Domino>()
        for (i in 0..6) {
            for (j in i..6) {
                dominoes.add(Domino(i, j))
            }
        }
        return dominoes
    }
}

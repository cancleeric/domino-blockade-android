package com.dominoblockade.domain.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class DominoSetTest {

    @Test
    fun `createFullSet generates exactly 28 dominoes`() {
        val set = DominoSet.createFullSet()
        assertEquals(28, set.size)
    }

    @Test
    fun `createFullSet contains all unique pairs from 0-0 to 6-6`() {
        val set = DominoSet.createFullSet()
        val pairs = set.map { Pair(it.left, it.right) }.toSet()

        val expected = mutableSetOf<Pair<Int, Int>>()
        for (left in 0..6) {
            for (right in left..6) {
                expected.add(Pair(left, right))
            }
        }
        assertEquals(expected, pairs)
    }

    @Test
    fun `createFullSet assigns unique ids to all dominoes`() {
        val set = DominoSet.createFullSet()
        val ids = set.map { it.id }.toSet()
        assertEquals(28, ids.size)
    }

    @Test
    fun `createFullSet contains no duplicate dominoes`() {
        val set = DominoSet.createFullSet()
        val unique = set.toSet()
        assertEquals(28, unique.size)
    }

    @Test
    fun `createFullSet includes double-blank (0-0)`() {
        val set = DominoSet.createFullSet()
        assertTrue(set.any { it.left == 0 && it.right == 0 })
    }

    @Test
    fun `createFullSet includes double-six (6-6)`() {
        val set = DominoSet.createFullSet()
        assertTrue(set.any { it.left == 6 && it.right == 6 })
    }

    @Test
    fun `createFullSet all pips are in range 0 to 6`() {
        val set = DominoSet.createFullSet()
        set.forEach { domino ->
            assertTrue(domino.left in 0..6)
            assertTrue(domino.right in 0..6)
        }
    }

    @Test
    fun `shuffled returns 28 dominoes`() {
        val shuffled = DominoSet.shuffled()
        assertEquals(28, shuffled.size)
    }

    @Test
    fun `shuffled contains the same dominoes as createFullSet`() {
        val full = DominoSet.createFullSet().map { Pair(it.left, it.right) }.toSet()
        val shuffled = DominoSet.shuffled().map { Pair(it.left, it.right) }.toSet()
        assertEquals(full, shuffled)
    }
}

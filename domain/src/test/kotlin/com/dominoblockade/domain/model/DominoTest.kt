package com.dominoblockade.domain.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class DominoTest {

    @Test
    fun `Domino creation stores left, right, and id`() {
        val domino = Domino(left = 3, right = 5, id = 10)
        assertEquals(3, domino.left)
        assertEquals(5, domino.right)
        assertEquals(10, domino.id)
    }

    @Test
    fun `isDouble returns true when left equals right`() {
        val double = Domino(left = 4, right = 4, id = 0)
        assertTrue(double.isDouble)
    }

    @Test
    fun `isDouble returns false when left differs from right`() {
        val nonDouble = Domino(left = 2, right = 5, id = 0)
        assertFalse(nonDouble.isDouble)
    }

    @Test
    fun `totalPips returns sum of left and right`() {
        val domino = Domino(left = 3, right = 4, id = 0)
        assertEquals(7, domino.totalPips)
    }

    @Test
    fun `totalPips is zero for double-blank`() {
        val domino = Domino(left = 0, right = 0, id = 0)
        assertEquals(0, domino.totalPips)
    }

    @Test
    fun `hasValue returns true when left matches`() {
        val domino = Domino(left = 3, right = 5, id = 0)
        assertTrue(domino.hasValue(3))
    }

    @Test
    fun `hasValue returns true when right matches`() {
        val domino = Domino(left = 3, right = 5, id = 0)
        assertTrue(domino.hasValue(5))
    }

    @Test
    fun `hasValue returns false when neither side matches`() {
        val domino = Domino(left = 3, right = 5, id = 0)
        assertFalse(domino.hasValue(2))
    }

    @Test
    fun `flip swaps left and right while preserving id`() {
        val original = Domino(left = 2, right = 6, id = 7)
        val flipped = original.flip()
        assertEquals(6, flipped.left)
        assertEquals(2, flipped.right)
        assertEquals(7, flipped.id)
    }

    @Test
    fun `flip of a double remains equal`() {
        val double = Domino(left = 5, right = 5, id = 0)
        assertEquals(double, double.flip())
    }

    @Test
    fun `double flip returns original`() {
        val original = Domino(left = 1, right = 4, id = 3)
        assertEquals(original, original.flip().flip())
    }
}

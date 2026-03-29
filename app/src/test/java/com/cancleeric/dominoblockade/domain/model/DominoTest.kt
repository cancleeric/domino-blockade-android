package com.cancleeric.dominoblockade.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DominoTest {
    @Test
    fun `total returns sum of left and right`() {
        val domino = Domino(3, 4)
        assertEquals(7, domino.total)
    }

    @Test
    fun `isDouble returns true when left equals right`() {
        val domino = Domino(5, 5)
        assertTrue(domino.isDouble)
    }

    @Test
    fun `isDouble returns false when left differs from right`() {
        val domino = Domino(3, 4)
        assertFalse(domino.isDouble)
    }
}

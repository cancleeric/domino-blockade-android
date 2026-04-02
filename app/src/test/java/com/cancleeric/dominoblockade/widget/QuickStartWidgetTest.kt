package com.cancleeric.dominoblockade.widget

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuickStartWidgetTest {

    @Test
    fun `EXTRA_QUICK_START constant has expected value`() {
        assertEquals("extra_quick_start", QuickStartWidget.EXTRA_QUICK_START)
    }

    @Test
    fun `EXTRA_PLAYER_COUNT constant has expected value`() {
        assertEquals("extra_player_count", QuickStartWidget.EXTRA_PLAYER_COUNT)
    }

    @Test
    fun `QUICK_START_PLAYER_COUNT is two`() {
        assertEquals(2, QuickStartWidget.QUICK_START_PLAYER_COUNT)
    }

    @Test
    fun `QUICK_START_PLAYER_COUNT is a valid player count`() {
        assertTrue(QuickStartWidget.QUICK_START_PLAYER_COUNT in 2..4)
    }
}

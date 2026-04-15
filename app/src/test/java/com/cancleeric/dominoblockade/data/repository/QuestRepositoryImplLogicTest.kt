package com.cancleeric.dominoblockade.data.repository

import org.junit.Assert.assertEquals
import org.junit.Test

class QuestRepositoryImplLogicTest {

    @Test
    fun `questLevelFromXp handles boundaries`() {
        assertEquals(1, questLevelFromXp(0))
        assertEquals(1, questLevelFromXp(99))
        assertEquals(2, questLevelFromXp(100))
        assertEquals(3, questLevelFromXp(200))
    }

    @Test
    fun `questLevelFromXp clamps negative xp to level one`() {
        assertEquals(1, questLevelFromXp(-50))
    }

    @Test
    fun `questProgressIncrementForTaskId increments play quests`() {
        assertEquals(1, questProgressIncrementForTaskId("short_play_10", isWin = false, isBlocked = false))
    }

    @Test
    fun `questProgressIncrementForTaskId increments win quests only on wins`() {
        assertEquals(1, questProgressIncrementForTaskId("daily_win_1", isWin = true, isBlocked = false))
        assertEquals(0, questProgressIncrementForTaskId("daily_win_1", isWin = false, isBlocked = false))
    }

    @Test
    fun `questProgressIncrementForTaskId increments blocked quests only when blocked`() {
        assertEquals(1, questProgressIncrementForTaskId("long_blocked_10", isWin = false, isBlocked = true))
        assertEquals(0, questProgressIncrementForTaskId("long_blocked_10", isWin = false, isBlocked = false))
    }
}

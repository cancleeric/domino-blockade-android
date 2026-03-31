package com.cancleeric.dominoblockade.data.remote.firestore

import org.junit.Assert.assertEquals
import org.junit.Test

class LeaderboardEntryTest {

    @Test
    fun `default values are set correctly`() {
        val entry = LeaderboardEntry()
        assertEquals("", entry.userId)
        assertEquals("", entry.displayName)
        assertEquals(0, entry.highScore)
        assertEquals(0, entry.totalWins)
        assertEquals(LeaderboardEntry.PLATFORM_ANDROID, entry.platform)
        assertEquals(0L, entry.lastUpdated)
    }

    @Test
    fun `platform constants have correct values`() {
        assertEquals("android", LeaderboardEntry.PLATFORM_ANDROID)
        assertEquals("ios", LeaderboardEntry.PLATFORM_IOS)
    }

    @Test
    fun `field name constants have correct values`() {
        assertEquals("displayName", LeaderboardEntry.FIELD_DISPLAY_NAME)
        assertEquals("highScore", LeaderboardEntry.FIELD_HIGH_SCORE)
        assertEquals("totalWins", LeaderboardEntry.FIELD_TOTAL_WINS)
        assertEquals("platform", LeaderboardEntry.FIELD_PLATFORM)
        assertEquals("lastUpdated", LeaderboardEntry.FIELD_LAST_UPDATED)
    }

    @Test
    fun `copy creates a new entry with updated fields`() {
        val original = LeaderboardEntry(
            userId = "user1",
            displayName = "Alice",
            highScore = 100,
            totalWins = 5,
            platform = LeaderboardEntry.PLATFORM_ANDROID,
            lastUpdated = 1000L
        )
        val updated = original.copy(highScore = 200, totalWins = 6)
        assertEquals("user1", updated.userId)
        assertEquals("Alice", updated.displayName)
        assertEquals(200, updated.highScore)
        assertEquals(6, updated.totalWins)
        assertEquals(LeaderboardEntry.PLATFORM_ANDROID, updated.platform)
        assertEquals(1000L, updated.lastUpdated)
    }
}

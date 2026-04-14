package com.cancleeric.dominoblockade.data.remote.firestore

import com.cancleeric.dominoblockade.domain.model.LeaderboardEntry
import org.junit.Assert.assertEquals
import org.junit.Test

class LeaderboardEntryTest {

    @Test
    fun `default values are set correctly`() {
        val entry = LeaderboardEntry()
        assertEquals("", entry.userId)
        assertEquals("", entry.displayName)
        assertEquals(LeaderboardEntry.DEFAULT_ELO, entry.elo)
        assertEquals(0, entry.wins)
        assertEquals(0, entry.losses)
        assertEquals("", entry.season)
    }

    @Test
    fun `default elo constant has correct value`() {
        assertEquals(1000, LeaderboardEntry.DEFAULT_ELO)
    }

    @Test
    fun `field name constants have correct values`() {
        assertEquals("displayName", LeaderboardEntry.FIELD_DISPLAY_NAME)
        assertEquals("elo", LeaderboardEntry.FIELD_ELO)
        assertEquals("wins", LeaderboardEntry.FIELD_WINS)
        assertEquals("losses", LeaderboardEntry.FIELD_LOSSES)
        assertEquals("season", LeaderboardEntry.FIELD_SEASON)
    }

    @Test
    fun `copy creates a new entry with updated fields`() {
        val original = LeaderboardEntry(
            userId = "user1",
            displayName = "Alice",
            elo = 1200,
            wins = 5,
            losses = 2,
            season = "2026-04"
        )
        val updated = original.copy(elo = 1212, wins = 6, losses = 2)
        assertEquals("user1", updated.userId)
        assertEquals("Alice", updated.displayName)
        assertEquals(1212, updated.elo)
        assertEquals(6, updated.wins)
        assertEquals(2, updated.losses)
        assertEquals("2026-04", updated.season)
    }
}

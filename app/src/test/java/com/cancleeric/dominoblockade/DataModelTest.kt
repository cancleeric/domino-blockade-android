package com.cancleeric.dominoblockade.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LeaderboardEntryTest {

    @Test
    fun `default platform is android`() {
        val entry = LeaderboardEntry(userId = "u1", displayName = "Test", highScore = 10)
        assertEquals("android", entry.platform)
    }

    @Test
    fun `LeaderboardEntry equality works correctly`() {
        val entry1 = LeaderboardEntry(
            userId = "u1",
            displayName = "Alice",
            highScore = 100,
            totalWins = 5,
            platform = "android",
            lastUpdated = 12345L
        )
        val entry2 = entry1.copy()
        assertEquals(entry1, entry2)
    }

    @Test
    fun `iOS platform entry has correct platform field`() {
        val entry = LeaderboardEntry(userId = "u2", displayName = "Bob", platform = "ios")
        assertEquals("ios", entry.platform)
        assertFalse(entry.platform == "android")
    }
}

class UserTest {

    @Test
    fun `anonymous user has isAnonymous true`() {
        val user = User(uid = "anon123", displayName = null, email = null, isAnonymous = true)
        assertTrue(user.isAnonymous)
    }

    @Test
    fun `google user has isAnonymous false`() {
        val user = User(uid = "google123", displayName = "Eric", email = "eric@example.com", isAnonymous = false)
        assertFalse(user.isAnonymous)
        assertEquals("Eric", user.displayName)
    }
}

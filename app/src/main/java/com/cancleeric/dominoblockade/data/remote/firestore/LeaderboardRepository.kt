package com.cancleeric.dominoblockade.data.remote.firestore

import com.cancleeric.dominoblockade.data.model.LeaderboardEntry
import kotlinx.coroutines.flow.Flow

/**
 * Contract for Firestore leaderboard operations.
 *
 * Firestore path: leaderboard/domino_blockade/{userId}
 * This structure is shared with the iOS LifeSnap project.
 */
interface LeaderboardRepository {
    /** Returns a real-time stream of the top [limit] players, ordered by highScore descending. */
    fun getTopPlayers(limit: Int = 20): Flow<List<LeaderboardEntry>>

    /** Submits or updates a player's score. Creates the document if it doesn't exist. */
    suspend fun submitScore(entry: LeaderboardEntry)

    /** Returns a real-time stream of the given player's rank (1-based), or null if not ranked. */
    fun getPlayerRank(userId: String): Flow<Int?>
}

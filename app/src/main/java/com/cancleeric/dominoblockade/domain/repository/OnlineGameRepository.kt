package com.cancleeric.dominoblockade.domain.repository

import com.cancleeric.dominoblockade.domain.model.GameState
import com.cancleeric.dominoblockade.domain.model.OnlineRoom
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing online multiplayer rooms via Firebase Realtime Database.
 */
interface OnlineGameRepository {

    /**
     * Creates a new room with [hostId] and [hostName]. Returns the generated room code.
     */
    suspend fun createRoom(hostId: String, hostName: String): String

    /**
     * Joins an existing room identified by [roomId]. Returns true on success.
     */
    suspend fun joinRoom(roomId: String, guestId: String, guestName: String): Boolean

    /** Observes real-time updates of the room identified by [roomId]. */
    fun observeRoom(roomId: String): Flow<OnlineRoom>

    /** Writes [gameState] to the room so both players see the same board. */
    suspend fun updateGameState(roomId: String, gameState: GameState)

    /** Marks the room as finished when a player leaves. */
    suspend fun leaveRoom(roomId: String)

    /**
     * Registers a Firebase onDisconnect handler so that [playerId] is written to
     * `disconnectedPlayerId` automatically if the player's connection drops. Also clears any
     * stale disconnect flag left from a previous session.
     */
    suspend fun registerPresence(roomId: String, playerId: String)
}

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
     * Registers a Firebase `onDisconnect()` handler that automatically writes a disconnect
     * timestamp to the room when the local player's connection is lost.
     *
     * @param roomId The room identifier.
     * @param isHost True if the local player is the host, false if they are the guest.
     */
    suspend fun registerDisconnectHandler(roomId: String, isHost: Boolean)

    /**
     * Clears the local player's disconnect timestamp in the room, indicating they are connected.
     * Call this on setup / reconnect to signal presence.
     *
     * @param roomId The room identifier.
     * @param isHost True if the local player is the host, false if they are the guest.
     */
    suspend fun markPlayerConnected(roomId: String, isHost: Boolean)
}

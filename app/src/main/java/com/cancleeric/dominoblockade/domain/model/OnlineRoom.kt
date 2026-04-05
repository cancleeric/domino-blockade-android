package com.cancleeric.dominoblockade.domain.model

enum class OnlineRoomStatus { WAITING, PLAYING, FINISHED }

/**
 * Represents an online multiplayer room.
 *
 * @property roomId Unique identifier / join code shared between players.
 * @property hostId Firebase UID (or generated ID) of the room creator.
 * @property hostName Display name of the host player.
 * @property guestId Firebase UID of the joining player, or null while waiting.
 * @property guestName Display name of the guest player, or null while waiting.
 * @property status Current lifecycle status of the room.
 * @property gameState Current game state, or null before the game has started.
 * @property hostDisconnectedAt Server timestamp (ms) when the host disconnected, or null if connected.
 * @property guestDisconnectedAt Server timestamp (ms) when the guest disconnected, or null if connected.
 */
data class OnlineRoom(
    val roomId: String,
    val hostId: String,
    val hostName: String,
    val guestId: String? = null,
    val guestName: String? = null,
    val status: OnlineRoomStatus = OnlineRoomStatus.WAITING,
    val gameState: GameState? = null,
    val hostDisconnectedAt: Long? = null,
    val guestDisconnectedAt: Long? = null
)


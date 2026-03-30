package com.cancleeric.dominoblockade.domain.model

enum class RoomStatus {
    WAITING,
    READY,
    IN_GAME,
    CLOSED
}

data class OnlineRoom(
    val id: String,
    val hostId: String,
    val hostName: String,
    val guestId: String? = null,
    val guestName: String? = null,
    val status: RoomStatus = RoomStatus.WAITING,
    val createdAt: Long = System.currentTimeMillis(),
    val maxWaitSeconds: Int = 60
) {
    val isFull: Boolean get() = guestId != null
}

package com.cancleeric.dominoblockade.domain.model

enum class FriendRequestStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}

data class FriendRequest(
    val id: String,
    val fromUid: String,
    val fromUsername: String,
    val toUid: String,
    val status: FriendRequestStatus,
    val createdAt: Long
)

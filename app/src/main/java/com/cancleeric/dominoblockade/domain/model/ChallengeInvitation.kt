package com.cancleeric.dominoblockade.domain.model

enum class ChallengeStatus {
    PENDING,
    ACCEPTED,
    DECLINED
}

data class ChallengeInvitation(
    val id: String,
    val challengerUid: String,
    val challengerName: String,
    val opponentUid: String,
    val gameMode: String,
    val roomId: String,
    val status: ChallengeStatus,
    val createdAt: Long
)

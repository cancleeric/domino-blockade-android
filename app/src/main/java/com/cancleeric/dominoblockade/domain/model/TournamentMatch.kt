package com.cancleeric.dominoblockade.domain.model

data class TournamentMatch(
    val matchId: String,
    val player1: TournamentPlayer?,
    val player2: TournamentPlayer?,
    val winnerId: String? = null,
    val roundNumber: Int,
    val matchIndex: Int
)

package com.cancleeric.dominoblockade.domain.model

data class TournamentPlayer(
    val playerId: String,
    val playerName: String,
    val score: Int = 0,
    val isAi: Boolean = false
)

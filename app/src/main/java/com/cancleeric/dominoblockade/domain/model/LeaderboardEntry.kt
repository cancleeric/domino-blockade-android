package com.cancleeric.dominoblockade.domain.model

data class LeaderboardEntry(
    val userId: String = "",
    val displayName: String = "",
    val elo: Int = DEFAULT_ELO,
    val wins: Int = 0,
    val losses: Int = 0,
    val season: String = ""
) {
    companion object {
        const val DEFAULT_ELO = 1000
        const val FIELD_DISPLAY_NAME = "displayName"
        const val FIELD_ELO = "elo"
        const val FIELD_WINS = "wins"
        const val FIELD_LOSSES = "losses"
        const val FIELD_SEASON = "season"
    }
}

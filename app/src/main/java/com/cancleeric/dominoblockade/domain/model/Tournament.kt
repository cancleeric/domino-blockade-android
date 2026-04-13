package com.cancleeric.dominoblockade.domain.model

data class Tournament(
    val id: String,
    val playerCount: Int,
    val status: TournamentStatus,
    val rounds: List<List<TournamentMatch>>,
    val createdAt: Long = System.currentTimeMillis()
) {
    val champion: TournamentPlayer?
        get() {
            val lastRound = rounds.lastOrNull() ?: return null
            val lastMatch = lastRound.lastOrNull() ?: return null
            val winnerId = lastMatch.winnerId ?: return null
            return when {
                lastMatch.player1?.playerId == winnerId -> lastMatch.player1
                lastMatch.player2?.playerId == winnerId -> lastMatch.player2
                else -> null
            }
        }

    val currentRoundIndex: Int
        get() = rounds.indexOfFirst { round -> round.any { it.winnerId == null } }
}

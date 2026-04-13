package com.cancleeric.dominoblockade.domain.usecase

import com.cancleeric.dominoblockade.domain.model.Tournament
import com.cancleeric.dominoblockade.domain.model.TournamentMatch
import com.cancleeric.dominoblockade.domain.model.TournamentPlayer
import com.cancleeric.dominoblockade.domain.model.TournamentStatus
import com.cancleeric.dominoblockade.domain.repository.TournamentRepository
import java.util.UUID
import javax.inject.Inject

private const val PLAYERS_PER_MATCH = 2

class CreateTournamentUseCase @Inject constructor(
    private val repository: TournamentRepository
) {
    suspend operator fun invoke(playerCount: Int, playerNames: List<String>): Tournament {
        val players = playerNames.map { name ->
            TournamentPlayer(playerId = UUID.randomUUID().toString(), playerName = name)
        }
        val rounds = buildRounds(players, playerCount)
        val tournament = Tournament(
            id = UUID.randomUUID().toString(),
            playerCount = playerCount,
            status = TournamentStatus.IN_PROGRESS,
            rounds = rounds
        )
        repository.createTournament(tournament)
        return tournament
    }

    private fun buildRounds(players: List<TournamentPlayer>, playerCount: Int): List<List<TournamentMatch>> {
        val rounds = mutableListOf<List<TournamentMatch>>()
        val firstRound = buildFirstRound(players)
        rounds.add(firstRound)
        var matchesInRound = firstRound.size / PLAYERS_PER_MATCH
        var roundNumber = 1
        while (matchesInRound >= 1) {
            rounds.add(buildEmptyRound(roundNumber, matchesInRound))
            roundNumber++
            matchesInRound /= PLAYERS_PER_MATCH
        }
        return rounds
    }

    private fun buildFirstRound(players: List<TournamentPlayer>): List<TournamentMatch> {
        return players.chunked(PLAYERS_PER_MATCH).mapIndexed { index, pair ->
            TournamentMatch(
                matchId = UUID.randomUUID().toString(),
                player1 = pair.getOrNull(0),
                player2 = pair.getOrNull(1),
                roundNumber = 0,
                matchIndex = index
            )
        }
    }

    private fun buildEmptyRound(roundNumber: Int, matchCount: Int): List<TournamentMatch> {
        return (0 until matchCount).map { index ->
            TournamentMatch(
                matchId = UUID.randomUUID().toString(),
                player1 = null,
                player2 = null,
                roundNumber = roundNumber,
                matchIndex = index
            )
        }
    }
}

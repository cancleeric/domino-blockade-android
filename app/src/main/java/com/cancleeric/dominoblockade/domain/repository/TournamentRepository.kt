package com.cancleeric.dominoblockade.domain.repository

import com.cancleeric.dominoblockade.domain.model.Tournament
import kotlinx.coroutines.flow.Flow

interface TournamentRepository {
    fun getActiveTournament(): Flow<Tournament?>
    suspend fun createTournament(tournament: Tournament)
    suspend fun updateMatch(tournamentId: String, roundIndex: Int, matchIndex: Int, winnerId: String)
    suspend fun deleteTournament(tournamentId: String)
}

package com.cancleeric.dominoblockade.domain.usecase

import com.cancleeric.dominoblockade.domain.repository.TournamentRepository
import javax.inject.Inject

class AdvanceTournamentUseCase @Inject constructor(
    private val repository: TournamentRepository
) {
    suspend operator fun invoke(tournamentId: String, roundIndex: Int, matchIndex: Int, winnerId: String) {
        repository.updateMatch(tournamentId, roundIndex, matchIndex, winnerId)
    }
}

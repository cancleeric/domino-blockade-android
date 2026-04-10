package com.cancleeric.dominoblockade.domain.usecase

import com.cancleeric.dominoblockade.domain.model.Tournament
import com.cancleeric.dominoblockade.domain.repository.TournamentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class CreateTournamentUseCaseTest {

    private class FakeTournamentRepository : TournamentRepository {
        var savedTournament: Tournament? = null
        override fun getActiveTournament(): Flow<Tournament?> = emptyFlow()
        override suspend fun createTournament(tournament: Tournament) { savedTournament = tournament }
        override suspend fun updateMatch(tournamentId: String, roundIndex: Int, matchIndex: Int, winnerId: String) {}
        override suspend fun deleteTournament(tournamentId: String) {}
    }

    private val repository = FakeTournamentRepository()
    private val useCase = CreateTournamentUseCase(repository)

    @Test
    fun `8-player tournament creates 3 rounds`() = runTest {
        val names = List(8) { "Player $it" }
        val tournament = useCase(8, names)
        assertEquals(3, tournament.rounds.size)
    }

    @Test
    fun `16-player tournament creates 4 rounds`() = runTest {
        val names = List(16) { "Player $it" }
        val tournament = useCase(16, names)
        assertEquals(4, tournament.rounds.size)
    }

    @Test
    fun `8-player tournament first round has 4 matches`() = runTest {
        val names = List(8) { "Player $it" }
        val tournament = useCase(8, names)
        assertEquals(4, tournament.rounds[0].size)
    }

    @Test
    fun `16-player tournament first round has 8 matches`() = runTest {
        val names = List(16) { "Player $it" }
        val tournament = useCase(16, names)
        assertEquals(8, tournament.rounds[0].size)
    }

    @Test
    fun `first round players are assigned correctly`() = runTest {
        val names = listOf("Alice", "Bob", "Charlie", "Dave", "Eve", "Frank", "Grace", "Heidi")
        val tournament = useCase(8, names)
        assertEquals("Alice", tournament.rounds[0][0].player1?.playerName)
        assertEquals("Bob", tournament.rounds[0][0].player2?.playerName)
    }

    @Test
    fun `subsequent rounds start with null players`() = runTest {
        val names = List(8) { "Player $it" }
        val tournament = useCase(8, names)
        assertNull(tournament.rounds[1][0].player1)
        assertNull(tournament.rounds[1][0].player2)
    }

    @Test
    fun `tournament is saved to repository`() = runTest {
        val names = List(8) { "Player $it" }
        useCase(8, names)
        assertNotNull(repository.savedTournament)
    }
}

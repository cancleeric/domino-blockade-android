package com.cancleeric.dominoblockade.presentation.tournament

import com.cancleeric.dominoblockade.domain.model.Tournament
import com.cancleeric.dominoblockade.domain.repository.TournamentRepository
import com.cancleeric.dominoblockade.domain.usecase.AdvanceTournamentUseCase
import com.cancleeric.dominoblockade.domain.usecase.CreateTournamentUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TournamentViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeTournamentRepository : TournamentRepository {
        val flow = MutableStateFlow<Tournament?>(null)
        var savedTournament: Tournament? = null

        override fun getActiveTournament(): Flow<Tournament?> = flow
        override suspend fun createTournament(tournament: Tournament) {
            savedTournament = tournament
            flow.value = tournament
        }
        override suspend fun updateMatch(tournamentId: String, roundIndex: Int, matchIndex: Int, winnerId: String) {}
        override suspend fun deleteTournament(tournamentId: String) { flow.value = null }
    }

    private lateinit var repository: FakeTournamentRepository
    private lateinit var viewModel: TournamentViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeTournamentRepository()
        viewModel = TournamentViewModel(
            createTournamentUseCase = CreateTournamentUseCase(repository),
            advanceTournamentUseCase = AdvanceTournamentUseCase(repository),
            tournamentRepository = repository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Setup`() {
        assertTrue(viewModel.uiState.value is TournamentUiState.Setup)
    }

    @Test
    fun `createTournament transitions to BracketView`() = runTest {
        val names = List(8) { "Player $it" }
        viewModel.createTournament(8, names)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value is TournamentUiState.BracketView)
    }

    @Test
    fun `createTournament sets tournament state`() = runTest {
        val names = List(8) { "Player $it" }
        viewModel.createTournament(8, names)
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value as? TournamentUiState.BracketView
        assertEquals(8, state?.tournament?.playerCount)
    }

    @Test
    fun `deleteTournament resets to Setup state`() = runTest {
        val names = List(8) { "Player $it" }
        viewModel.createTournament(8, names)
        testDispatcher.scheduler.advanceUntilIdle()
        val tournament = viewModel.tournament.value
        if (tournament != null) {
            viewModel.deleteTournament(tournament.id)
            testDispatcher.scheduler.advanceUntilIdle()
        }
        assertTrue(viewModel.uiState.value is TournamentUiState.Setup)
    }
}

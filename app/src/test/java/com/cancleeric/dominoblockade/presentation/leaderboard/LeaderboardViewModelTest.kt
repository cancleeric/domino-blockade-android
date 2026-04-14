package com.cancleeric.dominoblockade.presentation.leaderboard

import com.cancleeric.dominoblockade.data.remote.auth.AuthService
import com.cancleeric.dominoblockade.data.remote.auth.User
import com.cancleeric.dominoblockade.domain.model.LeaderboardEntry
import com.cancleeric.dominoblockade.domain.repository.LeaderboardSegment
import com.cancleeric.dominoblockade.domain.repository.LeaderboardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LeaderboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeLeaderboardRepository : LeaderboardRepository {
        private val _entries = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
        private val _playerRank = MutableStateFlow<Int?>(null)

        fun setEntries(entries: List<LeaderboardEntry>) { _entries.value = entries }
        fun setPlayerRank(rank: Int?) { _playerRank.value = rank }

        override fun getTopPlayers(
            limit: Int,
            segment: LeaderboardSegment,
            currentUserId: String?
        ): Flow<List<LeaderboardEntry>> = _entries
        override fun getPlayerRank(
            userId: String,
            segment: LeaderboardSegment,
            currentUserId: String?
        ): Flow<Int?> = _playerRank
        override suspend fun ensurePlayerEntry(userId: String, displayName: String) = Unit
        override suspend fun updateRankedMatchResult(
            winnerId: String,
            winnerDisplayName: String,
            loserId: String,
            loserDisplayName: String
        ) = Unit
        override suspend fun resetSeasonIfNeeded() = Unit
    }

    private class FakeAuthService : AuthService {
        private val _currentUser = MutableStateFlow<User?>(null)

        fun setCurrentUser(user: User?) { _currentUser.value = user }

        override fun getCurrentUser(): Flow<User?> = _currentUser
        override suspend fun signInAnonymously(): User {
            val user = User(uid = "anon-uid", displayName = null, isAnonymous = true)
            _currentUser.value = user
            return user
        }
        override suspend fun signInWithGoogle(idToken: String): User {
            val user = User(uid = "google-uid", displayName = "Test User", isAnonymous = false)
            _currentUser.value = user
            return user
        }
        override suspend fun signOut() { _currentUser.value = null }
    }

    private lateinit var fakeRepository: FakeLeaderboardRepository
    private lateinit var fakeAuthService: FakeAuthService

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeLeaderboardRepository()
        fakeAuthService = FakeAuthService()
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial uiState has isLoading true and no entries`() = runTest(testDispatcher) {
        val viewModel = LeaderboardViewModel(fakeRepository, fakeAuthService)
        val state = viewModel.uiState.first()
        assertTrue(state.isLoading)
        assertTrue(state.entries.isEmpty())
        assertNull(state.currentUser)
        assertNull(state.playerRank)
        assertNull(state.error)
    }

    @Test
    fun `entries are populated after repository emits`() = runTest(testDispatcher) {
        val entries = listOf(
            LeaderboardEntry(userId = "u1", displayName = "Alice", elo = 1200, wins = 5, losses = 1),
            LeaderboardEntry(userId = "u2", displayName = "Bob", elo = 1100, wins = 3, losses = 2)
        )
        fakeRepository.setEntries(entries)
        val viewModel = LeaderboardViewModel(fakeRepository, fakeAuthService)
        advanceUntilIdle()
        val state = viewModel.uiState.first()
        assertFalse(state.isLoading)
        assertEquals(2, state.entries.size)
        assertEquals("Alice", state.entries[0].displayName)
    }

    @Test
    fun `currentUser is updated when auth emits`() = runTest(testDispatcher) {
        val viewModel = LeaderboardViewModel(fakeRepository, fakeAuthService)
        advanceUntilIdle()
        assertNull(viewModel.uiState.first().currentUser)

        fakeAuthService.setCurrentUser(User(uid = "uid-1", displayName = "Alice", isAnonymous = false))
        advanceUntilIdle()
        assertEquals("uid-1", viewModel.uiState.first().currentUser?.uid)
    }

    @Test
    fun `playerRank is loaded when user signs in`() = runTest(testDispatcher) {
        fakeRepository.setPlayerRank(3)
        val viewModel = LeaderboardViewModel(fakeRepository, fakeAuthService)
        advanceUntilIdle()
        assertNull(viewModel.uiState.first().playerRank)

        fakeAuthService.setCurrentUser(User(uid = "uid-1", displayName = "Alice", isAnonymous = false))
        advanceUntilIdle()
        assertEquals(3, viewModel.uiState.first().playerRank)
    }

    @Test
    fun `signInAnonymously updates currentUser`() = runTest(testDispatcher) {
        val viewModel = LeaderboardViewModel(fakeRepository, fakeAuthService)
        advanceUntilIdle()
        viewModel.signInAnonymously()
        advanceUntilIdle()
        val user = viewModel.uiState.first().currentUser
        assertEquals("anon-uid", user?.uid)
        assertTrue(user?.isAnonymous == true)
    }

    @Test
    fun `signOut clears currentUser`() = runTest(testDispatcher) {
        fakeAuthService.setCurrentUser(User(uid = "uid-1", displayName = "Alice", isAnonymous = false))
        val viewModel = LeaderboardViewModel(fakeRepository, fakeAuthService)
        advanceUntilIdle()
        viewModel.signOut()
        advanceUntilIdle()
        assertNull(viewModel.uiState.first().currentUser)
    }

    @Test
    fun `clearError removes error from state`() = runTest(testDispatcher) {
        val viewModel = LeaderboardViewModel(fakeRepository, fakeAuthService)
        advanceUntilIdle()
        viewModel.clearError()
        assertNull(viewModel.uiState.first().error)
    }

    @Test
    fun `entries update when repository emits new values`() = runTest(testDispatcher) {
        val viewModel = LeaderboardViewModel(fakeRepository, fakeAuthService)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.first().entries.isEmpty())

        fakeRepository.setEntries(
            listOf(LeaderboardEntry(userId = "u1", displayName = "Charlie", elo = 1050, wins = 2, losses = 1))
        )
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.first().entries.size)
        assertEquals("Charlie", viewModel.uiState.first().entries[0].displayName)
    }

    @Test
    fun `selectSegment updates selected segment`() = runTest(testDispatcher) {
        val viewModel = LeaderboardViewModel(fakeRepository, fakeAuthService)
        advanceUntilIdle()
        viewModel.selectSegment(LeaderboardSegment.ALL_TIME)
        advanceUntilIdle()
        assertEquals(LeaderboardSegment.ALL_TIME, viewModel.uiState.first().selectedSegment)
    }
}

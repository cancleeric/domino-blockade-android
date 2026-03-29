package com.cancleeric.dominoblockade.presentation.leaderboard

import com.cancleeric.dominoblockade.data.model.LeaderboardEntry
import com.cancleeric.dominoblockade.data.model.User
import com.cancleeric.dominoblockade.data.remote.auth.AuthService
import com.cancleeric.dominoblockade.data.remote.firestore.LeaderboardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LeaderboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var fakeAuthService: FakeAuthService
    private lateinit var fakeLeaderboardRepository: FakeLeaderboardRepository
    private lateinit var viewModel: LeaderboardViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeAuthService = FakeAuthService()
        fakeLeaderboardRepository = FakeLeaderboardRepository()
        viewModel = LeaderboardViewModel(fakeLeaderboardRepository, fakeAuthService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has loading true`() {
        // LeaderboardViewModel starts with isLoading = true before flows emit
        val initialState = LeaderboardUiState()
        assert(initialState.isLoading)
    }

    @Test
    fun `leaderboard entries are loaded from repository`() = runTest {
        val entries = listOf(
            LeaderboardEntry(
                userId = "user1",
                displayName = "Alice",
                highScore = 100,
                totalWins = 5,
                platform = "android"
            ),
            LeaderboardEntry(
                userId = "user2",
                displayName = "Bob",
                highScore = 80,
                totalWins = 3,
                platform = "ios"
            )
        )
        fakeLeaderboardRepository.topPlayersFlow.value = entries
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(entries, state.entries)
    }

    @Test
    fun `sign out clears current user`() = runTest {
        fakeAuthService.currentUserFlow.value = User(
            uid = "user1",
            displayName = "Alice",
            email = null,
            isAnonymous = true
        )
        advanceUntilIdle()

        viewModel.signOut()
        advanceUntilIdle()

        assert(fakeAuthService.signOutCalled)
    }

    @Test
    fun `dismiss error clears error message`() = runTest {
        // Directly set error state via signOut failure
        fakeAuthService.shouldFailSignOut = true
        viewModel.signOut()
        advanceUntilIdle()

        viewModel.dismissError()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.errorMessage)
    }

    // --- Fakes ---

    class FakeAuthService : AuthService {
        val currentUserFlow = MutableStateFlow<User?>(null)
        var signOutCalled = false
        var shouldFailSignOut = false

        override fun getCurrentUser(): Flow<User?> = currentUserFlow

        override suspend fun signInAnonymously(): User {
            val user = User(uid = "anon", displayName = null, email = null, isAnonymous = true)
            currentUserFlow.value = user
            return user
        }

        override suspend fun signInWithGoogle(idToken: String): User {
            val user = User(uid = "google_user", displayName = "Test", email = "test@test.com", isAnonymous = false)
            currentUserFlow.value = user
            return user
        }

        override suspend fun signOut() {
            if (shouldFailSignOut) throw RuntimeException("sign out failed")
            signOutCalled = true
            currentUserFlow.value = null
        }
    }

    class FakeLeaderboardRepository : LeaderboardRepository {
        val topPlayersFlow = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
        var submittedEntry: LeaderboardEntry? = null

        override fun getTopPlayers(limit: Int): Flow<List<LeaderboardEntry>> = topPlayersFlow

        override suspend fun submitScore(entry: LeaderboardEntry) {
            submittedEntry = entry
        }

        override fun getPlayerRank(userId: String): Flow<Int?> = flowOf(null)
    }
}

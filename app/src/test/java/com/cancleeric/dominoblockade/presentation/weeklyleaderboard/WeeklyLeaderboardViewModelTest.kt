package com.cancleeric.dominoblockade.presentation.weeklyleaderboard

import com.cancleeric.dominoblockade.data.remote.auth.AuthService
import com.cancleeric.dominoblockade.data.remote.auth.User
import com.cancleeric.dominoblockade.domain.model.WeeklyLeaderboardEntry
import com.cancleeric.dominoblockade.domain.repository.WeeklyLeaderboardRepository
import com.cancleeric.dominoblockade.presentation.notification.RankChangeNotifier
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
class WeeklyLeaderboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeWeeklyLeaderboardRepository : WeeklyLeaderboardRepository {
        private val _entries = MutableStateFlow<List<WeeklyLeaderboardEntry>>(emptyList())
        private val _playerRank = MutableStateFlow<Int?>(null)
        private val _playerEntry = MutableStateFlow<WeeklyLeaderboardEntry?>(null)

        fun setEntries(entries: List<WeeklyLeaderboardEntry>) { _entries.value = entries }
        fun setPlayerRank(rank: Int?) { _playerRank.value = rank }
        fun setPlayerEntry(entry: WeeklyLeaderboardEntry?) { _playerEntry.value = entry }

        override fun getCurrentWeekId(): String = "2026-W16"
        override fun getMillisUntilWeekReset(): Long = 100_000L
        override fun getTopPlayers(weekId: String, limit: Int): Flow<List<WeeklyLeaderboardEntry>> = _entries
        override fun getPlayerEntry(weekId: String, userId: String): Flow<WeeklyLeaderboardEntry?> = _playerEntry
        override fun getPlayerRank(weekId: String, userId: String): Flow<Int?> = _playerRank
        override suspend fun ensurePlayerEntry(weekId: String, userId: String, displayName: String) = Unit
        override suspend fun updateMatchResult(
            weekId: String,
            winnerId: String,
            winnerDisplayName: String,
            loserId: String,
            loserDisplayName: String
        ) = Unit
        override suspend fun grantBadgeToTopFinishers(weekId: String) = Unit
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

    private class FakeRankChangeNotifier : RankChangeNotifier {
        val events = mutableListOf<Pair<Int, Int?>>()
        override fun notifyRankChange(newRank: Int, previousRank: Int?) {
            events.add(newRank to previousRank)
        }
    }

    private lateinit var fakeRepository: FakeWeeklyLeaderboardRepository
    private lateinit var fakeAuthService: FakeAuthService
    private lateinit var fakeNotifier: FakeRankChangeNotifier

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeWeeklyLeaderboardRepository()
        fakeAuthService = FakeAuthService()
        fakeNotifier = FakeRankChangeNotifier()
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has correct weekId and isLoading true`() = runTest(testDispatcher) {
        val viewModel = WeeklyLeaderboardViewModel(fakeRepository, fakeAuthService, fakeNotifier)
        val state = viewModel.uiState.first()
        assertEquals("2026-W16", state.weekId)
        assertTrue(state.isLoading)
        assertTrue(state.entries.isEmpty())
        assertNull(state.currentUser)
        assertNull(state.playerRank)
        assertNull(state.error)
    }

    @Test
    fun `entries are populated after repository emits`() = runTest(testDispatcher) {
        val entries = listOf(
            WeeklyLeaderboardEntry(weekId = "2026-W16", userId = "u1", displayName = "Alice", score = 100),
            WeeklyLeaderboardEntry(weekId = "2026-W16", userId = "u2", displayName = "Bob", score = 80)
        )
        fakeRepository.setEntries(entries)
        val viewModel = WeeklyLeaderboardViewModel(fakeRepository, fakeAuthService, fakeNotifier)
        advanceUntilIdle()
        val state = viewModel.uiState.first()
        assertFalse(state.isLoading)
        assertEquals(2, state.entries.size)
        assertEquals("Alice", state.entries[0].displayName)
    }

    @Test
    fun `currentUser is updated when auth emits`() = runTest(testDispatcher) {
        val viewModel = WeeklyLeaderboardViewModel(fakeRepository, fakeAuthService, fakeNotifier)
        advanceUntilIdle()
        assertNull(viewModel.uiState.first().currentUser)

        fakeAuthService.setCurrentUser(User(uid = "uid-1", displayName = "Alice", isAnonymous = false))
        advanceUntilIdle()
        assertEquals("uid-1", viewModel.uiState.first().currentUser?.uid)
    }

    @Test
    fun `playerRank is loaded when user signs in`() = runTest(testDispatcher) {
        fakeRepository.setPlayerRank(5)
        val viewModel = WeeklyLeaderboardViewModel(fakeRepository, fakeAuthService, fakeNotifier)
        advanceUntilIdle()
        assertNull(viewModel.uiState.first().playerRank)

        fakeAuthService.setCurrentUser(User(uid = "uid-1", displayName = "Alice", isAnonymous = false))
        advanceUntilIdle()
        assertEquals(5, viewModel.uiState.first().playerRank)
    }

    @Test
    fun `playerEntry is loaded when user signs in`() = runTest(testDispatcher) {
        val entry = WeeklyLeaderboardEntry(weekId = "2026-W16", userId = "uid-1", displayName = "Alice", score = 50)
        fakeRepository.setPlayerEntry(entry)
        val viewModel = WeeklyLeaderboardViewModel(fakeRepository, fakeAuthService, fakeNotifier)
        fakeAuthService.setCurrentUser(User(uid = "uid-1", displayName = "Alice", isAnonymous = false))
        advanceUntilIdle()
        assertEquals("Alice", viewModel.uiState.first().playerEntry?.displayName)
    }

    @Test
    fun `clearError removes error from state`() = runTest(testDispatcher) {
        val viewModel = WeeklyLeaderboardViewModel(fakeRepository, fakeAuthService, fakeNotifier)
        advanceUntilIdle()
        viewModel.clearError()
        assertNull(viewModel.uiState.first().error)
    }

    @Test
    fun `millisUntilReset is set from repository`() = runTest(testDispatcher) {
        val viewModel = WeeklyLeaderboardViewModel(fakeRepository, fakeAuthService, fakeNotifier)
        advanceUntilIdle()
        assertEquals(100_000L, viewModel.uiState.first().millisUntilReset)
    }

    @Test
    fun `rank change notification is fired when rank changes after initial load`() = runTest(testDispatcher) {
        fakeRepository.setPlayerRank(8)
        val viewModel = WeeklyLeaderboardViewModel(fakeRepository, fakeAuthService, fakeNotifier)
        fakeAuthService.setCurrentUser(User(uid = "uid-1", displayName = "Alice", isAnonymous = false))
        advanceUntilIdle()
        // First emission sets previousRank; no notification on initial load
        assertEquals(0, fakeNotifier.events.size)

        // Simulate rank change by emitting a new rank
        fakeRepository.setPlayerRank(5)
        advanceUntilIdle()
        assertTrue(fakeNotifier.events.isNotEmpty())
        assertEquals(5, fakeNotifier.events.last().first)
        assertEquals(8, fakeNotifier.events.last().second)
    }
}

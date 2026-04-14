package com.cancleeric.dominoblockade.presentation.onlinegame

import com.cancleeric.dominoblockade.domain.model.Domino
import com.cancleeric.dominoblockade.domain.model.GameState
import com.cancleeric.dominoblockade.domain.model.OnlineRoom
import com.cancleeric.dominoblockade.domain.model.OnlineRoomStatus
import com.cancleeric.dominoblockade.domain.model.Player
import com.cancleeric.dominoblockade.domain.repository.LeaderboardRepository
import com.cancleeric.dominoblockade.domain.repository.LeaderboardSegment
import com.cancleeric.dominoblockade.domain.repository.OnlineGameRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private const val GRACE_PERIOD = 30
private const val ROOM_ID = "ROOM01"
private const val HOST_ID = "player0"
private const val GUEST_ID = "player1"
private const val ONE_SECOND_MS = 1_001L
private const val GRACE_PERIOD_PLUS_BUFFER_MS = 31_000L

class OnlineGameViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val roomFlow = MutableSharedFlow<OnlineRoom>(replay = 1)

    private val fakeRepository = object : OnlineGameRepository {
        override suspend fun createRoom(hostId: String, hostName: String) = ROOM_ID
        override suspend fun joinRoom(roomId: String, guestId: String, guestName: String) = true
        override fun observeRoom(roomId: String): Flow<OnlineRoom> = roomFlow
        override suspend fun updateGameState(roomId: String, gameState: GameState) = Unit
        override suspend fun leaveRoom(roomId: String) = Unit
        override suspend fun registerPresence(roomId: String, playerId: String) = Unit
        override suspend fun joinRankedQueue(playerId: String, playerName: String) = Unit
        override fun observeRankedAssignment(playerId: String): Flow<Pair<String, Int>?> = roomFlow.map { null }
        override suspend fun leaveRankedQueue(playerId: String) = Unit
    }

    private val fakeLeaderboardRepository = object : LeaderboardRepository {
        override fun getTopPlayers(
            limit: Int,
            segment: LeaderboardSegment,
            currentUserId: String?
        ): Flow<List<com.cancleeric.dominoblockade.domain.model.LeaderboardEntry>> = roomFlow.map { emptyList() }
        override fun getPlayerRank(
            userId: String,
            segment: LeaderboardSegment,
            currentUserId: String?
        ): Flow<Int?> = roomFlow.map { null }
        override suspend fun ensurePlayerEntry(userId: String, displayName: String) = Unit
        override suspend fun updateRankedMatchResult(
            matchId: String,
            winnerId: String,
            winnerDisplayName: String,
            loserId: String,
            loserDisplayName: String
        ) = Unit
        override suspend fun resetSeasonIfNeeded() = Unit
    }

    private lateinit var viewModel: OnlineGameViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = OnlineGameViewModel(fakeRepository, fakeLeaderboardRepository, GRACE_PERIOD)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildRoom(disconnectedPlayerId: String? = null): OnlineRoom {
        val host = Player(id = HOST_ID, name = "Alice", hand = listOf(Domino(1, 2)))
        val guest = Player(id = GUEST_ID, name = "Bob", hand = listOf(Domino(3, 4)))
        val gameState = GameState(players = listOf(host, guest))
        return OnlineRoom(
            roomId = ROOM_ID,
            hostId = HOST_ID,
            hostName = "Alice",
            guestId = GUEST_ID,
            guestName = "Bob",
            status = OnlineRoomStatus.PLAYING,
            gameState = gameState,
            disconnectedPlayerId = disconnectedPlayerId
        )
    }

    @Test
    fun `countdown starts at grace period when opponent disconnects`() = runTest(testDispatcher) {
        viewModel.setup(ROOM_ID, 0, HOST_ID)
        roomFlow.emit(buildRoom(disconnectedPlayerId = GUEST_ID))
        testDispatcher.scheduler.runCurrent()

        assertEquals(GRACE_PERIOD, viewModel.uiState.value.reconnectionCountdown)
        assertEquals("Bob", viewModel.uiState.value.disconnectedOpponentName)
    }

    @Test
    fun `countdown decrements each second when opponent disconnects`() = runTest(testDispatcher) {
        viewModel.setup(ROOM_ID, 0, HOST_ID)
        roomFlow.emit(buildRoom(disconnectedPlayerId = GUEST_ID))
        testDispatcher.scheduler.runCurrent()

        advanceTimeBy(ONE_SECOND_MS)
        assertEquals(GRACE_PERIOD - 1, viewModel.uiState.value.reconnectionCountdown)

        advanceTimeBy(ONE_SECOND_MS)
        assertEquals(GRACE_PERIOD - 2, viewModel.uiState.value.reconnectionCountdown)
    }

    @Test
    fun `grace period expiry sets roomFinished correctly`() = runTest(testDispatcher) {
        viewModel.setup(ROOM_ID, 0, HOST_ID)
        roomFlow.emit(buildRoom(disconnectedPlayerId = GUEST_ID))
        testDispatcher.scheduler.runCurrent()

        advanceTimeBy(GRACE_PERIOD_PLUS_BUFFER_MS)

        assertTrue(viewModel.uiState.value.roomFinished)
        assertNull(viewModel.uiState.value.reconnectionCountdown)
    }

    @Test
    fun `countdown cancelled and cleared when opponent reconnects`() = runTest(testDispatcher) {
        viewModel.setup(ROOM_ID, 0, HOST_ID)
        roomFlow.emit(buildRoom(disconnectedPlayerId = GUEST_ID))
        testDispatcher.scheduler.runCurrent()

        advanceTimeBy(ONE_SECOND_MS * 5)
        assertEquals(GRACE_PERIOD - 5, viewModel.uiState.value.reconnectionCountdown)

        // Opponent reconnects: disconnectedPlayerId is null
        roomFlow.emit(buildRoom(disconnectedPlayerId = null))
        testDispatcher.scheduler.runCurrent()

        assertNull(viewModel.uiState.value.reconnectionCountdown)
        assertNull(viewModel.uiState.value.disconnectedOpponentName)
    }

    @Test
    fun `roomFinished set when room status is FINISHED`() = runTest(testDispatcher) {
        viewModel.setup(ROOM_ID, 0, HOST_ID)
        roomFlow.emit(
            OnlineRoom(
                roomId = ROOM_ID,
                hostId = HOST_ID,
                hostName = "Alice",
                status = OnlineRoomStatus.FINISHED
            )
        )
        testDispatcher.scheduler.runCurrent()

        assertTrue(viewModel.uiState.value.roomFinished)
    }
}

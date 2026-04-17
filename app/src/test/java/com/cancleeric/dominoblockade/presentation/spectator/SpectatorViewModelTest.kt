package com.cancleeric.dominoblockade.presentation.spectator

import com.cancleeric.dominoblockade.domain.model.Domino
import com.cancleeric.dominoblockade.domain.model.GameState
import com.cancleeric.dominoblockade.domain.model.OnlineRoom
import com.cancleeric.dominoblockade.domain.model.OnlineRoomStatus
import com.cancleeric.dominoblockade.domain.model.Player
import com.cancleeric.dominoblockade.domain.repository.OnlineGameRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
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

private const val ROOM_ID = "ROOM01"
private const val HOST_ID = "host1"
private const val GUEST_ID = "guest1"
private const val SPECTATOR_ID = "spectator1"
private const val SPECTATOR_NAME = "Watcher"

class SpectatorViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val roomFlow = MutableSharedFlow<OnlineRoom>(replay = 1)
    private var leaveAsSpectatorCalled = false
    private var lastLeaveRoomId: String? = null
    private var lastLeaveSpectatorId: String? = null

    private val fakeRepository = object : OnlineGameRepository {
        override suspend fun createRoom(hostId: String, hostName: String) = ROOM_ID
        override suspend fun joinRoom(roomId: String, guestId: String, guestName: String) = true
        override fun observeRoom(roomId: String): Flow<OnlineRoom> = roomFlow
        override suspend fun updateGameState(roomId: String, gameState: GameState) = Unit
        override suspend fun leaveRoom(roomId: String) = Unit
        override suspend fun registerPresence(roomId: String, playerId: String) = Unit
        override suspend fun joinRankedQueue(playerId: String, playerName: String) = Unit
        override fun observeRankedAssignment(playerId: String): Flow<Pair<String, Int>?> =
            roomFlow.map { null }
        override suspend fun leaveRankedQueue(playerId: String) = Unit
        override suspend fun joinAsSpectator(
            roomId: String,
            spectatorId: String,
            spectatorName: String
        ) = true
        override suspend fun leaveAsSpectator(roomId: String, spectatorId: String) {
            leaveAsSpectatorCalled = true
            lastLeaveRoomId = roomId
            lastLeaveSpectatorId = spectatorId
        }
        override suspend fun setSpectatorPermission(roomId: String, allowed: Boolean) = Unit
    }

    private lateinit var viewModel: SpectatorViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = SpectatorViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildRoom(
        spectators: Map<String, String> = emptyMap(),
        status: OnlineRoomStatus = OnlineRoomStatus.PLAYING,
        gameState: GameState? = buildGameState()
    ): OnlineRoom = OnlineRoom(
        roomId = ROOM_ID,
        hostId = HOST_ID,
        hostName = "Alice",
        guestId = GUEST_ID,
        guestName = "Bob",
        status = status,
        gameState = gameState,
        spectators = spectators
    )

    private fun buildGameState(): GameState {
        val host = Player(id = HOST_ID, name = "Alice", hand = listOf(Domino(1, 2)))
        val guest = Player(id = GUEST_ID, name = "Bob", hand = listOf(Domino(3, 4)))
        return GameState(players = listOf(host, guest))
    }

    @Test
    fun `initial state shows loading`() {
        val state = viewModel.uiState.value
        assertTrue(state.isLoading)
        assertNull(state.gameState)
        assertFalse(state.roomFinished)
    }

    @Test
    fun `setup populates game state from room`() = runTest(testDispatcher) {
        viewModel.setup(ROOM_ID, SPECTATOR_ID)
        roomFlow.emit(buildRoom())
        testDispatcher.scheduler.runCurrent()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Alice", state.hostName)
        assertEquals("Bob", state.guestName)
    }

    @Test
    fun `spectator count reflects spectators in room`() = runTest(testDispatcher) {
        val spectators = mapOf(
            SPECTATOR_ID to SPECTATOR_NAME,
            "spectator2" to "Viewer2"
        )
        viewModel.setup(ROOM_ID, SPECTATOR_ID)
        roomFlow.emit(buildRoom(spectators = spectators))
        testDispatcher.scheduler.runCurrent()

        val state = viewModel.uiState.value
        assertEquals(2, state.spectatorCount)
        assertTrue(state.spectatorNames.contains(SPECTATOR_NAME))
        assertTrue(state.spectatorNames.contains("Viewer2"))
    }

    @Test
    fun `room finished when status is FINISHED`() = runTest(testDispatcher) {
        viewModel.setup(ROOM_ID, SPECTATOR_ID)
        roomFlow.emit(buildRoom(status = OnlineRoomStatus.FINISHED, gameState = null))
        testDispatcher.scheduler.runCurrent()

        assertTrue(viewModel.uiState.value.roomFinished)
    }

    @Test
    fun `leave calls leaveAsSpectator on repository`() = runTest(testDispatcher) {
        viewModel.setup(ROOM_ID, SPECTATOR_ID)
        viewModel.leave()
        testDispatcher.scheduler.runCurrent()

        assertTrue(leaveAsSpectatorCalled)
        assertEquals(ROOM_ID, lastLeaveRoomId)
        assertEquals(SPECTATOR_ID, lastLeaveSpectatorId)
    }

    @Test
    fun `setup is idempotent for same room`() = runTest(testDispatcher) {
        viewModel.setup(ROOM_ID, SPECTATOR_ID)
        roomFlow.emit(buildRoom())
        testDispatcher.scheduler.runCurrent()

        val firstState = viewModel.uiState.value
        viewModel.setup(ROOM_ID, "other-spectator")
        testDispatcher.scheduler.runCurrent()

        assertEquals(firstState.spectatorCount, viewModel.uiState.value.spectatorCount)
    }

    @Test
    fun `game state updates in real-time`() = runTest(testDispatcher) {
        viewModel.setup(ROOM_ID, SPECTATOR_ID)
        roomFlow.emit(buildRoom())
        testDispatcher.scheduler.runCurrent()

        val updatedGameState = buildGameState().copy(currentPlayerIndex = 1)
        roomFlow.emit(buildRoom(gameState = updatedGameState))
        testDispatcher.scheduler.runCurrent()

        assertEquals(1, viewModel.uiState.value.gameState?.currentPlayerIndex)
    }

    @Test
    fun `isLoading is true when gameState is null`() = runTest(testDispatcher) {
        viewModel.setup(ROOM_ID, SPECTATOR_ID)
        roomFlow.emit(buildRoom(gameState = null))
        testDispatcher.scheduler.runCurrent()

        assertTrue(viewModel.uiState.value.isLoading)
    }
}

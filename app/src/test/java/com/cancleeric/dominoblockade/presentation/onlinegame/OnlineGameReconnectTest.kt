package com.cancleeric.dominoblockade.presentation.onlinegame

import com.cancleeric.dominoblockade.domain.model.Domino
import com.cancleeric.dominoblockade.domain.model.GameState
import com.cancleeric.dominoblockade.domain.model.OnlineRoom
import com.cancleeric.dominoblockade.domain.model.OnlineRoomStatus
import com.cancleeric.dominoblockade.domain.model.Player
import com.cancleeric.dominoblockade.domain.repository.OnlineGameRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private const val GRACE_PERIOD_MS = 60_000L
private const val ONE_SECOND_MS = 1_000L

class OnlineGameReconnectTest {

    private val roomFlow = MutableSharedFlow<OnlineRoom>(replay = 1)
    private var leftRoomCount = 0
    private var connectedCalls = 0
    private var disconnectHandlerCalls = 0

    private val fakeRepository = object : OnlineGameRepository {
        override suspend fun createRoom(hostId: String, hostName: String) = "ROOM01"
        override suspend fun joinRoom(roomId: String, guestId: String, guestName: String) = true
        override fun observeRoom(roomId: String): Flow<OnlineRoom> = roomFlow
        override suspend fun updateGameState(roomId: String, gameState: GameState) = Unit
        override suspend fun leaveRoom(roomId: String) { leftRoomCount++ }
        override suspend fun registerDisconnectHandler(roomId: String, isHost: Boolean) { disconnectHandlerCalls++ }
        override suspend fun markPlayerConnected(roomId: String, isHost: Boolean) { connectedCalls++ }
    }

    private lateinit var viewModel: OnlineGameViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = OnlineGameViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildRoom(
        guestDisconnectedAt: Long? = null,
        hostDisconnectedAt: Long? = null
    ) = OnlineRoom(
        roomId = "ROOM01",
        hostId = "host1",
        hostName = "Alice",
        guestId = "guest1",
        guestName = "Bob",
        status = OnlineRoomStatus.PLAYING,
        gameState = GameState(
            players = listOf(
                Player("host1", "Alice", listOf(Domino(1, 2))),
                Player("guest1", "Bob", listOf(Domino(3, 4)))
            ),
            currentPlayerIndex = 0
        ),
        guestDisconnectedAt = guestDisconnectedAt,
        hostDisconnectedAt = hostDisconnectedAt
    )

    @Test
    fun `setup registers disconnect handler and marks player connected`() = runTest {
        viewModel.setup("ROOM01", 0)
        assertEquals(1, disconnectHandlerCalls)
        assertEquals(1, connectedCalls)
    }

    @Test
    fun `setup with same roomId does not re-observe but does re-register handlers`() = runTest {
        viewModel.setup("ROOM01", 0)
        viewModel.setup("ROOM01", 0)
        assertEquals(2, disconnectHandlerCalls)
        assertEquals(2, connectedCalls)
    }

    @Test
    fun `opponent disconnect sets opponentDisconnected true with full grace period`() = runTest {
        viewModel.setup("ROOM01", 0)
        roomFlow.emit(buildRoom(guestDisconnectedAt = System.currentTimeMillis()))
        assertTrue(viewModel.uiState.value.opponentDisconnected)
        assertEquals(60, viewModel.uiState.value.gracePeriodSeconds)
    }

    @Test
    fun `opponent reconnect clears opponentDisconnected flag`() = runTest {
        viewModel.setup("ROOM01", 0)
        roomFlow.emit(buildRoom(guestDisconnectedAt = System.currentTimeMillis()))
        assertTrue(viewModel.uiState.value.opponentDisconnected)
        roomFlow.emit(buildRoom(guestDisconnectedAt = null))
        assertFalse(viewModel.uiState.value.opponentDisconnected)
        assertEquals(0, viewModel.uiState.value.gracePeriodSeconds)
    }

    @Test
    fun `grace period countdown decrements each second`() = runTest {
        viewModel.setup("ROOM01", 0)
        roomFlow.emit(buildRoom(guestDisconnectedAt = System.currentTimeMillis()))
        assertEquals(60, viewModel.uiState.value.gracePeriodSeconds)
        advanceTimeBy(ONE_SECOND_MS)
        assertEquals(59, viewModel.uiState.value.gracePeriodSeconds)
        advanceTimeBy(ONE_SECOND_MS * 9)
        assertEquals(50, viewModel.uiState.value.gracePeriodSeconds)
    }

    @Test
    fun `grace period expiry leaves room and sets roomFinished`() = runTest {
        viewModel.setup("ROOM01", 0)
        roomFlow.emit(buildRoom(guestDisconnectedAt = System.currentTimeMillis()))
        advanceTimeBy(GRACE_PERIOD_MS)
        assertTrue(viewModel.uiState.value.roomFinished)
        assertTrue(leftRoomCount > 0)
    }

    @Test
    fun `grace period cancelled on reconnect does not finish room`() = runTest {
        viewModel.setup("ROOM01", 0)
        roomFlow.emit(buildRoom(guestDisconnectedAt = System.currentTimeMillis()))
        advanceTimeBy(ONE_SECOND_MS * 10)
        roomFlow.emit(buildRoom(guestDisconnectedAt = null))
        advanceTimeBy(GRACE_PERIOD_MS)
        assertFalse(viewModel.uiState.value.roomFinished)
        assertEquals(0, leftRoomCount)
    }

    @Test
    fun `host disconnect is detected when local player is guest`() = runTest {
        viewModel.setup("ROOM01", 1)
        roomFlow.emit(buildRoom(hostDisconnectedAt = System.currentTimeMillis()))
        assertTrue(viewModel.uiState.value.opponentDisconnected)
    }

    @Test
    fun `host own disconnect timestamp does not trigger opponent disconnected`() = runTest {
        viewModel.setup("ROOM01", 0)
        roomFlow.emit(buildRoom(hostDisconnectedAt = System.currentTimeMillis()))
        assertFalse(viewModel.uiState.value.opponentDisconnected)
    }
}

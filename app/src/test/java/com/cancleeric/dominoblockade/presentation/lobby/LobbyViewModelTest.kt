package com.cancleeric.dominoblockade.presentation.lobby

import com.cancleeric.dominoblockade.domain.model.GameState
import com.cancleeric.dominoblockade.domain.model.OnlineRoom
import com.cancleeric.dominoblockade.domain.model.OnlineRoomStatus
import com.cancleeric.dominoblockade.domain.repository.OnlineGameRepository
import com.cancleeric.dominoblockade.domain.usecase.StartGameUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class LobbyViewModelTest {

    private val roomFlow = MutableSharedFlow<OnlineRoom>(replay = 1)
    private var createdRoomId: String = "ROOM01"
    private var joinSuccess = true

    private val fakeRepository = object : OnlineGameRepository {
        override suspend fun createRoom(hostId: String, hostName: String): String = createdRoomId
        override suspend fun joinRoom(roomId: String, guestId: String, guestName: String) = joinSuccess
        override fun observeRoom(roomId: String): Flow<OnlineRoom> = roomFlow
        override suspend fun updateGameState(roomId: String, gameState: GameState) = Unit
        override suspend fun leaveRoom(roomId: String) = Unit
        override suspend fun registerPresence(roomId: String, playerId: String) = Unit
        override suspend fun joinRankedQueue(playerId: String, playerName: String) = Unit
        override fun observeRankedAssignment(playerId: String): Flow<Pair<String, Int>?> =
            MutableSharedFlow()
        override suspend fun leaveRankedQueue(playerId: String) = Unit
        override suspend fun joinAsSpectator(roomId: String, spectatorId: String, spectatorName: String) = true
        override suspend fun leaveAsSpectator(roomId: String, spectatorId: String) = Unit
        override suspend fun setSpectatorPermission(roomId: String, allowed: Boolean) = Unit
    }

    private lateinit var viewModel: LobbyViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = LobbyViewModel(fakeRepository, StartGameUseCase())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has empty playerName and roomCode`() {
        val state = viewModel.uiState.value
        assertEquals("", state.playerName)
        assertEquals("", state.roomCode)
        assertNull(state.error)
        assertNull(state.navigateToGame)
    }

    @Test
    fun `setPlayerName updates playerName in state`() {
        viewModel.setPlayerName("Alice")
        assertEquals("Alice", viewModel.uiState.value.playerName)
    }

    @Test
    fun `setRoomCode converts to uppercase`() {
        viewModel.setRoomCode("abc123")
        assertEquals("ABC123", viewModel.uiState.value.roomCode)
    }

    @Test
    fun `createRoom with empty name sets error`() = runTest {
        viewModel.createRoom()
        assertNotNull(viewModel.uiState.value.error)
    }

    @Test
    fun `joinRoom with empty name sets error`() = runTest {
        viewModel.joinRoom()
        assertNotNull(viewModel.uiState.value.error)
    }

    @Test
    fun `joinRoom with empty roomCode sets error`() = runTest {
        viewModel.setPlayerName("Bob")
        viewModel.joinRoom()
        assertNotNull(viewModel.uiState.value.error)
    }

    @Test
    fun `dismissError clears error`() = runTest {
        viewModel.createRoom()
        viewModel.dismissError()
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `resetNavigation clears navigateToGame`() = runTest {
        viewModel.resetNavigation()
        assertNull(viewModel.uiState.value.navigateToGame)
    }

    @Test
    fun `createRoom with valid name sets createdRoomId`() = runTest {
        viewModel.setPlayerName("Alice")
        viewModel.createRoom()
        assertEquals("ROOM01", viewModel.uiState.value.createdRoomId)
    }

    @Test
    fun `joinAsSpectator with empty name sets error`() = runTest {
        viewModel.setRoomCode("ROOM01")
        viewModel.joinAsSpectator()
        assertNotNull(viewModel.uiState.value.error)
        assertNull(viewModel.uiState.value.navigateToSpectator)
    }

    @Test
    fun `joinAsSpectator with empty roomCode sets error`() = runTest {
        viewModel.setPlayerName("Watcher")
        viewModel.joinAsSpectator()
        assertNotNull(viewModel.uiState.value.error)
        assertNull(viewModel.uiState.value.navigateToSpectator)
    }

    @Test
    fun `joinAsSpectator with valid data navigates to spectator`() = runTest {
        viewModel.setPlayerName("Watcher")
        viewModel.setRoomCode("ROOM01")
        viewModel.joinAsSpectator()
        assertNull(viewModel.uiState.value.error)
        assertNotNull(viewModel.uiState.value.navigateToSpectator)
        assertEquals("ROOM01", viewModel.uiState.value.navigateToSpectator?.roomId)
    }

    @Test
    fun `resetSpectatorNavigation clears navigateToSpectator`() = runTest {
        viewModel.setPlayerName("Watcher")
        viewModel.setRoomCode("ROOM01")
        viewModel.joinAsSpectator()
        viewModel.resetSpectatorNavigation()
        assertNull(viewModel.uiState.value.navigateToSpectator)
    }
}

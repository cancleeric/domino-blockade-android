package com.cancleeric.dominoblockade.presentation.replay

import app.cash.turbine.test
import com.cancleeric.dominoblockade.data.local.entity.GameMoveEntity
import com.cancleeric.dominoblockade.data.local.entity.GameReplayEntity
import com.cancleeric.dominoblockade.domain.repository.GameReplayRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class ReplayViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repository: GameReplayRepository = mockk()
    private lateinit var viewModel: ReplayViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildReplay(
        winnerName: String = "Player 1",
        isBlocked: Boolean = false,
        playerCount: Int = 2
    ) = GameReplayEntity(
        id = 1L,
        playerCount = playerCount,
        winnerName = winnerName,
        isBlocked = isBlocked,
        timestamp = 1000L
    )

    private fun buildMove(
        moveIndex: Int,
        playerName: String = "Player 1",
        moveType: String = "PLACE",
        dominoLeft: Int = 3,
        dominoRight: Int = 4,
        boardState: String = "3|4",
        boneyardSize: Int = 20
    ) = GameMoveEntity(
        id = moveIndex.toLong() + 1,
        replayId = 1L,
        moveIndex = moveIndex,
        playerIndex = 0,
        playerName = playerName,
        moveType = moveType,
        dominoLeft = dominoLeft,
        dominoRight = dominoRight,
        boardState = boardState,
        boneyardSize = boneyardSize
    )

    private fun createViewModel(): ReplayViewModel {
        return ReplayViewModel(repository)
    }

    @Test
    fun `initial state is loading`() = runTest(testDispatcher) {
        coEvery { repository.getLatestReplayWithMoves() } returns null
        viewModel = createViewModel()
        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `empty state when no replay available`() = runTest(testDispatcher) {
        coEvery { repository.getLatestReplayWithMoves() } returns null
        viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.steps.isEmpty())
    }

    @Test
    fun `loads replay steps from repository`() = runTest(testDispatcher) {
        val replay = buildReplay(winnerName = "Alice")
        val moves = listOf(
            buildMove(0, moveType = "DEAL", dominoLeft = -1, dominoRight = -1, boardState = ""),
            buildMove(1, moveType = "PLACE", dominoLeft = 3, dominoRight = 4, boardState = "3|4")
        )
        coEvery { repository.getLatestReplayWithMoves() } returns (replay to moves)
        viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.steps.size)
        assertEquals("Alice", state.winnerName)
        assertFalse(state.isBlocked)
    }

    @Test
    fun `nextStep advances currentIndex`() = runTest(testDispatcher) {
        val replay = buildReplay()
        val moves = listOf(buildMove(0, boardState = ""), buildMove(1), buildMove(2, boardState = "3|4,4|2"))
        coEvery { repository.getLatestReplayWithMoves() } returns (replay to moves)
        viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.currentIndex)
        viewModel.nextStep()
        assertEquals(1, viewModel.uiState.value.currentIndex)
        viewModel.nextStep()
        assertEquals(2, viewModel.uiState.value.currentIndex)
    }

    @Test
    fun `nextStep does not exceed last step`() = runTest(testDispatcher) {
        val replay = buildReplay()
        val moves = listOf(buildMove(0))
        coEvery { repository.getLatestReplayWithMoves() } returns (replay to moves)
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.nextStep()
        assertEquals(0, viewModel.uiState.value.currentIndex)
    }

    @Test
    fun `previousStep decrements currentIndex`() = runTest(testDispatcher) {
        val replay = buildReplay()
        val moves = listOf(buildMove(0, boardState = ""), buildMove(1))
        coEvery { repository.getLatestReplayWithMoves() } returns (replay to moves)
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.nextStep()
        assertEquals(1, viewModel.uiState.value.currentIndex)
        viewModel.previousStep()
        assertEquals(0, viewModel.uiState.value.currentIndex)
    }

    @Test
    fun `previousStep does not go below zero`() = runTest(testDispatcher) {
        val replay = buildReplay()
        val moves = listOf(buildMove(0))
        coEvery { repository.getLatestReplayWithMoves() } returns (replay to moves)
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.previousStep()
        assertEquals(0, viewModel.uiState.value.currentIndex)
    }

    @Test
    fun `goToStep clamps to valid range`() = runTest(testDispatcher) {
        val replay = buildReplay()
        val moves = listOf(buildMove(0, boardState = ""), buildMove(1), buildMove(2, boardState = "3|4,4|2"))
        coEvery { repository.getLatestReplayWithMoves() } returns (replay to moves)
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.goToStep(10)
        assertEquals(2, viewModel.uiState.value.currentIndex)
        viewModel.goToStep(-1)
        assertEquals(0, viewModel.uiState.value.currentIndex)
    }

    @Test
    fun `canGoBack and canGoForward are correct`() = runTest(testDispatcher) {
        val replay = buildReplay()
        val moves = listOf(buildMove(0, boardState = ""), buildMove(1))
        coEvery { repository.getLatestReplayWithMoves() } returns (replay to moves)
        viewModel = createViewModel()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.canGoBack)
        assertTrue(viewModel.uiState.value.canGoForward)

        viewModel.nextStep()
        assertTrue(viewModel.uiState.value.canGoBack)
        assertFalse(viewModel.uiState.value.canGoForward)
    }

    @Test
    fun `currentStep reflects current index`() = runTest(testDispatcher) {
        val replay = buildReplay()
        val moves = listOf(
            buildMove(0, playerName = "Player 1", boardState = ""),
            buildMove(1, playerName = "Player 2", boardState = "3|4")
        )
        coEvery { repository.getLatestReplayWithMoves() } returns (replay to moves)
        viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals("Player 1", viewModel.uiState.value.currentStep?.playerName)
        viewModel.nextStep()
        assertEquals("Player 2", viewModel.uiState.value.currentStep?.playerName)
    }

    @Test
    fun `blocked game sets isBlocked on state`() = runTest(testDispatcher) {
        val replay = buildReplay(winnerName = "", isBlocked = true)
        val moves = listOf(buildMove(0, boardState = ""))
        coEvery { repository.getLatestReplayWithMoves() } returns (replay to moves)
        viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isBlocked)
        assertTrue(viewModel.uiState.value.winnerName.isEmpty())
    }

    @Test
    fun `board is deserialized correctly`() = runTest(testDispatcher) {
        val replay = buildReplay()
        val moves = listOf(buildMove(0, boardState = "3|4,4|2,2|6"))
        coEvery { repository.getLatestReplayWithMoves() } returns (replay to moves)
        viewModel = createViewModel()
        advanceUntilIdle()

        val board = viewModel.uiState.value.currentStep?.board ?: emptyList()
        assertEquals(3, board.size)
        assertEquals(3, board[0].left)
        assertEquals(4, board[0].right)
        assertEquals(2, board[2].left)
        assertEquals(6, board[2].right)
    }

    @Test
    fun `uiState emits via turbine on step navigation`() = runTest(testDispatcher) {
        val replay = buildReplay()
        val moves = listOf(buildMove(0, boardState = ""), buildMove(1))
        coEvery { repository.getLatestReplayWithMoves() } returns (replay to moves)
        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // loading
            advanceUntilIdle()
            val loaded = awaitItem()
            assertFalse(loaded.isLoading)
            assertEquals(0, loaded.currentIndex)

            viewModel.nextStep()
            val stepped = awaitItem()
            assertEquals(1, stepped.currentIndex)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

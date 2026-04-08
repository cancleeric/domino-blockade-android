package com.cancleeric.dominoblockade.presentation.profile

import app.cash.turbine.test
import com.cancleeric.dominoblockade.data.local.entity.PlayerStatsEntity
import com.cancleeric.dominoblockade.domain.model.PlayerProfile
import com.cancleeric.dominoblockade.domain.repository.PlayerProfileRepository
import com.cancleeric.dominoblockade.domain.repository.PlayerStatsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerProfileViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val profileFlow = MutableStateFlow(PlayerProfile())
    private val profileRepository: PlayerProfileRepository = mockk(relaxed = true) {
        every { getProfile() } returns profileFlow
    }
    private val statsRepository: PlayerStatsRepository = mockk {
        coEvery { getByName(any()) } returns null
    }

    private lateinit var viewModel: PlayerProfileViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = PlayerProfileViewModel(profileRepository, statsRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `profile initial value has default name and avatar`() = runTest(testDispatcher) {
        advanceUntilIdle()
        val profile = viewModel.profile.value
        assertEquals(PlayerProfile.DEFAULT_NAME, profile.playerName)
        assertEquals(PlayerProfile.DEFAULT_AVATAR, profile.avatarEmoji)
    }

    @Test
    fun `profile updates when repository emits new profile`() = runTest(testDispatcher) {
        viewModel.profile.test {
            assertEquals(PlayerProfile.DEFAULT_NAME, awaitItem().playerName)
            profileFlow.value = PlayerProfile(playerName = "Alice", avatarEmoji = "🦊")
            assertEquals("Alice", awaitItem().playerName)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `stats is null when repository returns null`() = runTest(testDispatcher) {
        advanceUntilIdle()
        assertNull(viewModel.stats.value)
    }

    @Test
    fun `stats is populated when repository returns stats`() = runTest(testDispatcher) {
        val statsEntity = PlayerStatsEntity(playerName = "Player", wins = 3, totalGames = 5)
        coEvery { statsRepository.getByName(any()) } returns statsEntity
        profileFlow.value = PlayerProfile(playerName = "Player")
        advanceUntilIdle()
        assertEquals(3, viewModel.stats.value?.wins)
    }

    @Test
    fun `saveName calls profileRepository with trimmed name`() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.saveName("  Bob  ")
        advanceUntilIdle()
        coVerify { profileRepository.saveProfile(match { it.playerName == "Bob" }) }
    }

    @Test
    fun `saveAvatar calls profileRepository with emoji`() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.saveAvatar("🎯")
        advanceUntilIdle()
        coVerify { profileRepository.saveProfile(match { it.avatarEmoji == "🎯" }) }
    }

    @Test
    fun `saveName preserves existing avatarEmoji`() = runTest(testDispatcher) {
        profileFlow.value = PlayerProfile(playerName = "Alice", avatarEmoji = "🦊")
        advanceUntilIdle()
        viewModel.saveName("Alice2")
        advanceUntilIdle()
        coVerify { profileRepository.saveProfile(match { it.avatarEmoji == "🦊" && it.playerName == "Alice2" }) }
    }
}

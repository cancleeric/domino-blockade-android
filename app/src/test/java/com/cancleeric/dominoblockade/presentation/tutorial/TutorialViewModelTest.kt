package com.cancleeric.dominoblockade.presentation.tutorial

import com.cancleeric.dominoblockade.domain.repository.TutorialRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private const val EXPECTED_TOTAL_STEPS = 5

class TutorialViewModelTest {

    private lateinit var viewModel: TutorialViewModel
    private val completedFlow = MutableStateFlow(false)

    private val fakeRepository = object : TutorialRepository {
        override val isTutorialCompleted: Flow<Boolean> = completedFlow
        override suspend fun markTutorialCompleted() {
            completedFlow.value = true
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = TutorialViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state shows tutorial when not completed`() {
        assertTrue(viewModel.uiState.value.isVisible)
        assertEquals(0, viewModel.uiState.value.currentStep)
    }

    @Test
    fun `initial state hides tutorial when already completed`() {
        completedFlow.value = true
        val completedViewModel = TutorialViewModel(fakeRepository)
        assertFalse(completedViewModel.uiState.value.isVisible)
    }

    @Test
    fun `nextStep increments currentStep`() {
        viewModel.nextStep()
        assertEquals(1, viewModel.uiState.value.currentStep)
    }

    @Test
    fun `nextStep on last step hides tutorial`() {
        repeat(EXPECTED_TOTAL_STEPS - 1) { viewModel.nextStep() }
        viewModel.nextStep()
        assertFalse(viewModel.uiState.value.isVisible)
    }

    @Test
    fun `completeTutorial hides overlay immediately`() {
        viewModel.completeTutorial()
        assertFalse(viewModel.uiState.value.isVisible)
    }

    @Test
    fun `totalSteps matches expected count`() {
        assertEquals(EXPECTED_TOTAL_STEPS, viewModel.uiState.value.totalSteps)
    }
}

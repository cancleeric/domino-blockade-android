package com.cancleeric.dominoblockade.presentation.tutorial

import com.cancleeric.dominoblockade.domain.repository.TutorialRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class TutorialViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: TutorialRepository
    private lateinit var viewModel: TutorialViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mock()
        whenever(repository.isTutorialCompleted).thenReturn(flowOf(false))
        viewModel = TutorialViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial step index is zero`() {
        assertEquals(0, viewModel.currentStepIndex.value)
    }

    @Test
    fun `nextStep increments step index`() {
        viewModel.nextStep()
        assertEquals(1, viewModel.currentStepIndex.value)
    }

    @Test
    fun `previousStep decrements step index`() {
        viewModel.nextStep()
        viewModel.previousStep()
        assertEquals(0, viewModel.currentStepIndex.value)
    }

    @Test
    fun `previousStep does not go below zero`() {
        viewModel.previousStep()
        assertEquals(0, viewModel.currentStepIndex.value)
    }

    @Test
    fun `nextStep does not exceed total steps`() {
        repeat(20) { viewModel.nextStep() }
        assertTrue(viewModel.currentStepIndex.value < viewModel.totalSteps)
    }

    @Test
    fun `isLastStep returns false on first step`() {
        assertFalse(viewModel.isLastStep())
    }

    @Test
    fun `isLastStep returns true on last step`() {
        repeat(viewModel.totalSteps - 1) { viewModel.nextStep() }
        assertTrue(viewModel.isLastStep())
    }

    @Test
    fun `completeTutorial calls markTutorialCompleted`() = runTest {
        viewModel.completeTutorial()
        testDispatcher.scheduler.advanceUntilIdle()
        verify(repository).markTutorialCompleted()
    }

    @Test
    fun `skipTutorial calls markTutorialCompleted`() = runTest {
        viewModel.skipTutorial()
        testDispatcher.scheduler.advanceUntilIdle()
        verify(repository).markTutorialCompleted()
    }

    @Test
    fun `totalSteps is between 5 and 7`() {
        assertTrue(viewModel.totalSteps in 5..7)
    }
}

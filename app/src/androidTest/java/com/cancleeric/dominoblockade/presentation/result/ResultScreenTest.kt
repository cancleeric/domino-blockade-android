package com.cancleeric.dominoblockade.presentation.result

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit4.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ResultScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun resultScreen_winner_displaysWinnerName() {
        composeTestRule.setContent {
            ResultScreen(winnerName = "Alice", isBlocked = false)
        }

        composeTestRule.onNodeWithText("Alice wins!").assertIsDisplayed()
    }

    @Test
    fun resultScreen_winner_displaysGameOverTitle() {
        composeTestRule.setContent {
            ResultScreen(winnerName = "Bob", isBlocked = false)
        }

        composeTestRule.onNodeWithText("Game Over").assertIsDisplayed()
    }

    @Test
    fun resultScreen_blocked_displaysBlockedTitle() {
        composeTestRule.setContent {
            ResultScreen(winnerName = "", isBlocked = true)
        }

        composeTestRule.onNodeWithText("Game Blocked!").assertIsDisplayed()
    }

    @Test
    fun resultScreen_displaysPlayAgainButton() {
        composeTestRule.setContent {
            ResultScreen(winnerName = "Alice", isBlocked = false)
        }

        composeTestRule.onNodeWithText("Play Again").assertIsDisplayed()
    }

    @Test
    fun resultScreen_playAgainButton_invokesCallback() {
        var playAgainCalled = false

        composeTestRule.setContent {
            ResultScreen(
                winnerName = "Alice",
                isBlocked = false,
                onPlayAgain = { playAgainCalled = true }
            )
        }

        composeTestRule.onNodeWithText("Play Again").performClick()
        assertTrue(playAgainCalled)
    }
}

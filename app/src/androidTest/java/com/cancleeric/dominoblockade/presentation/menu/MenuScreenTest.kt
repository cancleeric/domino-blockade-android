package com.cancleeric.dominoblockade.presentation.menu

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit4.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MenuScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun menuScreen_displaysTitle() {
        composeTestRule.setContent {
            MenuScreen(onStartGame = {}, onLeaderboard = {})
        }

        composeTestRule.onNodeWithText("Domino Blockade").assertIsDisplayed()
    }

    @Test
    fun menuScreen_displaysStartGameButton() {
        composeTestRule.setContent {
            MenuScreen(onStartGame = {}, onLeaderboard = {})
        }

        composeTestRule.onNodeWithText("Start Game").assertIsDisplayed()
    }

    @Test
    fun menuScreen_startGameButton_invokesCallbackWithPlayerCount() {
        var receivedCount = 0

        composeTestRule.setContent {
            MenuScreen(onStartGame = { count -> receivedCount = count }, onLeaderboard = {})
        }

        composeTestRule.onNodeWithText("Start Game").performClick()

        assertTrue(receivedCount >= 2)
    }

    @Test
    fun menuScreen_displaysPlayerCountSelector() {
        composeTestRule.setContent {
            MenuScreen(onStartGame = {}, onLeaderboard = {})
        }

        composeTestRule.onNodeWithText("Number of Players").assertIsDisplayed()
    }

    @Test
    fun menuScreen_selectingPlayerCount_updatesSelection() {
        var receivedCount = 0

        composeTestRule.setContent {
            MenuScreen(onStartGame = { count -> receivedCount = count }, onLeaderboard = {})
        }

        composeTestRule.onNodeWithText("3").performClick()
        composeTestRule.onNodeWithText("Start Game").performClick()

        assertEquals(3, receivedCount)
    }

    @Test
    fun menuScreen_displaysLeaderboardButton() {
        composeTestRule.setContent {
            MenuScreen(onStartGame = {}, onLeaderboard = {})
        }

        composeTestRule.onNodeWithText("Leaderboard").assertIsDisplayed()
    }

    @Test
    fun menuScreen_leaderboardButton_invokesCallback() {
        var leaderboardClicked = false

        composeTestRule.setContent {
            MenuScreen(onStartGame = {}, onLeaderboard = { leaderboardClicked = true })
        }

        composeTestRule.onNodeWithText("Leaderboard").performClick()

        assertTrue(leaderboardClicked)
    }
}

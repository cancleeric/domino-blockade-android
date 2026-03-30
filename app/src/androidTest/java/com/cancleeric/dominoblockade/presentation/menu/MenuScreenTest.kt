package com.cancleeric.dominoblockade.presentation.menu

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
class MenuScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun menuScreen_displaysTitle() {
        composeTestRule.setContent {
            MenuScreen(onStartGame = {})
        }

        composeTestRule.onNodeWithText("Domino Blockade").assertIsDisplayed()
    }

    @Test
    fun menuScreen_displaysStartGameButton() {
        composeTestRule.setContent {
            MenuScreen(onStartGame = {})
        }

        composeTestRule.onNodeWithText("Start Game").assertIsDisplayed()
    }

    @Test
    fun menuScreen_startGameButton_invokesCallback() {
        var startGameCalled = false

        composeTestRule.setContent {
            MenuScreen(onStartGame = { startGameCalled = true })
        }

        composeTestRule.onNodeWithText("Start Game").performClick()

        assertTrue(startGameCalled)
    }
}

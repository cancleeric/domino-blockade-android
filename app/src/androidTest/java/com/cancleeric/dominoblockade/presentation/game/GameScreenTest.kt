package com.cancleeric.dominoblockade.presentation.game

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit4.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun gameScreen_displaysPlaceholderText() {
        composeTestRule.setContent {
            GameScreen()
        }

        composeTestRule.onNodeWithText("Game Screen - Coming Soon").assertIsDisplayed()
    }
}

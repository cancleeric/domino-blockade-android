package com.cancleeric.dominoblockade.presentation.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit4.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun appNavigation_startsOnMenuScreen() {
        composeTestRule.setContent {
            AppNavigation()
        }

        composeTestRule.onNodeWithText("Domino Blockade").assertIsDisplayed()
        composeTestRule.onNodeWithText("Start Game").assertIsDisplayed()
    }

    @Test
    fun appNavigation_startGame_navigatesToGameScreen() {
        composeTestRule.setContent {
            AppNavigation()
        }

        composeTestRule.onNodeWithText("Start Game").performClick()

        composeTestRule.onNodeWithText("Domino Blockade").assertDoesNotExist()
    }
}

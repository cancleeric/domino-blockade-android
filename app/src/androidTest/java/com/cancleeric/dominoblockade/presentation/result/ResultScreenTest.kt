package com.cancleeric.dominoblockade.presentation.result

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit4.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ResultScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun resultScreen_displaysPlaceholderText() {
        composeTestRule.setContent {
            ResultScreen()
        }

        composeTestRule.onNodeWithText("Result Screen - Coming Soon").assertIsDisplayed()
    }
}

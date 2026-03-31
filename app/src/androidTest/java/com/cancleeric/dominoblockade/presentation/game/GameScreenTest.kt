package com.cancleeric.dominoblockade.presentation.game

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit4.runners.AndroidJUnit4
import com.cancleeric.dominoblockade.domain.model.Domino
import com.cancleeric.dominoblockade.domain.model.GameState
import com.cancleeric.dominoblockade.domain.model.Player
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun buildUiState(playerName: String = "Player 1"): GameUiState {
        val player = Player(id = "p1", name = playerName, hand = listOf(Domino(1, 2)))
        val gameState = GameState(
            players = listOf(player),
            board = emptyList(),
            boneyard = emptyList()
        )
        return GameUiState(gameState = gameState)
    }

    @Test
    fun gameContent_showsCurrentPlayerName() {
        composeTestRule.setContent {
            GameContent(
                uiState = buildUiState("Alice"),
                onSelectDomino = {},
                onPlaceDomino = {},
                onDrawDomino = {}
            )
        }

        composeTestRule.onNodeWithText("Alice's Turn").assertIsDisplayed()
    }

    @Test
    fun gameContent_showsBoneyardCount() {
        composeTestRule.setContent {
            GameContent(
                uiState = buildUiState(),
                onSelectDomino = {},
                onPlaceDomino = {},
                onDrawDomino = {}
            )
        }

        composeTestRule.onNodeWithText("Boneyard: 0").assertIsDisplayed()
    }

    @Test
    fun gameContent_emptyBoard_showsPlacementHint() {
        composeTestRule.setContent {
            GameContent(
                uiState = buildUiState(),
                onSelectDomino = {},
                onPlaceDomino = {},
                onDrawDomino = {}
            )
        }

        composeTestRule.onNodeWithText("Place the first tile").assertIsDisplayed()
    }
}

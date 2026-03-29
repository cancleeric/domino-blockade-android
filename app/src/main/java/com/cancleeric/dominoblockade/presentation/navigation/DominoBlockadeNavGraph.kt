package com.cancleeric.dominoblockade.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cancleeric.dominoblockade.domain.model.GameConfig
import com.cancleeric.dominoblockade.domain.model.GamePhase
import com.cancleeric.dominoblockade.presentation.game.GameScreen
import com.cancleeric.dominoblockade.presentation.game.MultiplayerGameViewModel
import com.cancleeric.dominoblockade.presentation.menu.MenuScreen
import com.cancleeric.dominoblockade.presentation.passandplay.PassAndPlayScreen
import com.cancleeric.dominoblockade.presentation.result.ResultScreen

/**
 * Root navigation graph for Domino Blockade.
 */
@Composable
fun DominoBlockadeNavGraph() {
    val navController = rememberNavController()

    // Shared ViewModel scoped to the navigation graph
    val gameViewModel: MultiplayerGameViewModel = hiltViewModel()
    val gameState by gameViewModel.gameState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Routes.MENU
    ) {
        composable(Routes.MENU) {
            MenuScreen(
                onStartGame = { config: GameConfig ->
                    gameViewModel.startGame(config)
                    navController.navigate(Routes.GAME) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Routes.GAME) {
            GameScreen(
                viewModel = gameViewModel,
                onPassAndPlay = { prevName, nextName ->
                    navController.navigate(
                        "${Routes.PASS_AND_PLAY}/$prevName/$nextName"
                    ) {
                        launchSingleTop = true
                    }
                },
                onGameOver = {
                    navController.navigate(Routes.RESULT) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = "${Routes.PASS_AND_PLAY}/{prevName}/{nextName}",
            arguments = listOf(
                navArgument("prevName") { type = NavType.StringType },
                navArgument("nextName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val prevName = backStackEntry.arguments?.getString("prevName") ?: ""
            val nextName = backStackEntry.arguments?.getString("nextName") ?: ""

            PassAndPlayScreen(
                previousPlayerName = prevName,
                nextPlayerName = nextName,
                onReady = {
                    gameViewModel.confirmPassAndPlay()
                    navController.popBackStack(Routes.GAME, inclusive = false)
                }
            )
        }

        composable(Routes.RESULT) {
            ResultScreen(
                viewModel = gameViewModel,
                onPlayAgain = {
                    val currentState = gameState
                    val phase = currentState?.phase as? GamePhase.GameOver
                    // Restart with same config (player count and names)
                    if (currentState != null) {
                        val config = GameConfig(
                            playerCount = currentState.players.size,
                            playerNames = currentState.players.map { it.name }
                        )
                        gameViewModel.startGame(config)
                        navController.navigate(Routes.GAME) {
                            popUpTo(Routes.MENU) { inclusive = false }
                        }
                    }
                },
                onMainMenu = {
                    gameViewModel.resetGame()
                    navController.navigate(Routes.MENU) {
                        popUpTo(Routes.MENU) { inclusive = true }
                    }
                }
            )
        }
    }
}

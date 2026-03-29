package com.cancleeric.dominoblockade.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cancleeric.dominoblockade.presentation.game.GameScreen
import com.cancleeric.dominoblockade.presentation.menu.MenuScreen
import com.cancleeric.dominoblockade.presentation.result.ResultScreen

sealed class Screen(val route: String) {
    object Menu : Screen("menu")
    object Game : Screen("game/{numPlayers}/{aiDifficulty}") {
        fun createRoute(numPlayers: Int, aiDifficulty: String) = "game/$numPlayers/$aiDifficulty"
    }
    object Result : Screen("result/{winnerName}/{scores}") {
        fun createRoute(winnerName: String, scores: String) = "result/$winnerName/$scores"
    }
}

@Composable
fun DominoNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Menu.route
    ) {
        composable(Screen.Menu.route) {
            MenuScreen(
                onStartGame = { numPlayers, difficulty ->
                    navController.navigate(Screen.Game.createRoute(numPlayers, difficulty))
                }
            )
        }
        composable(
            route = Screen.Game.route,
            arguments = listOf(
                navArgument("numPlayers") { type = NavType.IntType },
                navArgument("aiDifficulty") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val numPlayers = backStackEntry.arguments?.getInt("numPlayers") ?: 2
            val aiDifficulty = backStackEntry.arguments?.getString("aiDifficulty") ?: "EASY"
            GameScreen(
                numPlayers = numPlayers,
                aiDifficulty = aiDifficulty,
                onGameEnd = { winnerName, scores ->
                    navController.navigate(Screen.Result.createRoute(winnerName, scores)) {
                        popUpTo(Screen.Menu.route)
                    }
                }
            )
        }
        composable(
            route = Screen.Result.route,
            arguments = listOf(
                navArgument("winnerName") { type = NavType.StringType },
                navArgument("scores") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val winnerName = backStackEntry.arguments?.getString("winnerName") ?: ""
            val scores = backStackEntry.arguments?.getString("scores") ?: ""
            ResultScreen(
                winnerName = winnerName,
                scoresEncoded = scores,
                onPlayAgain = {
                    navController.popBackStack(Screen.Menu.route, false)
                },
                onBackToMenu = {
                    navController.popBackStack(Screen.Menu.route, false)
                }
            )
        }
    }
}

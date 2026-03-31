package com.cancleeric.dominoblockade.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cancleeric.dominoblockade.presentation.game.GameScreen
import com.cancleeric.dominoblockade.presentation.menu.MenuScreen
import com.cancleeric.dominoblockade.presentation.result.ResultScreen

private const val DEFAULT_PLAYER_COUNT = 2

sealed class Screen(val route: String) {
    object Menu : Screen("menu")
    object Game : Screen("game/{playerCount}") {
        fun createRoute(playerCount: Int) = "game/$playerCount"
    }
    object Result : Screen("result/{winnerName}/{isBlocked}") {
        fun createRoute(winnerName: String, isBlocked: Boolean) =
            "result/${winnerName.ifEmpty { "_" }}/$isBlocked"
    }
}

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Screen.Menu.route,
        modifier = modifier
    ) {
        composable(Screen.Menu.route) {
            MenuScreen(
                onStartGame = { playerCount ->
                    navController.navigate(Screen.Game.createRoute(playerCount))
                }
            )
        }
        composable(
            route = Screen.Game.route,
            arguments = listOf(
                navArgument("playerCount") {
                    type = NavType.IntType
                    defaultValue = DEFAULT_PLAYER_COUNT
                }
            )
        ) { backStackEntry ->
            val playerCount = backStackEntry.arguments?.getInt("playerCount") ?: DEFAULT_PLAYER_COUNT
            GameScreen(
                playerCount = playerCount,
                onGameOver = { winnerName, isBlocked ->
                    navController.navigate(Screen.Result.createRoute(winnerName, isBlocked)) {
                        popUpTo(Screen.Game.route) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = Screen.Result.route,
            arguments = listOf(
                navArgument("winnerName") { type = NavType.StringType },
                navArgument("isBlocked") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val winnerName = backStackEntry.arguments?.getString("winnerName")
                ?.let { if (it == "_") "" else it }.orEmpty()
            val isBlocked = backStackEntry.arguments?.getBoolean("isBlocked") ?: false
            ResultScreen(
                winnerName = winnerName,
                isBlocked = isBlocked,
                onPlayAgain = {
                    navController.navigate(Screen.Menu.route) {
                        popUpTo(Screen.Menu.route) { inclusive = true }
                    }
                },
                onMenu = {
                    navController.navigate(Screen.Menu.route) {
                        popUpTo(Screen.Menu.route) { inclusive = true }
                    }
                }
            )
        }
    }
}

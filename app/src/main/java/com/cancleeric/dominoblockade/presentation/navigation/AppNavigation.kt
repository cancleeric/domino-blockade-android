package com.cancleeric.dominoblockade.presentation.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cancleeric.dominoblockade.presentation.game.GameScreen
import com.cancleeric.dominoblockade.presentation.leaderboard.LeaderboardScreen
import com.cancleeric.dominoblockade.presentation.lobby.LobbyScreen
import com.cancleeric.dominoblockade.presentation.menu.MenuScreen
import com.cancleeric.dominoblockade.presentation.onlinegame.OnlineGameScreen
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
    object Leaderboard : Screen("leaderboard")
    object Lobby : Screen("lobby")
    object OnlineGame : Screen("onlineGame/{roomId}/{playerIndex}") {
        fun createRoute(roomId: String, playerIndex: Int) = "onlineGame/$roomId/$playerIndex"
    }
}

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Screen.Menu.route,
        modifier = modifier,
        enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) + fadeOut() },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) + fadeIn() },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
    ) {
        composable(Screen.Menu.route) {
            MenuScreen(
                onStartGame = { playerCount ->
                    navController.navigate(Screen.Game.createRoute(playerCount))
                },
                onLeaderboard = { navController.navigate(Screen.Leaderboard.route) },
                onOnlineMultiplayer = { navController.navigate(Screen.Lobby.route) }
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
            val navigateToMenu = {
                navController.navigate(Screen.Menu.route) {
                    popUpTo(Screen.Menu.route) { inclusive = true }
                }
            }
            ResultScreen(
                winnerName = winnerName,
                isBlocked = isBlocked,
                onPlayAgain = navigateToMenu,
                onMenu = navigateToMenu
            )
        }
        composable(Screen.Leaderboard.route) {
            LeaderboardScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Screen.Lobby.route) {
            LobbyScreen(
                onNavigateToGame = { roomId, playerIndex ->
                    navController.navigate(Screen.OnlineGame.createRoute(roomId, playerIndex)) {
                        popUpTo(Screen.Lobby.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.OnlineGame.route,
            arguments = listOf(
                navArgument("roomId") { type = NavType.StringType },
                navArgument("playerIndex") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString("roomId").orEmpty()
            val playerIndex = backStackEntry.arguments?.getInt("playerIndex") ?: 0
            OnlineGameScreen(
                roomId = roomId,
                localPlayerIndex = playerIndex,
                onGameOver = { winnerName, isBlocked ->
                    navController.navigate(Screen.Result.createRoute(winnerName, isBlocked)) {
                        popUpTo(Screen.OnlineGame.route) { inclusive = true }
                    }
                },
                onOpponentLeft = {
                    navController.navigate(Screen.Menu.route) {
                        popUpTo(Screen.Menu.route) { inclusive = true }
                    }
                }
            )
        }
    }
}

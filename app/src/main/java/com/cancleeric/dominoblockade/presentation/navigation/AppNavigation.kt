package com.cancleeric.dominoblockade.presentation.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cancleeric.dominoblockade.presentation.game.GameScreen
import com.cancleeric.dominoblockade.presentation.leaderboard.LeaderboardScreen
import com.cancleeric.dominoblockade.presentation.localmultiplayer.LocalMultiplayerScreen
import com.cancleeric.dominoblockade.presentation.menu.MenuScreen
import com.cancleeric.dominoblockade.presentation.result.ResultScreen
import com.cancleeric.dominoblockade.presentation.theme.ThemeSelectionScreen
import com.cancleeric.dominoblockade.presentation.tutorial.TutorialOverlay
import com.cancleeric.dominoblockade.presentation.tutorial.TutorialViewModel

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
    object LocalMultiplayer : Screen("localMultiplayer")
    object ThemeSelection : Screen("theme")
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
            val tutorialViewModel: TutorialViewModel = hiltViewModel()
            val tutorialState by tutorialViewModel.uiState.collectAsState()
            Box(modifier = Modifier.fillMaxSize()) {
                MenuScreen(
                    onStartGame = { playerCount ->
                        navController.navigate(Screen.Game.createRoute(playerCount))
                    },
                    onLeaderboard = {
                        navController.navigate(Screen.Leaderboard.route)
                    },
                    onLocalMultiplayer = {
                        navController.navigate(Screen.LocalMultiplayer.route)
                    },
                    onThemeSettings = {
                        navController.navigate(Screen.ThemeSelection.route)
                    }
                )
                TutorialOverlay(
                    uiState = tutorialState,
                    onNext = tutorialViewModel::nextStep,
                    onSkip = tutorialViewModel::completeTutorial
                )
            }
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
            LeaderboardScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.LocalMultiplayer.route) {
            LocalMultiplayerScreen(
                onGameOver = { winnerName, isBlocked ->
                    navController.navigate(Screen.Result.createRoute(winnerName, isBlocked)) {
                        popUpTo(Screen.LocalMultiplayer.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.ThemeSelection.route) {
            ThemeSelectionScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

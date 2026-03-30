package com.cancleeric.dominoblockade.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cancleeric.dominoblockade.presentation.menu.MenuScreen
import com.cancleeric.dominoblockade.presentation.menu.MenuViewModel
import com.cancleeric.dominoblockade.presentation.tutorial.TutorialScreen

sealed class Screen(val route: String) {
    object Menu : Screen("menu")
    object Game : Screen("game")
    object Result : Screen("result")
    object Tutorial : Screen("tutorial")
}

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val menuViewModel: MenuViewModel = hiltViewModel()
    val shouldAutoStartTutorial by menuViewModel.shouldAutoStartTutorial.collectAsState(initial = null)

    // Wait for the DataStore read before rendering to avoid flashing the wrong start screen
    val resolvedStart = shouldAutoStartTutorial ?: return

    val startDestination = if (resolvedStart) Screen.Tutorial.route else Screen.Menu.route

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Tutorial.route) {
            TutorialScreen(
                onTutorialFinished = {
                    navController.navigate(Screen.Menu.route) {
                        popUpTo(Screen.Tutorial.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Menu.route) {
            MenuScreen(
                onStartGame = { navController.navigate(Screen.Game.route) },
                onStartTutorial = {
                    navController.navigate(Screen.Tutorial.route)
                }
            )
        }
        composable(Screen.Game.route) {
            // Placeholder - GameScreen
        }
        composable(Screen.Result.route) {
            // Placeholder - ResultScreen
        }
    }
}

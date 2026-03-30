package com.cancleeric.dominoblockade.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cancleeric.dominoblockade.presentation.menu.MenuScreen
import com.cancleeric.dominoblockade.presentation.theme.ThemeSelectionScreen

sealed class Screen(val route: String) {
    object Menu : Screen("menu")
    object Game : Screen("game")
    object Result : Screen("result")
    object ThemeSettings : Screen("theme_settings")
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
                onStartGame = { navController.navigate(Screen.Game.route) },
                onOpenThemeSettings = { navController.navigate(Screen.ThemeSettings.route) }
            )
        }
        composable(Screen.Game.route) {
            // Placeholder - GameScreen
        }
        composable(Screen.Result.route) {
            // Placeholder - ResultScreen
        }
        composable(Screen.ThemeSettings.route) {
            ThemeSelectionScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}

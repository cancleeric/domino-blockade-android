package com.cancleeric.dominoblockade.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cancleeric.dominoblockade.presentation.history.GameDetailScreen
import com.cancleeric.dominoblockade.presentation.history.GameHistoryScreen
import com.cancleeric.dominoblockade.presentation.menu.MenuScreen
import com.cancleeric.dominoblockade.presentation.stats.StatsScreen

sealed class Screen(val route: String) {
    object Menu : Screen("menu")
    object Game : Screen("game")
    object Result : Screen("result")
    object History : Screen("history")
    object Stats : Screen("stats")
    object GameDetail : Screen("game_detail/{recordId}") {
        fun createRoute(recordId: Long) = "game_detail/$recordId"
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
                onStartGame = { navController.navigate(Screen.Game.route) },
                onOpenHistory = { navController.navigate(Screen.History.route) },
                onOpenStats = { navController.navigate(Screen.Stats.route) }
            )
        }
        composable(Screen.Game.route) {
            // Placeholder - GameScreen
        }
        composable(Screen.Result.route) {
            // Placeholder - ResultScreen
        }
        composable(Screen.History.route) {
            GameHistoryScreen(
                onBack = { navController.popBackStack() },
                onRecordClick = { recordId ->
                    navController.navigate(Screen.GameDetail.createRoute(recordId))
                }
            )
        }
        composable(Screen.Stats.route) {
            StatsScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Screen.GameDetail.route,
            arguments = listOf(navArgument("recordId") { type = NavType.LongType })
        ) {
            GameDetailScreen(onBack = { navController.popBackStack() })
        }
    }
}

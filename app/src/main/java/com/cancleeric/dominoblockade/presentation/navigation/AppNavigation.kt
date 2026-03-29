package com.cancleeric.dominoblockade.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cancleeric.dominoblockade.presentation.leaderboard.LeaderboardScreen
import com.cancleeric.dominoblockade.presentation.menu.MenuScreen

private object Routes {
    const val MENU = "menu"
    const val LEADERBOARD = "leaderboard"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.MENU
    ) {
        composable(Routes.MENU) {
            MenuScreen(
                onStartGame = {
                    // TODO: navigate to game screen (Phase 3)
                },
                onOpenLeaderboard = {
                    navController.navigate(Routes.LEADERBOARD)
                }
            )
        }
        composable(Routes.LEADERBOARD) {
            LeaderboardScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

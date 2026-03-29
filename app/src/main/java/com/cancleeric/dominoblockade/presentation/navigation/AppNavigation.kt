package com.cancleeric.dominoblockade.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cancleeric.dominoblockade.presentation.achievement.AchievementScreen
import com.cancleeric.dominoblockade.presentation.history.HistoryScreen
import com.cancleeric.dominoblockade.presentation.stats.StatsScreen

sealed class Screen(val route: String) {
    object History : Screen("history")
    object Stats : Screen("stats")
    object Achievements : Screen("achievements")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.History.route) {
        composable(Screen.History.route) {
            HistoryScreen(
                onNavigateToStats = { navController.navigate(Screen.Stats.route) },
                onNavigateToAchievements = { navController.navigate(Screen.Achievements.route) }
            )
        }
        composable(Screen.Stats.route) {
            StatsScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Screen.Achievements.route) {
            AchievementScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}

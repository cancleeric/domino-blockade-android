package com.cancleeric.dominoblockade.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cancleeric.dominoblockade.presentation.menu.MenuScreen

sealed class Screen(val route: String) {
    object Menu : Screen("menu")
    object Game : Screen("game")
    object Result : Screen("result")
}

private const val NAV_ANIM_DURATION_MS = 350

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Screen.Menu.route,
        modifier = modifier,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(NAV_ANIM_DURATION_MS)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(NAV_ANIM_DURATION_MS)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(NAV_ANIM_DURATION_MS)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(NAV_ANIM_DURATION_MS)
            )
        }
    ) {
        composable(Screen.Menu.route) {
            MenuScreen(onStartGame = { navController.navigate(Screen.Game.route) })
        }
        composable(Screen.Game.route) {
            // Placeholder - GameScreen
        }
        composable(Screen.Result.route) {
            // Placeholder - ResultScreen
        }
    }
}


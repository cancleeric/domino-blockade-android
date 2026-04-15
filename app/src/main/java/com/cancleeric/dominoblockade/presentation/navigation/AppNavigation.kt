package com.cancleeric.dominoblockade.presentation.navigation

import android.net.Uri
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cancleeric.dominoblockade.presentation.achievements.AchievementsScreen
import com.cancleeric.dominoblockade.presentation.game.GameScreen
import com.cancleeric.dominoblockade.presentation.leaderboard.LeaderboardScreen
import com.cancleeric.dominoblockade.presentation.lobby.LobbyScreen
import com.cancleeric.dominoblockade.presentation.localmultiplayer.LocalMultiplayerScreen
import com.cancleeric.dominoblockade.presentation.menu.MenuScreen
import com.cancleeric.dominoblockade.presentation.onlinegame.OnlineGameScreen
import com.cancleeric.dominoblockade.presentation.profile.PlayerProfileScreen
import com.cancleeric.dominoblockade.presentation.quest.QuestScreen
import com.cancleeric.dominoblockade.presentation.replay.ReplayScreen
import com.cancleeric.dominoblockade.presentation.tournament.TournamentBracketScreen
import com.cancleeric.dominoblockade.presentation.tournament.TournamentSetupScreen
import com.cancleeric.dominoblockade.presentation.result.ResultScreen
import com.cancleeric.dominoblockade.presentation.result.ResultViewModel
import com.cancleeric.dominoblockade.presentation.settings.SettingsScreen
import com.cancleeric.dominoblockade.presentation.shop.ShopScreen
import com.cancleeric.dominoblockade.presentation.social.SocialScreen
import com.cancleeric.dominoblockade.presentation.theme.ThemeSelectionScreen
import com.cancleeric.dominoblockade.presentation.tutorial.TutorialOverlay
import com.cancleeric.dominoblockade.presentation.tutorial.TutorialViewModel

private const val DEFAULT_PLAYER_COUNT = 2
private const val NO_QUICK_START = -1

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
    object Settings : Screen("settings")
    object Achievements : Screen("achievements")
    object PlayerProfile : Screen("playerProfile")
    object Social : Screen("social?challengeId={challengeId}&challengeAction={challengeAction}") {
        fun createRoute(challengeId: String? = null, challengeAction: String? = null): String {
            val id = Uri.encode(challengeId.orEmpty())
            val action = Uri.encode(challengeAction.orEmpty())
            return "social?challengeId=$id&challengeAction=$action"
        }
    }
    object Lobby : Screen("lobby?roomCode={roomCode}") {
        fun createRoute(roomCode: String? = null): String {
            val encodedRoomCode = Uri.encode(roomCode.orEmpty())
            return if (encodedRoomCode.isBlank()) "lobby" else "lobby?roomCode=$encodedRoomCode"
        }
    }
    object Replay : Screen("replay")
    object TournamentSetup : Screen("tournamentSetup")
    object TournamentBracket : Screen("tournamentBracket")
    object OnlineGame : Screen("onlineGame/{roomId}/{playerIndex}/{playerId}") {
        fun createRoute(roomId: String, playerIndex: Int, playerId: String) =
            "onlineGame/$roomId/$playerIndex/$playerId"
    }
    object Shop : Screen("shop")
    object Quests : Screen("quests")
}

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    quickStartPlayerCount: Int = NO_QUICK_START,
    incomingDeepLink: String? = null,
    onDeepLinkHandled: () -> Unit = {}
) {
    val navController = rememberNavController()
    val startDestination = if (quickStartPlayerCount > 0) {
        Screen.Game.createRoute(quickStartPlayerCount)
    } else {
        Screen.Menu.route
    }
    LaunchedEffect(incomingDeepLink) {
        val deepLink = incomingDeepLink ?: return@LaunchedEffect
        val uri = runCatching { Uri.parse(deepLink) }.getOrNull() ?: return@LaunchedEffect
        if (uri.scheme != "domino-blockade") return@LaunchedEffect
        when (uri.host) {
            "challenge" -> {
                val action = uri.pathSegments.firstOrNull()
                val challengeId = uri.getQueryParameter("challengeId")
                if (!challengeId.isNullOrBlank() && !action.isNullOrBlank()) {
                    navController.navigate(Screen.Social.createRoute(challengeId, action))
                }
            }
            "friend" -> navController.navigate(Screen.Social.createRoute())
        }
        onDeepLinkHandled()
    }
    NavHost(
        navController = navController,
        startDestination = startDestination,
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
                    },
                    onSettings = {
                        navController.navigate(Screen.Settings.route)
                    },
                    onAchievements = {
                        navController.navigate(Screen.Achievements.route)
                    },
                    onProfile = {
                        navController.navigate(Screen.PlayerProfile.route)
                    },
                    onOnlineMultiplayer = { navController.navigate(Screen.Lobby.createRoute()) },
                    onSocial = { navController.navigate(Screen.Social.createRoute()) },
                    onReplayLastGame = {
                        navController.navigate(Screen.Replay.route)
                    },
                    onTournament = { navController.navigate(Screen.TournamentSetup.route) },
                    onShop = { navController.navigate(Screen.Shop.route) },
                    onQuests = { navController.navigate(Screen.Quests.route) }
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
            val resultViewModel: ResultViewModel = hiltViewModel()
            val newAchievements by resultViewModel.newAchievements.collectAsStateWithLifecycle()
            val navigateToMenu = {
                navController.navigate(Screen.Menu.route) {
                    popUpTo(Screen.Menu.route) { inclusive = true }
                }
            }
            ResultScreen(
                winnerName = winnerName,
                isBlocked = isBlocked,
                newAchievements = newAchievements,
                onPlayAgain = navigateToMenu,
                onMenu = navigateToMenu,
                onViewReplay = { navController.navigate(Screen.Replay.route) }
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
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Achievements.route) {
            AchievementsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.PlayerProfile.route) {
            PlayerProfileScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Replay.route) {
            ReplayScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.TournamentSetup.route) {
            TournamentSetupScreen(
                onBack = { navController.popBackStack() },
                onTournamentCreated = {
                    navController.navigate(Screen.TournamentBracket.route) {
                        popUpTo(Screen.TournamentSetup.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.TournamentBracket.route) {
            TournamentBracketScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.Lobby.route,
            arguments = listOf(
                navArgument("roomCode") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val roomCode = backStackEntry.arguments?.getString("roomCode")
                ?.takeIf { it.isNotBlank() }
            LobbyScreen(
                initialRoomCode = roomCode,
                onNavigateToGame = { roomId, playerIndex, playerId ->
                    navController.navigate(Screen.OnlineGame.createRoute(roomId, playerIndex, playerId)) {
                        popUpTo(Screen.Lobby.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.Social.route,
            arguments = listOf(
                navArgument("challengeId") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("challengeAction") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            SocialScreen(
                onBack = { navController.popBackStack() },
                onNavigateToLobby = { roomId ->
                    navController.navigate(Screen.Lobby.createRoute(roomId))
                },
                challengeIdFromDeepLink = backStackEntry.arguments?.getString("challengeId")
                    ?.takeIf { it.isNotBlank() },
                challengeActionFromDeepLink = backStackEntry.arguments?.getString("challengeAction")
                    ?.takeIf { it.isNotBlank() }
            )
        }
        composable(
            route = Screen.OnlineGame.route,
            arguments = listOf(
                navArgument("roomId") { type = NavType.StringType },
                navArgument("playerIndex") { type = NavType.IntType },
                navArgument("playerId") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString("roomId").orEmpty()
            val playerIndex = backStackEntry.arguments?.getInt("playerIndex") ?: 0
            val playerId = backStackEntry.arguments?.getString("playerId").orEmpty()
            OnlineGameScreen(
                roomId = roomId,
                localPlayerIndex = playerIndex,
                localPlayerId = playerId,
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
        composable(Screen.Shop.route) {
            ShopScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Quests.route) {
            QuestScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

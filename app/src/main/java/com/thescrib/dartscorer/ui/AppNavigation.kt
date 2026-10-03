package com.thescrib.dartscorer.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.thescrib.dartscorer.AppContainer
import com.thescrib.dartscorer.ui.components.GamePreset
import com.thescrib.dartscorer.ui.game.GameScreen
import com.thescrib.dartscorer.ui.home.HomeScreen
import com.thescrib.dartscorer.ui.settings.SettingsScreen
import com.thescrib.dartscorer.ui.setup.SetupScreen
import kotlinx.coroutines.launch

object Routes {
    const val HOME = "home"
    const val SETUP = "setup/{preset}"
    const val GAME = "game"
    const val SETTINGS = "settings"

    fun setup(preset: GamePreset) = "setup/${preset.name}"
}

@Composable
fun AppNavigation(navController: NavHostController, container: AppContainer) {
    val match by container.matches.match.collectAsStateWithLifecycle()
    val settings by container.settings.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val repo = container.settingsRepository

    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() },
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                match = match,
                onChoose = { navController.navigate(Routes.setup(it)) },
                onResume = { navController.navigate(Routes.GAME) { launchSingleTop = true } },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
            )
        }
        composable(Routes.SETUP) { entry ->
            val preset = entry.arguments?.getString("preset")
                ?.let { name -> GamePreset.entries.firstOrNull { it.name == name } } ?: GamePreset.X501
            // Eerst de vorige spelers laden, zodat de namen meteen klaarstaan.
            val lastPlayers by produceState<List<String>?>(null) { value = repo.lastPlayers() }
            lastPlayers?.let { players ->
                SetupScreen(
                    preset = preset,
                    initialPlayers = players,
                    onStart = { config, names ->
                        container.matches.start(config, names)
                        navController.navigate(Routes.GAME) { popUpTo(Routes.HOME) }
                    },
                    onBack = { navController.popBackStack() },
                )
            }
        }
        composable(Routes.GAME) {
            val current = match
            if (current == null) {
                LaunchedEffect(Unit) { navController.popBackStack(Routes.HOME, inclusive = false) }
            } else {
                GameScreen(
                    match = current,
                    onDart = container.matches::throwDart,
                    onUndo = container.matches::undo,
                    onRestart = container.matches::restart,
                    onQuit = {
                        navController.popBackStack(Routes.HOME, inclusive = false)
                        container.matches.quit()
                    },
                    onBack = { navController.popBackStack(Routes.HOME, inclusive = false) },
                )
            }
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                settings = settings,
                onKeepScreenOn = { scope.launch { repo.setKeepScreenOn(it) } },
                onThemeMode = { scope.launch { repo.setThemeMode(it) } },
                onLanguage = { scope.launch { repo.setLanguage(it) } },
                onBack = { navController.popBackStack() },
            )
        }
    }
}

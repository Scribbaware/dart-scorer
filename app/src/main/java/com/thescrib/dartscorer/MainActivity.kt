package com.thescrib.dartscorer

import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.thescrib.dartscorer.data.AppLanguage
import com.thescrib.dartscorer.ui.AppNavigation
import com.thescrib.dartscorer.ui.Routes
import com.thescrib.dartscorer.ui.theme.DartScorerTheme
import com.thescrib.dartscorer.ui.theme.isDark

class MainActivity : ComponentActivity() {

    /**
     * Gekozen taal van de app. Dialogen en menu's openen in een eigen venster, en Compose haalt de teksten
     * daar opnieuw uit de activity. Daarom geeft de activity zelf ook de teksten in deze taal terug
     * (zie [getResources]); anders tonen die vensters de taal van de telefoon.
     */
    private var appLanguage: AppLanguage? = null
        set(value) {
            if (field != value) localizedResources = null
            field = value
        }
    private var localizedResources: Resources? = null
    private var localizedFor: Configuration? = null

    override fun getResources(): Resources {
        val base = super.getResources()
        val language = appLanguage ?: return base
        val config = base.configuration
        localizedResources?.let { if (config == localizedFor) return it }
        return resourcesIn(language, config).also {
            localizedResources = it
            localizedFor = Configuration(config)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as DartScorerApp).container

        setContent {
            val settings by container.settings.collectAsStateWithLifecycle()
            val dark = settings.themeMode.isDark()
            val navController = rememberNavController()
            val backStack by navController.currentBackStackEntryAsState()
            val inGame = backStack?.destination?.route == Routes.GAME

            DisposableEffect(dark) {
                val style = if (dark) {
                    SystemBarStyle.dark(Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                onDispose { }
            }

            // Tijdens een spel blijft het scherm aan (als dat in de instellingen aan staat).
            DisposableEffect(inGame, settings.keepScreenOn) {
                if (inGame && settings.keepScreenOn) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
                onDispose { }
            }

            // Teksten in de gekozen taal, onafhankelijk van de taal van de telefoon.
            SideEffect { appLanguage = settings.language }
            val baseConfig = LocalConfiguration.current
            val localized = remember(settings.language, baseConfig) { withLanguage(settings.language, baseConfig) }
            CompositionLocalProvider(
                LocalContext provides localized,
                LocalConfiguration provides localized.resources.configuration,
            ) {
                DartScorerTheme(darkTheme = dark) {
                    AppNavigation(navController, container)
                }
            }
        }
    }
}

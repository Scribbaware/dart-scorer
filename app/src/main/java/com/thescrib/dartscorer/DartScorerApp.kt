package com.thescrib.dartscorer

import android.app.Application
import com.thescrib.dartscorer.data.MatchController
import com.thescrib.dartscorer.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.thescrib.dartscorer.data.Settings as AppSettings

/**
 * Eenvoudige "handmatige dependency injection": één plek waar alle gedeelde objecten leven.
 * Voor een kleine app is dit overzichtelijker dan een DI-framework zoals Hilt.
 */
class AppContainer(app: Application) {
    /** Scope die zo lang leeft als de app; voor werk dat een scherm moet overleven. */
    val appScope = CoroutineScope(SupervisorJob())

    val settingsRepository = SettingsRepository(app)
    val settings: StateFlow<AppSettings> =
        settingsRepository.settings.stateIn(appScope, SharingStarted.Eagerly, AppSettings())
    val matches = MatchController(settingsRepository, appScope)
}

class DartScorerApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.appScope.launch { container.matches.restore() }
    }
}

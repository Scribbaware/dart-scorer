package com.thescrib.dartscorer.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import com.thescrib.dartscorer.data.AppLanguage
import com.thescrib.dartscorer.data.Settings
import com.thescrib.dartscorer.domain.AroundTheClockConfig
import com.thescrib.dartscorer.domain.CricketConfig
import com.thescrib.dartscorer.domain.Dart
import com.thescrib.dartscorer.domain.KillerConfig
import com.thescrib.dartscorer.domain.Match
import com.thescrib.dartscorer.domain.ShanghaiConfig
import com.thescrib.dartscorer.domain.X01Config
import com.thescrib.dartscorer.ui.components.GamePreset
import com.thescrib.dartscorer.ui.components.KofiDialogContent
import com.thescrib.dartscorer.ui.game.GameScreen
import com.thescrib.dartscorer.ui.home.HomeScreen
import com.thescrib.dartscorer.ui.settings.KofiCard
import com.thescrib.dartscorer.ui.settings.SettingsScreen
import com.thescrib.dartscorer.ui.setup.SetupScreen
import com.thescrib.dartscorer.ui.theme.DartScorerTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/**
 * Rendert alle schermen naar `app/build/screenshots/`. Draai met:
 * `./gradlew :app:testDebugUnitTest --tests '*ScreenshotTest*'`
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h860dp-xxhdpi")
class ScreenshotTest(private val dark: Boolean) {

    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private val theme = if (dark) "dark" else "light"
    private val players = listOf("Anna", "Bram", "Chris")

    private fun t(n: Int) = Dart(n, 3)
    private fun d(n: Int) = Dart(n, 2)
    private fun s(n: Int) = Dart(n)

    private fun snap(name: String, content: @Composable () -> Unit) {
        compose.setContent { DartScorerTheme(darkTheme = dark) { content() } }
        compose.waitForIdle()
        compose.mainClock.advanceTimeBy(2_000)
        val view = compose.activity.window.decorView
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        view.draw(Canvas(bitmap))
        val dir = File("build/screenshots").apply { mkdirs() }
        File(dir, "${name}_$theme.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    @Composable
    private fun game(match: Match) {
        GameScreen(match, onDart = {}, onUndo = {}, onRestart = {}, onQuit = {}, onBack = {})
    }

    @Test
    fun home() = snap("1_home") {
        HomeScreen(match = null, onChoose = {}, onResume = {}, onOpenSettings = {})
    }

    @Test
    fun homeWithGame() = snap("2_home_resume") {
        HomeScreen(match = Match(X01Config(), players), onChoose = {}, onResume = {}, onOpenSettings = {})
    }

    @Test
    fun setup() = snap("3_setup_501") {
        SetupScreen(GamePreset.X501, players, onStart = { _, _ -> }, onBack = {})
    }

    @Test
    fun x01() = snap("4_game_501") {
        game(
            Match(
                X01Config(), players,
                listOf(t(20), t(20), s(20), t(19), s(19), s(7), s(20), s(5), s(1), t(20), t(20), t(20), s(20)),
            ),
        )
    }

    @Test
    fun cricket() = snap("5_game_cricket") {
        game(Match(CricketConfig, players.take(2), listOf(t(20), d(19), s(18), t(20), s(17), s(17), t(20), s(15))))
    }

    @Test
    fun clock() = snap("6_game_clock") {
        game(Match(AroundTheClockConfig(), players, listOf(s(1), s(2), s(3), s(1), s(5), s(2), s(1), s(2))))
    }

    @Test
    fun shanghai() = snap("7_game_shanghai") {
        game(Match(ShanghaiConfig(), players, listOf(s(1), t(1), s(1), s(1), Dart.MISS, Dart.MISS, d(1), s(1), Dart.MISS, s(2))))
    }

    @Test
    fun killer() = snap("8_game_killer") {
        game(Match(KillerConfig(3, listOf(14, 6, 11)), players, listOf(d(14), d(6), d(6), d(6), Dart.MISS, Dart.MISS, d(11))))
    }

    @Test
    fun winner() = snap("9_winner") {
        game(Match(X01Config(start = 101), players.take(2), listOf(t(20), s(1), d(20))))
    }

    @Test
    fun settings() = snap("10_settings") {
        SettingsScreen(Settings(language = AppLanguage.ENGLISH), {}, {}, {}, {})
    }

    // Dezelfde instellingen in het Nederlands: controleert de vertaling en de taalkeuze.
    @Test
    @Config(qualifiers = "+nl")
    fun settingsDutch() = snap("10_settings_nl") {
        SettingsScreen(Settings(language = AppLanguage.DUTCH), {}, {}, {}, {})
    }

    @Test
    @Config(qualifiers = "+de")
    fun settingsGerman() = snap("10_settings_de") {
        SettingsScreen(Settings(language = AppLanguage.GERMAN), {}, {}, {}, {})
    }

    @Test
    @Config(qualifiers = "+fr")
    fun setupFrench() = snap("3_setup_501_fr") {
        SetupScreen(GamePreset.X501, players, onStart = { _, _ -> }, onBack = {})
    }

    @Test
    @Config(qualifiers = "+es")
    fun homeSpanish() = snap("1_home_es") {
        HomeScreen(match = Match(X01Config(), players), onChoose = {}, onResume = {}, onOpenSettings = {})
    }

    @Test
    @Config(qualifiers = "+nl")
    fun x01Dutch() = snap("4_game_501_nl") {
        game(Match(X01Config(), players, listOf(t(20), t(20), s(20))))
    }

    // De Ko-fi-kaart staat onderaan Instellingen, mogelijk buiten beeld van de schermvullende screenshot.
    @Test
    fun kofiCard() = snap("11_kofi") {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Box(Modifier.padding(16.dp)) { KofiCard(onClick = {}) }
        }
    }

    // Het venster zelf; Dialog opent een eigen window dat niet in de screenshot komt, dus alleen de inhoud.
    @Test
    fun kofiDialog() = snap("12_kofi_dialog") {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f)) {
            Box(Modifier.padding(horizontal = 40.dp, vertical = 200.dp)) { KofiDialogContent(onOpen = {}, onDismiss = {}) }
        }
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "dark={0}")
        fun params() = listOf(arrayOf<Any>(false), arrayOf<Any>(true))
    }
}

package com.thescrib.dartscorer.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thescrib.dartscorer.R
import com.thescrib.dartscorer.data.AppLanguage
import com.thescrib.dartscorer.data.Settings
import com.thescrib.dartscorer.data.ThemeMode
import com.thescrib.dartscorer.domain.CricketConfig
import com.thescrib.dartscorer.domain.Dart
import com.thescrib.dartscorer.domain.KillerConfig
import com.thescrib.dartscorer.domain.Match
import com.thescrib.dartscorer.domain.ShanghaiConfig
import com.thescrib.dartscorer.domain.X01Config
import com.thescrib.dartscorer.ui.components.GamePreset
import com.thescrib.dartscorer.ui.game.GameScreen
import com.thescrib.dartscorer.ui.home.HomeScreen
import com.thescrib.dartscorer.ui.settings.SettingsScreen
import com.thescrib.dartscorer.ui.setup.SetupScreen
import com.thescrib.dartscorer.ui.theme.BoardGreen
import com.thescrib.dartscorer.ui.theme.BoardGreenDeep
import com.thescrib.dartscorer.ui.theme.BoardRed
import com.thescrib.dartscorer.ui.theme.DartScorerTheme
import com.thescrib.dartscorer.ui.theme.DisplayFont
import com.thescrib.dartscorer.ui.theme.TextFont
import com.thescrib.dartscorer.withLanguage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Naam in de Play Store; staat ook op de feature graphic. */
private const val STORE_NAME = "Scribba Darts"

/**
 * Maakt de afbeeldingen voor de Play Store in `app/build/store/`:
 * telefoon-screenshots per taal (verhouding 9:16, zoals de Play Store wil), het icoon van 512×512
 * en de feature graphic van 1024×500. Draai met:
 * `./gradlew :app:testDebugUnitTest --tests '*StoreGraphics*'`
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h711dp-xxhdpi")
class StoreScreenshotTest(private val language: AppLanguage) {

    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private val players = listOf("Anna", "Bram", "Chris")
    private fun t(n: Int) = Dart(n, 3)
    private fun d(n: Int) = Dart(n, 2)
    private fun s(n: Int) = Dart(n)

    private fun shot(name: String, dark: Boolean, content: @Composable () -> Unit) {
        compose.setContent {
            val localized = compose.activity.withLanguage(language)
            CompositionLocalProvider(
                LocalContext provides localized,
                LocalConfiguration provides localized.resources.configuration,
            ) {
                DartScorerTheme(darkTheme = dark) { content() }
            }
        }
        compose.waitForIdle()
        compose.mainClock.advanceTimeBy(2_000)
        save(compose.activity, File("build/store/screenshots/${language.tag}"), "$name.png")
    }

    @Composable
    private fun game(match: Match) {
        GameScreen(match, onDart = {}, onUndo = {}, onRestart = {}, onQuit = {}, onBack = {})
    }

    // 501 met uitgooi-advies: Anna staat op 100.
    private val x01 = Match(
        X01Config(), players.take(2),
        listOf(
            t(20), t(20), t(20), t(20), s(20), s(20),
            t(20), t(20), s(20), t(15), s(20), s(20),
            t(19), s(12), s(12), s(20), s(20), s(20),
        ),
    )

    @Test
    fun x01() = shot("1_501", dark = false) { game(x01) }

    @Test
    fun home() = shot("2_home", dark = true) {
        HomeScreen(match = x01, onChoose = {}, onResume = {}, onOpenSettings = {})
    }

    @Test
    fun cricket() = shot("3_cricket", dark = false) {
        game(Match(CricketConfig, players.take(2), listOf(t(20), d(19), s(18), t(20), s(17), s(17), t(20), s(15), Dart(25))))
    }

    @Test
    fun killer() = shot("4_killer", dark = true) {
        game(Match(KillerConfig(3, listOf(14, 6, 11)), players, listOf(d(14), d(6), d(6), d(6), Dart.MISS, Dart.MISS, d(11))))
    }

    @Test
    fun setup() = shot("5_setup", dark = false) {
        SetupScreen(GamePreset.X501, players, onStart = { _, _ -> }, onBack = {})
    }

    @Test
    fun shanghai() = shot("6_shanghai", dark = true) {
        game(Match(ShanghaiConfig(), players, listOf(s(1), t(1), s(1), s(1), Dart.MISS, Dart.MISS, d(1), s(1), Dart.MISS, s(2), d(2))))
    }

    @Test
    fun settings() = shot("7_settings", dark = false) {
        SettingsScreen(Settings(ThemeMode.SYSTEM, language), {}, {}, {}, {})
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun params() = AppLanguage.entries.map { arrayOf<Any>(it) }
    }
}

/** App-icoon voor de Play Store: 512×512, vierkant; Google maakt er zelf de ronde hoeken aan. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w512dp-h512dp-mdpi")
class StoreIconTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun icon() {
        compose.setContent {
            Box(Modifier.fillMaxSize()) {
                Image(painterResource(R.drawable.ic_launcher_background), null, Modifier.fillMaxSize())
                Image(painterResource(R.drawable.ic_launcher_foreground), null, Modifier.fillMaxSize())
            }
        }
        compose.waitForIdle()
        save(compose.activity, File("build/store"), "icon_512.png")
    }
}

/** Feature graphic: 1024×500, zonder transparantie (daarom JPEG). */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w1024dp-h500dp-mdpi")
class StoreFeatureGraphicTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun featureGraphic() {
        compose.setContent {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(listOf(BoardGreen, BoardGreenDeep, Color(0xFF07402A)))),
            ) {
                Dartboard(Modifier.size(620.dp).offset(x = 600.dp, y = (-60).dp))
                Row(
                    Modifier.fillMaxSize().padding(start = 64.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(132.dp).clip(RoundedCornerShape(32.dp))) {
                        Image(painterResource(R.drawable.ic_launcher_background), null, Modifier.fillMaxSize())
                        Image(painterResource(R.drawable.ic_launcher_foreground), null, Modifier.fillMaxSize())
                    }
                    Column(Modifier.padding(start = 32.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(STORE_NAME, style = TextStyle(fontFamily = DisplayFont, fontWeight = FontWeight.Bold, fontSize = 64.sp, color = Color.White))
                        Text(
                            "501 · 301 · Cricket · Killer · Shanghai",
                            style = TextStyle(fontFamily = TextFont, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, color = Color.White.copy(alpha = 0.9f)),
                        )
                    }
                }
            }
        }
        compose.waitForIdle()
        save(compose.activity, File("build/store"), "feature_graphic_1024x500.jpg", jpeg = true)
    }

    /** Gestileerd dartbord: 20 segmenten met rode en groene dubbel- en triple-ringen. */
    @Composable
    private fun Dartboard(modifier: Modifier) {
        val dark = Color(0xFF14161A)
        val light = Color(0xFFEFE6D2)
        Canvas(modifier) {
            val c = center
            val r = size.minDimension / 2
            drawCircle(dark, r)
            val sweep = 360f / 20
            fun ring(outer: Float, inner: Float, colors: (Int) -> Color) {
                for (i in 0 until 20) {
                    val start = -90f - sweep / 2 + i * sweep
                    val w = outer - inner
                    val rr = inner + w / 2
                    drawArc(
                        colors(i), start, sweep, useCenter = false,
                        topLeft = Offset(c.x - rr, c.y - rr), size = Size(rr * 2, rr * 2), style = Stroke(w),
                    )
                }
            }
            val board = r * 0.86f
            ring(board, board * 0.0f + 1f) { if (it % 2 == 0) dark else light }
            ring(board, board * 0.92f) { if (it % 2 == 0) BoardRed else BoardGreen }
            ring(board * 0.6f, board * 0.53f) { if (it % 2 == 0) BoardRed else BoardGreen }
            drawCircle(BoardGreen, board * 0.09f, c)
            drawCircle(BoardRed, board * 0.04f, c)
            // dunne draadjes tussen de segmenten
            for (i in 0 until 20) {
                val a = ((-90f - sweep / 2 + i * sweep) * PI / 180).toFloat()
                drawLine(Color(0x55FFFFFF), c, Offset(c.x + board * cos(a), c.y + board * sin(a)), 1.5f)
            }
        }
    }
}

private fun save(activity: ComponentActivity, dir: File, name: String, jpeg: Boolean = false) {
    val view = activity.window.decorView
    val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
    view.draw(Canvas(bitmap))
    dir.mkdirs()
    File(dir, name).outputStream().use {
        bitmap.compress(if (jpeg) Bitmap.CompressFormat.JPEG else Bitmap.CompressFormat.PNG, if (jpeg) 95 else 100, it)
    }
}

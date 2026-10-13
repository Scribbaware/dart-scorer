package com.thescrib.dartscorer.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Locale

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** Taal van de app; [tag] is de BCP-47-code voor de resources, [nativeName] de naam in die taal zelf. */
enum class AppLanguage(val tag: String, val nativeName: String) {
    ENGLISH("en", "English"),
    DUTCH("nl", "Nederlands"),
    GERMAN("de", "Deutsch"),
    FRENCH("fr", "Français"),
    SPANISH("es", "Español");

    companion object {
        /** Zolang je niets kiest: de taal van de telefoon als de app die kent, anders Engels. */
        fun fromSystem(locale: Locale = Locale.getDefault()): AppLanguage =
            entries.firstOrNull { it.tag == locale.language } ?: ENGLISH
    }
}

data class Settings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: AppLanguage = AppLanguage.fromSystem(),
    val keepScreenOn: Boolean = true,
)

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    private object Keys {
        val theme = stringPreferencesKey("theme")
        val language = stringPreferencesKey("language")
        val keepScreenOn = booleanPreferencesKey("keep_screen_on")
        val players = stringPreferencesKey("players")
        val match = stringPreferencesKey("match")
    }

    val settings: Flow<Settings> = context.dataStore.data.map { p ->
        val defaults = Settings()
        Settings(
            themeMode = p[Keys.theme]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: defaults.themeMode,
            language = p[Keys.language]?.let { runCatching { AppLanguage.valueOf(it) }.getOrNull() } ?: defaults.language,
            keepScreenOn = p[Keys.keepScreenOn] ?: defaults.keepScreenOn,
        )
    }

    suspend fun setThemeMode(value: ThemeMode) = set(Keys.theme, value.name)
    suspend fun setLanguage(value: AppLanguage) = set(Keys.language, value.name)
    suspend fun setKeepScreenOn(value: Boolean) = set(Keys.keepScreenOn, value)

    /** Namen van de laatst gebruikte spelers, zodat je ze niet elke keer opnieuw hoeft te typen. */
    suspend fun lastPlayers(): List<String> =
        context.dataStore.data.first()[Keys.players]?.split("\n")?.filter { it.isNotBlank() }.orEmpty()

    suspend fun setLastPlayers(names: List<String>) = set(Keys.players, names.joinToString("\n"))

    /** Het lopende spel als tekst (zie MatchCodec), of null. */
    suspend fun savedMatch(): String? = context.dataStore.data.first()[Keys.match]

    suspend fun setSavedMatch(value: String?) {
        context.dataStore.edit { if (value == null) it.remove(Keys.match) else it[Keys.match] = value }
    }

    private suspend fun <T> set(key: Preferences.Key<T>, value: T) {
        context.dataStore.edit { it[key] = value }
    }
}

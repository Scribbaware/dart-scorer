package com.thescrib.dartscorer

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import com.thescrib.dartscorer.data.AppLanguage
import java.util.Locale

val AppLanguage.locale: Locale
    get() = when (this) {
        AppLanguage.ENGLISH -> Locale.US
        AppLanguage.DUTCH -> Locale("nl", "NL")
        AppLanguage.GERMAN -> Locale.GERMANY
        AppLanguage.FRENCH -> Locale.FRANCE
        AppLanguage.SPANISH -> Locale("es", "ES")
    }

/**
 * Geeft een context terug waarvan de teksten in [language] zijn, los van de taal van de telefoon.
 * Het blijft een wrapper om de oorspronkelijke context, zodat al het andere gewoon werkt.
 */
fun Context.withLanguage(language: AppLanguage, base: Configuration = resources.configuration): Context {
    val localized = resourcesIn(language, base)
    return object : ContextWrapper(this) {
        override fun getResources(): Resources = localized
    }
}

/** Resources met de teksten in [language]; de rest van [base] (thema, schermgrootte) blijft gelijk. */
fun Context.resourcesIn(language: AppLanguage, base: Configuration): Resources =
    createConfigurationContext(Configuration(base).apply { setLocale(language.locale) }).resources

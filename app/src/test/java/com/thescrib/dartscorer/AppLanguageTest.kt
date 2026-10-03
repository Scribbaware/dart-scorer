package com.thescrib.dartscorer

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * De telefoon staat op Nederlands en de app volgt dat zolang je zelf niets kiest.
 * Vensters zoals dialogen krijgen een eigen scherm; ook daarin moeten de teksten in de app-taal zijn.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "nl-rNL")
class AppLanguageTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun followsDutchPhoneAndDialogsToo() {
        compose.onNodeWithText("Kies een spel".uppercase()).assertExists()
        compose.onNodeWithContentDescription("Trakteer op een koffie").performClick()
        compose.onNodeWithText("Naar Ko-fi").assertExists()
        compose.onNodeWithText("Sluiten").assertExists()
    }
}

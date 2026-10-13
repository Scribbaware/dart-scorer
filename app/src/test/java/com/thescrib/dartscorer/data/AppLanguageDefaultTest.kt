package com.thescrib.dartscorer.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class AppLanguageDefaultTest {
    @Test
    fun followsPhoneLanguageWhenKnown() {
        assertEquals(AppLanguage.DUTCH, AppLanguage.fromSystem(Locale("nl", "BE")))
        assertEquals(AppLanguage.GERMAN, AppLanguage.fromSystem(Locale.GERMANY))
        assertEquals(AppLanguage.FRENCH, AppLanguage.fromSystem(Locale.CANADA_FRENCH))
        assertEquals(AppLanguage.SPANISH, AppLanguage.fromSystem(Locale("es", "MX")))
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromSystem(Locale.JAPAN))
    }
}

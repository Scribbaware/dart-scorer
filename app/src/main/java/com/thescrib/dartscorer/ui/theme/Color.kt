package com.thescrib.dartscorer.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Merk: de kleuren van een dartbord. Groen en rood voor dubbel/triple, warm oranje voor Ko-fi.
val BoardGreen = Color(0xFF14A05A)
val BoardGreenDeep = Color(0xFF0B7A43)
val BoardRed = Color(0xFFE5383B)
val BoardRedDeep = Color(0xFFB71F2B)
val Gold = Color(0xFFF2B33D)
val EmberBright = Color(0xFFFF7A3D)
val Crimson = Color(0xFFE8384F)
val Azure = Color(0xFF4C8DFF)
val Violet = Color(0xFFA06BFF)

val LightColors = lightColorScheme(
    primary = BoardGreenDeep,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD3F3E2),
    onPrimaryContainer = Color(0xFF00391D),
    secondary = BoardRedDeep,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDDDD),
    onSecondaryContainer = Color(0xFF410008),
    tertiary = Color(0xFF8A5A00),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE8BF),
    onTertiaryContainer = Color(0xFF2B1A00),
    background = Color(0xFFF5F5F7),
    onBackground = Color(0xFF111217),
    surface = Color(0xFFF5F5F7),
    onSurface = Color(0xFF111217),
    surfaceVariant = Color(0xFFE9EAEE),
    onSurfaceVariant = Color(0xFF5E6270),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFBFBFC),
    surfaceContainer = Color.White,
    surfaceContainerHigh = Color(0xFFEEEEF2),
    surfaceContainerHighest = Color(0xFFE6E7EC),
    outline = Color(0xFFC9CBD3),
    outlineVariant = Color(0xFFE3E4E9),
    error = Color(0xFFD92D3A),
    inverseSurface = Color(0xFF1E2027),
    inverseOnSurface = Color(0xFFF2F2F5),
)

val DarkColors = darkColorScheme(
    primary = Color(0xFF3DD68C),
    onPrimary = Color(0xFF00210F),
    primaryContainer = Color(0xFF0F3322),
    onPrimaryContainer = Color(0xFFB8F2D3),
    secondary = Color(0xFFFF6B6E),
    onSecondary = Color(0xFF3F0008),
    secondaryContainer = Color(0xFF3B1418),
    onSecondaryContainer = Color(0xFFFFD9DA),
    tertiary = Gold,
    onTertiary = Color(0xFF2B1A00),
    tertiaryContainer = Color(0xFF3A2A0A),
    onTertiaryContainer = Color(0xFFFFE3B0),
    background = Color(0xFF0C0D10),
    onBackground = Color(0xFFF2F2F5),
    surface = Color(0xFF0C0D10),
    onSurface = Color(0xFFF2F2F5),
    surfaceVariant = Color(0xFF1E2027),
    onSurfaceVariant = Color(0xFF9A9DAA),
    surfaceContainerLowest = Color(0xFF08090B),
    surfaceContainerLow = Color(0xFF121318),
    surfaceContainer = Color(0xFF16171D),
    surfaceContainerHigh = Color(0xFF1E2027),
    surfaceContainerHighest = Color(0xFF262830),
    outline = Color(0xFF3A3D48),
    outlineVariant = Color(0xFF25272F),
    error = Color(0xFFFF5A64),
    inverseSurface = Color(0xFFF2F2F5),
    inverseOnSurface = Color(0xFF111217),
)

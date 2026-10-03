package com.thescrib.dartscorer.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.thescrib.dartscorer.R

@OptIn(ExperimentalTextApi::class)
private fun variableFamily(res: Int, vararg weights: Int) = FontFamily(
    weights.map { w ->
        Font(res, FontWeight(w), variationSettings = FontVariation.Settings(FontVariation.weight(w)))
    },
)

/** Koppen en grote cijfers: Space Grotesk (strak, technisch). */
val DisplayFont = variableFamily(R.font.space_grotesk, 400, 500, 600, 700)

/** Lopende tekst: Inter (heel goed leesbaar op kleine formaten). */
val TextFont = variableFamily(R.font.inter, 400, 500, 600, 700)

/** Cijfers die even breed zijn, zodat scores niet "wiebelen" als ze veranderen. */
const val TABULAR = "tnum"

private val base = Typography()

val AppTypography = Typography(
    displayLarge = base.displayLarge.copy(fontFamily = DisplayFont, fontWeight = FontWeight.Bold, letterSpacing = (-0.02).em),
    displayMedium = base.displayMedium.copy(fontFamily = DisplayFont, fontWeight = FontWeight.Bold, letterSpacing = (-0.02).em),
    displaySmall = base.displaySmall.copy(fontFamily = DisplayFont, fontWeight = FontWeight.Bold, letterSpacing = (-0.01).em),
    headlineLarge = base.headlineLarge.copy(fontFamily = DisplayFont, fontWeight = FontWeight.Bold, letterSpacing = (-0.01).em),
    headlineMedium = base.headlineMedium.copy(fontFamily = DisplayFont, fontWeight = FontWeight.Bold),
    headlineSmall = base.headlineSmall.copy(fontFamily = DisplayFont, fontWeight = FontWeight.SemiBold),
    titleLarge = base.titleLarge.copy(fontFamily = DisplayFont, fontWeight = FontWeight.SemiBold),
    titleMedium = base.titleMedium.copy(fontFamily = TextFont, fontWeight = FontWeight.SemiBold),
    titleSmall = base.titleSmall.copy(fontFamily = TextFont, fontWeight = FontWeight.SemiBold),
    bodyLarge = base.bodyLarge.copy(fontFamily = TextFont),
    bodyMedium = base.bodyMedium.copy(fontFamily = TextFont),
    bodySmall = base.bodySmall.copy(fontFamily = TextFont),
    labelLarge = base.labelLarge.copy(fontFamily = TextFont, fontWeight = FontWeight.SemiBold),
    labelMedium = base.labelMedium.copy(fontFamily = TextFont, fontWeight = FontWeight.SemiBold),
    labelSmall = base.labelSmall.copy(fontFamily = TextFont, fontWeight = FontWeight.SemiBold, letterSpacing = 0.08.em),
)

/** Grote resterende score bij 501/301. */
val ScoreDigits = TextStyle(
    fontFamily = DisplayFont,
    fontWeight = FontWeight.Bold,
    fontSize = 44.sp,
    letterSpacing = (-0.02).em,
    fontFeatureSettings = TABULAR,
)

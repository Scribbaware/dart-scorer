package com.thescrib.dartscorer.ui.components

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Adjust
import androidx.compose.material.icons.rounded.GridOn
import androidx.compose.material.icons.rounded.HeartBroken
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.thescrib.dartscorer.R
import com.thescrib.dartscorer.domain.AroundTheClockConfig
import com.thescrib.dartscorer.domain.CricketConfig
import com.thescrib.dartscorer.domain.GameConfig
import com.thescrib.dartscorer.domain.KillerConfig
import com.thescrib.dartscorer.domain.ShanghaiConfig
import com.thescrib.dartscorer.domain.X01Config
import com.thescrib.dartscorer.ui.theme.Azure
import com.thescrib.dartscorer.ui.theme.BoardGreen
import com.thescrib.dartscorer.ui.theme.BoardRed
import com.thescrib.dartscorer.ui.theme.EmberBright
import com.thescrib.dartscorer.ui.theme.Gold
import com.thescrib.dartscorer.ui.theme.Violet

/** De spellen op het startscherm, met hun naam, uitleg, icoon en kleur. */
enum class GamePreset(
    @StringRes val title: Int,
    @StringRes val description: Int,
    @StringRes val rules: Int,
    val icon: ImageVector,
    val color: Color,
) {
    X501(R.string.game_501, R.string.game_501_desc, R.string.rules_x01, Icons.Rounded.Adjust, BoardGreen),
    X301(R.string.game_301, R.string.game_301_desc, R.string.rules_x01, Icons.Rounded.TrackChanges, BoardRed),
    CRICKET(R.string.game_cricket, R.string.game_cricket_desc, R.string.rules_cricket, Icons.Rounded.GridOn, Azure),
    AROUND_THE_CLOCK(R.string.game_clock, R.string.game_clock_desc, R.string.rules_clock, Icons.Rounded.Schedule, Gold),
    SHANGHAI(R.string.game_shanghai, R.string.game_shanghai_desc, R.string.rules_shanghai, Icons.Rounded.LocalFireDepartment, EmberBright),
    KILLER(R.string.game_killer, R.string.game_killer_desc, R.string.rules_killer, Icons.Rounded.HeartBroken, Violet),
    ;

    /** Standaardinstellingen van dit spel; Killer krijgt de nummers pas bij de start. */
    fun defaultConfig(players: Int): GameConfig = when (this) {
        X501 -> X01Config(start = 501)
        X301 -> X01Config(start = 301)
        CRICKET -> CricketConfig
        AROUND_THE_CLOCK -> AroundTheClockConfig()
        SHANGHAI -> ShanghaiConfig()
        KILLER -> KillerConfig.random(players)
    }

    companion object {
        /** Welk spel hoort bij deze instellingen (voor titel, icoon en kleur). */
        fun of(config: GameConfig): GamePreset = when (config) {
            is X01Config -> if (config.start == 301) X301 else X501
            CricketConfig -> CRICKET
            is AroundTheClockConfig -> AROUND_THE_CLOCK
            is ShanghaiConfig -> SHANGHAI
            is KillerConfig -> KILLER
        }
    }
}

/** Titel van een lopend spel: bij X01 het startgetal (101, 701 …), anders de naam van het spel. */
@Composable
fun gameTitle(config: GameConfig): String =
    if (config is X01Config) config.start.toString() else stringResource(GamePreset.of(config).title)

package com.thescrib.dartscorer.ui.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.thescrib.dartscorer.R
import com.thescrib.dartscorer.domain.AroundTheClockConfig
import com.thescrib.dartscorer.domain.GameConfig
import com.thescrib.dartscorer.domain.KillerConfig
import com.thescrib.dartscorer.domain.ShanghaiConfig
import com.thescrib.dartscorer.domain.X01Config
import com.thescrib.dartscorer.ui.components.GamePreset
import com.thescrib.dartscorer.ui.components.SectionLabel

const val MAX_PLAYERS = 8

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    preset: GamePreset,
    initialPlayers: List<String>,
    onStart: (GameConfig, List<String>) -> Unit,
    onBack: () -> Unit,
) {
    var players by rememberSaveable { mutableStateOf(initialPlayers.ifEmpty { listOf("", "") }.take(MAX_PLAYERS)) }

    // Spelopties; welke er getoond worden hangt van het spel af.
    var start by rememberSaveable { mutableIntStateOf(if (preset == GamePreset.X301) 301 else 501) }
    var doubleOut by rememberSaveable { mutableStateOf(true) }
    var doubleIn by rememberSaveable { mutableStateOf(false) }
    var legs by rememberSaveable { mutableIntStateOf(1) }
    var skip by rememberSaveable { mutableStateOf(false) }
    var rounds by rememberSaveable { mutableIntStateOf(7) }
    var lives by rememberSaveable { mutableIntStateOf(3) }

    val minPlayers = if (preset == GamePreset.KILLER) 2 else 1
    val defaultName = stringResource(R.string.player_default)
    val names = players.mapIndexed { i, name -> name.trim().ifEmpty { "$defaultName ${i + 1}" } }

    fun config(): GameConfig = when (preset) {
        GamePreset.X501, GamePreset.X301 -> X01Config(start, doubleIn, doubleOut, legs)
        GamePreset.AROUND_THE_CLOCK -> AroundTheClockConfig(skip)
        GamePreset.SHANGHAI -> ShanghaiConfig(rounds)
        GamePreset.KILLER -> KillerConfig.random(players.size, lives)
        GamePreset.CRICKET -> preset.defaultConfig(players.size)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(preset.title)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.back)) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.background) {
                Button(
                    onClick = { onStart(config(), names) },
                    enabled = players.size >= minPlayers,
                    colors = ButtonDefaults.buttonColors(containerColor = preset.color, contentColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp),
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                ) {
                    Icon(Icons.Rounded.PlayArrow, null, Modifier.size(ButtonDefaults.IconSize))
                    Text(stringResource(R.string.start_game), Modifier.padding(start = 8.dp), style = MaterialTheme.typography.titleMedium)
                }
            }
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SectionLabel(stringResource(R.string.players), Modifier.weight(1f))
                if (players.size > 1) {
                    TextButton(onClick = { players = players.shuffled() }) {
                        Icon(Icons.Rounded.Shuffle, null, Modifier.size(18.dp))
                        Text(stringResource(R.string.shuffle), Modifier.padding(start = 6.dp))
                    }
                }
            }
            Surface(color = MaterialTheme.colorScheme.surfaceContainer, shape = MaterialTheme.shapes.large) {
                Column(Modifier.padding(vertical = 8.dp)) {
                    players.forEachIndexed { i, name ->
                        Row(Modifier.padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(32.dp).background(preset.color.copy(alpha = 0.18f), CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("${i + 1}", style = MaterialTheme.typography.labelLarge, color = preset.color)
                            }
                            OutlinedTextField(
                                value = name,
                                onValueChange = { v -> players = players.toMutableList().also { it[i] = v.take(20) } },
                                placeholder = { Text("$defaultName ${i + 1}") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                                modifier = Modifier.weight(1f).padding(start = 12.dp),
                            )
                            IconButton(
                                onClick = { players = players.toMutableList().also { it.removeAt(i) } },
                                enabled = players.size > 1,
                            ) { Icon(Icons.Rounded.Close, stringResource(R.string.remove_player)) }
                        }
                    }
                    if (players.size < MAX_PLAYERS) {
                        TextButton(onClick = { players = players + "" }, modifier = Modifier.padding(start = 8.dp)) {
                            Icon(Icons.Rounded.PersonAdd, null, Modifier.size(18.dp))
                            Text(stringResource(R.string.add_player), Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }
            if (players.size < minPlayers) {
                Text(
                    stringResource(R.string.min_players, minPlayers),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }

            val hasOptions = preset != GamePreset.CRICKET
            if (hasOptions) {
                SectionLabel(stringResource(R.string.options), Modifier.padding(top = 16.dp))
                Surface(color = MaterialTheme.colorScheme.surfaceContainer, shape = MaterialTheme.shapes.large) {
                    Column {
                        when (preset) {
                            GamePreset.X501, GamePreset.X301 -> {
                                ChoiceRow(stringResource(R.string.start_score), listOf(101, 301, 501, 701), start) { start = it }
                                Divider()
                                SwitchRow(stringResource(R.string.double_out), stringResource(R.string.double_out_desc), doubleOut) { doubleOut = it }
                                Divider()
                                SwitchRow(stringResource(R.string.double_in), stringResource(R.string.double_in_desc), doubleIn) { doubleIn = it }
                                Divider()
                                ChoiceRow(stringResource(R.string.legs_to_win), listOf(1, 2, 3, 5), legs) { legs = it }
                            }
                            GamePreset.AROUND_THE_CLOCK ->
                                SwitchRow(stringResource(R.string.multipliers_skip), stringResource(R.string.multipliers_skip_desc), skip) { skip = it }
                            GamePreset.SHANGHAI -> ChoiceRow(stringResource(R.string.rounds), listOf(7, 10, 20), rounds) { rounds = it }
                            GamePreset.KILLER -> ChoiceRow(stringResource(R.string.lives), listOf(3, 5), lives) { lives = it }
                            GamePreset.CRICKET -> Unit
                        }
                    }
                }
            }

            SectionLabel(stringResource(R.string.how_to_play), Modifier.padding(top = 16.dp))
            Surface(color = MaterialTheme.colorScheme.surfaceContainer, shape = MaterialTheme.shapes.large) {
                Text(
                    stringResource(preset.rules),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
}

@Composable
private fun ChoiceRow(title: String, options: List<Int>, selected: Int, onSelect: (Int) -> Unit) {
    Column(Modifier.padding(16.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 12.dp))
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            options.forEachIndexed { i, value ->
                SegmentedButton(
                    selected = selected == value,
                    onClick = { onSelect(value) },
                    shape = SegmentedButtonDefaults.itemShape(i, options.size),
                    icon = {},
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                ) { Text("$value") }
            }
        }
    }
}

@Composable
private fun SwitchRow(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Surface(onClick = { onChange(!checked) }, color = Color.Transparent) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f).padding(end = 14.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = checked, onCheckedChange = onChange)
        }
    }
}

@Composable
private fun Divider() {
    HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
}

package com.thescrib.dartscorer.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.thescrib.dartscorer.R
import com.thescrib.dartscorer.domain.Dart
import com.thescrib.dartscorer.domain.Match
import com.thescrib.dartscorer.domain.ShanghaiState
import com.thescrib.dartscorer.domain.X01Config
import com.thescrib.dartscorer.domain.X01State
import com.thescrib.dartscorer.domain.checkout
import com.thescrib.dartscorer.ui.components.gameTitle
import com.thescrib.dartscorer.ui.theme.BoardGreen
import com.thescrib.dartscorer.ui.theme.EmberBright
import com.thescrib.dartscorer.ui.theme.Gold
import com.thescrib.dartscorer.ui.theme.TABULAR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    match: Match,
    onDart: (Dart) -> Unit,
    onUndo: () -> Unit,
    onRestart: () -> Unit,
    onQuit: () -> Unit,
    onBack: () -> Unit,
) {
    val state = match.state
    var confirmRestart by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(gameTitle(match.config))
                        Text(subtitle(match), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.back)) } },
                actions = {
                    IconButton(onClick = { confirmRestart = true }) { Icon(Icons.Rounded.Replay, stringResource(R.string.restart)) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            Scoreboard(
                match,
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
            TurnStrip(match, Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            DartPad(
                onDart = onDart,
                onUndo = onUndo,
                canUndo = match.canUndo,
                enabled = !state.isOver,
                modifier = Modifier.navigationBarsPadding().padding(bottom = 8.dp),
            )
        }
    }

    if (state.isOver) {
        WinnerDialog(match, onUndo = onUndo, onRematch = onRestart, onHome = onQuit)
    }

    if (confirmRestart) {
        AlertDialog(
            onDismissRequest = { confirmRestart = false },
            icon = { Icon(Icons.Rounded.Replay, null) },
            title = { Text(stringResource(R.string.restart_title)) },
            text = { Text(stringResource(R.string.restart_message)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmRestart = false
                    onRestart()
                }) { Text(stringResource(R.string.restart)) }
            },
            dismissButton = { TextButton(onClick = { confirmRestart = false }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}

@Composable
private fun subtitle(match: Match): String {
    val state = match.state
    val round = stringResource(R.string.round, state.turn.round)
    return when (state) {
        is X01State -> if ((match.config as X01Config).legs > 1) stringResource(R.string.leg, state.leg) + " · " + round else round
        is ShanghaiState -> stringResource(R.string.round_of, state.target, state.rounds)
        else -> round
    }
}

/** Wie er gooit, de pijlen van deze beurt en (bij X01) een uitgooi-advies. */
@Composable
private fun TurnStrip(match: Match, modifier: Modifier = Modifier) {
    val state = match.state
    val turn = state.turn
    Surface(color = MaterialTheme.colorScheme.surfaceContainerHigh, shape = MaterialTheme.shapes.large, modifier = modifier) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    match.players[turn.current],
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                for (i in 0 until 3) {
                    val dart = turn.darts.getOrNull(i)
                    Box(
                        Modifier
                            .padding(start = 6.dp)
                            .size(width = 54.dp, height = 36.dp)
                            .background(
                                if (dart != null) MaterialTheme.colorScheme.surfaceContainerLowest else MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(10.dp),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            dart?.label ?: "",
                            style = MaterialTheme.typography.titleSmall.copy(fontFeatureSettings = TABULAR),
                        )
                    }
                }
                // Het beurttotaal zegt alleen iets bij 501/301; bij de andere spellen tellen niet alle pijlen.
                if (state is X01State) {
                    Text(
                        "${turn.darts.sumOf { it.score }}",
                        style = MaterialTheme.typography.titleLarge.copy(fontFeatureSettings = TABULAR),
                        textAlign = TextAlign.End,
                        modifier = Modifier.padding(start = 10.dp).size(width = 44.dp, height = 32.dp),
                    )
                }
            }
            if (state is X01State && !state.isOver) {
                val config = match.config as X01Config
                val route = if (state.current.opened) checkout(state.current.remaining, turn.dartsLeft, config.doubleOut) else null
                if (route != null) {
                    Text(
                        stringResource(R.string.checkout, route.joinToString("  ") { it.label }),
                        style = MaterialTheme.typography.labelLarge,
                        color = BoardGreen,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun WinnerDialog(match: Match, onUndo: () -> Unit, onRematch: () -> Unit, onHome: () -> Unit) {
    val winners = match.state.winners.map { match.players[it] }
    // Sluiten door ernaast te tikken doet niets: je kiest bewust een van de knoppen.
    Dialog(onDismissRequest = {}) {
        Surface(color = MaterialTheme.colorScheme.surfaceContainer, shape = RoundedCornerShape(28.dp)) {
            Column(
                Modifier.padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    Modifier.size(72.dp).background(Brush.linearGradient(listOf(Gold, EmberBright)), RoundedCornerShape(22.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.EmojiEvents, null, tint = Color.White, modifier = Modifier.size(40.dp))
                }
                Text(
                    if (winners.size == 1) stringResource(R.string.winner, winners[0]) else stringResource(R.string.draw, winners.joinToString(" & ")),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                )
                val shanghai = (match.state as? ShanghaiState)?.shanghai == true
                if (shanghai) {
                    Text(stringResource(R.string.shanghai_win), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(onClick = onRematch, modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(48.dp)) {
                    Icon(Icons.Rounded.Replay, null, Modifier.size(18.dp))
                    Text(stringResource(R.string.rematch), Modifier.padding(start = 8.dp))
                }
                OutlinedButton(onClick = onHome, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                    Text(stringResource(R.string.home))
                }
                TextButton(onClick = onUndo, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.AutoMirrored.Rounded.Undo, null, Modifier.size(18.dp))
                    Text(stringResource(R.string.undo_last_dart), Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}

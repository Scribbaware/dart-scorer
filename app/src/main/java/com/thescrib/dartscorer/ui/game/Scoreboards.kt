package com.thescrib.dartscorer.ui.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.thescrib.dartscorer.R
import com.thescrib.dartscorer.domain.CLOCK_TARGETS
import com.thescrib.dartscorer.domain.CRICKET_NUMBERS
import com.thescrib.dartscorer.domain.ClockState
import com.thescrib.dartscorer.domain.CricketState
import com.thescrib.dartscorer.domain.Dart
import com.thescrib.dartscorer.domain.GameState
import com.thescrib.dartscorer.domain.KillerConfig
import com.thescrib.dartscorer.domain.KillerState
import com.thescrib.dartscorer.domain.Match
import com.thescrib.dartscorer.domain.ShanghaiState
import com.thescrib.dartscorer.domain.X01Config
import com.thescrib.dartscorer.domain.X01State
import com.thescrib.dartscorer.ui.theme.BoardRed
import com.thescrib.dartscorer.ui.theme.Gold
import com.thescrib.dartscorer.ui.theme.ScoreDigits
import com.thescrib.dartscorer.ui.theme.TABULAR
import java.util.Locale

/** Het scorebord dat bij het spel hoort. */
@Composable
fun Scoreboard(match: Match, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        when (val state = match.state) {
            is X01State -> X01Board(match, state)
            is CricketState -> CricketBoard(match, state)
            is ClockState -> ClockBoard(match, state)
            is ShanghaiState -> ShanghaiBoard(match, state)
            is KillerState -> KillerBoard(match, state)
        }
    }
}

private fun GameState.isActive(player: Int) = !isOver && turn.current == player

/** Kaart per speler; de speler aan de beurt krijgt een gekleurde rand. */
@Composable
private fun PlayerCard(
    name: String,
    active: Boolean,
    winner: Boolean,
    modifier: Modifier = Modifier,
    out: Boolean = false,
    trailing: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit = {},
) {
    val shape = MaterialTheme.shapes.large
    val borderColor = when {
        winner -> Gold
        active -> MaterialTheme.colorScheme.primary
        else -> Color.Transparent
    }
    Surface(
        color = if (active) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f) else MaterialTheme.colorScheme.surfaceContainer,
        shape = shape,
        modifier = modifier
            .fillMaxWidth()
            .border(2.dp, borderColor, shape)
            .alpha(if (out) 0.45f else 1f),
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (out) TextDecoration.LineThrough else null,
                )
                content()
            }
            trailing()
        }
    }
}

@Composable
private fun Detail(text: String, color: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Text(text, style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = TABULAR), color = color)
}

@Composable
private fun X01Board(match: Match, state: X01State) {
    val config = match.config as X01Config
    state.players.forEachIndexed { i, p ->
        PlayerCard(
            name = match.players[i],
            active = state.isActive(i),
            winner = i in state.winners,
            trailing = {
                Text(
                    "${p.remaining}",
                    style = ScoreDigits,
                    color = if (state.isActive(i)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                )
            },
        ) {
            val parts = buildList {
                if (config.legs > 1) add(stringResource(R.string.legs_won, p.legsWon))
                add(stringResource(R.string.average, String.format(Locale.ROOT, "%.1f", p.average)))
                add(stringResource(R.string.darts_thrown, p.dartsThrown))
            }
            Detail(parts.joinToString(" · "))
            if (p.lastVisit.isNotEmpty()) {
                val visit = p.lastVisit.joinToString(" ") { it.label }
                if (p.lastVisitBust) {
                    Detail(stringResource(R.string.last_visit_bust, visit), BoardRed)
                } else {
                    Detail(stringResource(R.string.last_visit, p.lastVisit.sumOf { it.score }, visit))
                }
            }
            if (config.doubleIn && !p.opened) Detail(stringResource(R.string.needs_double_in), BoardRed)
        }
    }
}

@Composable
private fun CricketBoard(match: Match, state: CricketState) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainer, shape = MaterialTheme.shapes.large) {
        Column(Modifier.padding(vertical = 8.dp)) {
            // Kop: spelersnamen, de speler aan de beurt gemarkeerd.
            Row(Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(52.dp))
                match.players.forEachIndexed { i, name ->
                    val active = state.isActive(i)
                    Text(
                        name,
                        style = MaterialTheme.typography.labelLarge,
                        color = when {
                            i in state.winners -> Gold
                            active -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (active) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                RoundedCornerShape(8.dp),
                            )
                            .padding(vertical = 6.dp, horizontal = 2.dp),
                    )
                }
            }
            CRICKET_NUMBERS.forEachIndexed { row, number ->
                val closedByAll = state.players.all { it.marks[row] >= 3 }
                Row(
                    Modifier.padding(horizontal = 8.dp).height(40.dp).alpha(if (closedByAll) 0.35f else 1f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        if (number == Dart.BULL) stringResource(R.string.bull) else "$number",
                        style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = TABULAR),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(52.dp),
                    )
                    state.players.forEach { p ->
                        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { Marks(p.marks[row]) }
                    }
                }
            }
            Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.points_short),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(52.dp),
                )
                state.players.forEach { p ->
                    Text(
                        "${p.points}",
                        style = MaterialTheme.typography.headlineSmall.copy(fontFeatureSettings = TABULAR),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

/** Klassieke cricket-tekens: / bij 1 hit, X bij 2, een omcirkelde X als het nummer dicht is. */
@Composable
private fun Marks(count: Int) {
    val color = if (count >= 3) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    Canvas(Modifier.size(26.dp)) {
        val w = size.width
        val stroke = w * 0.11f
        val inset = w * 0.22f
        if (count >= 1) drawLine(color, Offset(w - inset, inset), Offset(inset, w - inset), stroke, StrokeCap.Round)
        if (count >= 2) drawLine(color, Offset(inset, inset), Offset(w - inset, w - inset), stroke, StrokeCap.Round)
        if (count >= 3) drawCircle(color, radius = w / 2 - stroke / 2, style = Stroke(stroke))
    }
}

@Composable
private fun ClockBoard(match: Match, state: ClockState) {
    match.players.forEachIndexed { i, name ->
        val done = state.progress[i] >= CLOCK_TARGETS.size
        val target = state.target(i)
        PlayerCard(
            name = name,
            active = state.isActive(i),
            winner = i in state.winners,
            trailing = {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        stringResource(R.string.next_target).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        when {
                            done -> "✓"
                            target == Dart.BULL -> stringResource(R.string.bull)
                            else -> "$target"
                        },
                        style = ScoreDigits,
                        color = if (state.isActive(i)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    )
                }
            },
        ) {
            LinearProgressIndicator(
                progress = { state.progress[i].toFloat() / CLOCK_TARGETS.size },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, end = 16.dp),
                drawStopIndicator = {},
            )
        }
    }
}

@Composable
private fun ShanghaiBoard(match: Match, state: ShanghaiState) {
    Surface(color = MaterialTheme.colorScheme.tertiaryContainer, shape = MaterialTheme.shapes.large) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.round_of, state.target, state.rounds), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.shanghai_hint), style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(stringResource(R.string.target).uppercase(), style = MaterialTheme.typography.labelSmall)
                Text("${state.target}", style = ScoreDigits)
            }
        }
    }
    match.players.forEachIndexed { i, name ->
        PlayerCard(
            name = name,
            active = state.isActive(i),
            winner = i in state.winners,
            trailing = { Text("${state.scores[i]}", style = ScoreDigits) },
        )
    }
}

@Composable
private fun KillerBoard(match: Match, state: KillerState) {
    state.players.forEachIndexed { i, p ->
        PlayerCard(
            name = match.players[i],
            active = state.isActive(i),
            winner = i in state.winners,
            out = !p.isAlive,
            trailing = {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        stringResource(R.string.your_double).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text("D${p.number}", style = ScoreDigits)
                }
            },
        ) {
            Row(Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                val total = (match.config as KillerConfig).lives
                repeat(total) { n ->
                    Icon(
                        if (n < p.lives) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        null,
                        tint = BoardRed,
                        modifier = Modifier.size(18.dp),
                    )
                }
                if (p.isKiller && p.isAlive) {
                    Surface(
                        color = BoardRed,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.padding(start = 8.dp),
                    ) {
                        Row(Modifier.padding(horizontal = 8.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Bolt, null, Modifier.size(14.dp))
                            Text(stringResource(R.string.killer_badge), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

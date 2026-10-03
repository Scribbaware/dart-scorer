package com.thescrib.dartscorer.ui.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.thescrib.dartscorer.R
import com.thescrib.dartscorer.domain.Dart
import com.thescrib.dartscorer.ui.theme.BoardGreen
import com.thescrib.dartscorer.ui.theme.BoardRed
import com.thescrib.dartscorer.ui.theme.TABULAR

/**
 * Invoer per pijl: kies eerst eventueel Dubbel of Triple, tik dan het nummer.
 * Na elke pijl springt de keuze terug naar Enkel, want dat gooi je het vaakst.
 */
@Composable
fun DartPad(
    onDart: (Dart) -> Unit,
    onUndo: () -> Unit,
    canUndo: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    var multiplier by rememberSaveable { mutableIntStateOf(1) }
    val throwDart: (Dart) -> Unit = {
        onDart(it)
        multiplier = 1
    }
    val accent = when (multiplier) {
        2 -> BoardGreen
        3 -> BoardRed
        else -> MaterialTheme.colorScheme.onSurface
    }
    val prefix = when (multiplier) {
        2 -> "D"
        3 -> "T"
        else -> ""
    }

    Column(modifier.padding(horizontal = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ModeKey(stringResource(R.string.single), multiplier == 1, MaterialTheme.colorScheme.onSurface) { multiplier = 1 }
            ModeKey(stringResource(R.string.double_), multiplier == 2, BoardGreen) { multiplier = if (multiplier == 2) 1 else 2 }
            ModeKey(stringResource(R.string.treble), multiplier == 3, BoardRed) { multiplier = if (multiplier == 3) 1 else 3 }
        }
        for (row in 0 until 4) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (col in 1..5) {
                    val n = row * 5 + col
                    Key("$prefix$n", accent, enabled) { throwDart(Dart(n, multiplier)) }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            val bull = multiplier == 2
            Key(
                if (bull) "BULL" else "25",
                if (bull) BoardRed else BoardGreen,
                enabled && multiplier != 3,
                weight = 1.5f,
            ) { throwDart(Dart(Dart.BULL, if (bull) 2 else 1)) }
            Key(stringResource(R.string.miss), MaterialTheme.colorScheme.onSurfaceVariant, enabled, weight = 1.5f) { throwDart(Dart.MISS) }
            val undo = stringResource(R.string.undo)
            KeySurface(
                enabled = canUndo,
                weight = 2f,
                onClick = onUndo,
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                modifier = Modifier.semantics { contentDescription = undo },
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Rounded.Undo, null)
                    Text(undo, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(start = 6.dp))
                }
            }
        }
    }
}

@Composable
private fun RowScope.Key(label: String, color: Color, enabled: Boolean, weight: Float = 1f, onClick: () -> Unit) {
    KeySurface(enabled, weight, onClick) {
        Text(
            label,
            style = MaterialTheme.typography.titleLarge.copy(fontFeatureSettings = TABULAR),
            color = if (enabled) color else color.copy(alpha = 0.35f),
            maxLines = 1,
        )
    }
}

@Composable
private fun RowScope.ModeKey(label: String, selected: Boolean, color: Color, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (selected) color else MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = if (selected) {
            if (color == MaterialTheme.colorScheme.onSurface) MaterialTheme.colorScheme.surface else Color.White
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        shape = MaterialTheme.shapes.small,
        modifier = Modifier
            .weight(1f)
            .height(44.dp)
            .semantics {
                role = Role.Tab
                this.selected = selected
            },
    ) {
        Box(contentAlignment = Alignment.Center) { Text(label, style = MaterialTheme.typography.labelLarge) }
    }
}

@Composable
private fun RowScope.KeySurface(
    enabled: Boolean,
    weight: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surfaceContainer,
    content: @Composable () -> Unit,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        color = color,
        shape = MaterialTheme.shapes.small,
        modifier = modifier.weight(weight).height(52.dp).fillMaxWidth(),
    ) {
        Box(contentAlignment = Alignment.Center) { content() }
    }
}

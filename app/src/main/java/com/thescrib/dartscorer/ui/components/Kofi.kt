package com.thescrib.dartscorer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.thescrib.dartscorer.R
import com.thescrib.dartscorer.ui.theme.Crimson
import com.thescrib.dartscorer.ui.theme.EmberBright

const val KOFI_URL = "https://ko-fi.com/scribba"

/** Warm verloop voor alles wat met Ko-fi te maken heeft. */
val KofiGradient = listOf(EmberBright, Crimson)

/**
 * Klein venster met uitleg en de keuze om naar Ko-fi te gaan of te sluiten.
 * De knoppen in de app openen nooit direct de website, maar altijd eerst dit venster.
 */
@Composable
fun KofiDialog(onDismiss: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    Dialog(onDismissRequest = onDismiss) {
        KofiDialogContent(
            onOpen = {
                onDismiss()
                runCatching { uriHandler.openUri(KOFI_URL) }
            },
            onDismiss = onDismiss,
        )
    }
}

/** Inhoud van het Ko-fi-venster, los van [Dialog] zodat de screenshottest hem kan tonen. */
@Composable
fun KofiDialogContent(onOpen: () -> Unit, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(28.dp),
        modifier = modifier,
    ) {
        Column(
            Modifier.padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                Modifier.size(64.dp).background(Brush.linearGradient(KofiGradient), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.LocalCafe, null, tint = Color.White, modifier = Modifier.size(34.dp))
            }
            Text(
                stringResource(R.string.kofi_title),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                stringResource(R.string.kofi_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Button(
                onClick = onOpen,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
            ) {
                Text(stringResource(R.string.kofi_open))
                Icon(
                    Icons.AutoMirrored.Rounded.OpenInNew,
                    null,
                    modifier = Modifier.padding(start = 8.dp).size(ButtonDefaults.IconSize),
                )
            }
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.close)) }
        }
    }
}

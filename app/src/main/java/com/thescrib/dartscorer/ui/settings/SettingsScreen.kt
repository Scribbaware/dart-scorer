package com.thescrib.dartscorer.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.thescrib.dartscorer.BuildConfig
import com.thescrib.dartscorer.R
import com.thescrib.dartscorer.data.AppLanguage
import com.thescrib.dartscorer.data.Settings
import com.thescrib.dartscorer.data.ThemeMode
import com.thescrib.dartscorer.ui.components.KofiDialog
import com.thescrib.dartscorer.ui.components.KofiGradient
import com.thescrib.dartscorer.ui.components.SectionLabel
import com.thescrib.dartscorer.ui.theme.EmberBright
import com.thescrib.dartscorer.ui.theme.Gold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: Settings,
    onKeepScreenOn: (Boolean) -> Unit,
    onThemeMode: (ThemeMode) -> Unit,
    onLanguage: (AppLanguage) -> Unit,
    onBack: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.back)) } },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
                scrollBehavior = scrollBehavior,
            )
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SectionLabel(stringResource(R.string.settings_game))
            Surface(color = MaterialTheme.colorScheme.surfaceContainer, shape = MaterialTheme.shapes.large) {
                ToggleRow(
                    Icons.Rounded.Lightbulb,
                    Gold,
                    stringResource(R.string.keep_screen_on),
                    stringResource(R.string.keep_screen_on_desc),
                    settings.keepScreenOn,
                    onKeepScreenOn,
                )
            }

            SectionLabel(stringResource(R.string.settings_display), Modifier.padding(top = 16.dp))
            Surface(color = MaterialTheme.colorScheme.surfaceContainer, shape = MaterialTheme.shapes.large) {
                Column {
                    Column(Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.theme), style = MaterialTheme.typography.titleMedium)
                        Text(
                            stringResource(R.string.theme_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 12.dp),
                        )
                        val options = listOf(
                            Triple(ThemeMode.SYSTEM, stringResource(R.string.theme_system), Icons.Rounded.Smartphone),
                            Triple(ThemeMode.LIGHT, stringResource(R.string.theme_light), Icons.Rounded.LightMode),
                            Triple(ThemeMode.DARK, stringResource(R.string.theme_dark), Icons.Rounded.DarkMode),
                        )
                        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                            options.forEachIndexed { i, (mode, label, icon) ->
                                SegmentedButton(
                                    selected = settings.themeMode == mode,
                                    onClick = { onThemeMode(mode) },
                                    shape = SegmentedButtonDefaults.itemShape(i, options.size),
                                    icon = { Icon(icon, null, Modifier.size(18.dp)) },
                                    colors = SegmentedButtonDefaults.colors(
                                        activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    ),
                                ) { Text(label) }
                            }
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)) {
                        Text(stringResource(R.string.language), style = MaterialTheme.typography.titleMedium)
                        Text(
                            stringResource(R.string.language_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 12.dp),
                        )
                    }
                    // Taalnamen staan altijd in hun eigen taal, zodat je ze herkent.
                    AppLanguage.entries.forEach { language ->
                        LanguageRow(language.nativeName, settings.language == language) { onLanguage(language) }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            SectionLabel(stringResource(R.string.support_app), Modifier.padding(top = 16.dp))
            var showKofi by remember { mutableStateOf(false) }
            KofiCard(onClick = { showKofi = true })
            if (showKofi) KofiDialog(onDismiss = { showKofi = false })

            Text(
                stringResource(R.string.version, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * Ko-fi-kaart: valt op door het warme verloop in het icoon en de dunne rand,
 * maar blijft rustig doordat de kaart zelf dezelfde achtergrond heeft als de rest.
 * Tikken opent eerst het Ko-fi-venster, niet direct de website.
 */
@Composable
internal fun KofiCard(onClick: () -> Unit) {
    val warm = KofiGradient
    val shape = MaterialTheme.shapes.large
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = shape,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Brush.linearGradient(warm.map { it.copy(alpha = 0.45f) }), shape)
            .semantics { role = Role.Button },
    ) {
        Row(
            Modifier
                .background(Brush.horizontalGradient(listOf(EmberBright.copy(alpha = 0.08f), Color.Transparent)))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(40.dp).background(Brush.linearGradient(warm), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.LocalCafe, null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                Text(stringResource(R.string.kofi_title), style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(R.string.kofi_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                Icons.AutoMirrored.Rounded.OpenInNew,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/** Eén taal in de lijst, met een rondje dat aangeeft welke gekozen is. */
@Composable
private fun LanguageRow(name: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null, modifier = Modifier.padding(8.dp))
        Text(name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 4.dp))
    }
}

@Composable
private fun ToggleRow(
    icon: ImageVector,
    tint: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
) {
    Surface(onClick = { onChange(!checked) }, color = Color.Transparent) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).background(tint.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
            }
            Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = checked, onCheckedChange = onChange)
        }
    }
}


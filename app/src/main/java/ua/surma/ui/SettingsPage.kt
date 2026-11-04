package ua.surma.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ua.surma.Labels
import ua.surma.R
import ua.surma.alerts.Rules
import ua.surma.ui.theme.Group
import ua.surma.ui.theme.Look
import ua.surma.ui.theme.LookSettings
import ua.surma.ui.theme.Ornament
import ua.surma.ui.theme.SectionTitle
import ua.surma.ui.theme.Segments
import ua.surma.watch.WatchSettings

/** Settings: the regions, how often to check, then language and look, and the way to "About Surma". */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsPage(
    settings: WatchSettings,
    onSettings: (WatchSettings) -> Unit,
    look: Look,
    onLook: (Look) -> Unit,
    language: AppLanguage.Choice,
    onLanguage: (AppLanguage.Choice) -> Unit,
    onAbout: () -> Unit,
    onBack: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Scaffold(
        containerColor = scheme.background,
        topBar = {
            Column(Modifier.background(scheme.background)) {
                CenterAlignedTopAppBar(
                    title = { Text(stringResource(R.string.settings), color = scheme.primary, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = stringResource(R.string.back))
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = scheme.background),
                )
                Ornament(Modifier.padding(horizontal = 16.dp, vertical = 2.dp))
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()), contentAlignment = Alignment.TopCenter) {
            Column(Modifier.widthIn(max = 640.dp).fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Regions(settings, onSettings)
                Spacer(Modifier.height(8.dp))
                Checking(settings, onSettings)
                Spacer(Modifier.height(8.dp))
                LookSettings(look, onLook, language, onLanguage)
                AboutRow(onAbout)
            }
        }
    }
}

@Composable
private fun Regions(settings: WatchSettings, onSettings: (WatchSettings) -> Unit) {
    val context = LocalContext.current
    SectionTitle(stringResource(R.string.regions_title))
    Text(stringResource(R.string.regions_note), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Group {
        Column {
            for ((i, o) in Labels.sortedOblasts(context).withIndex()) {
                if (i > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                val on = o in settings.oblasts
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 48.dp)
                        .toggleable(value = on, role = Role.Checkbox) { onSettings(settings.copy(oblasts = if (it) settings.oblasts + o else settings.oblasts - o)) }
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(checked = on, onCheckedChange = null)
                    Spacer(Modifier.width(12.dp))
                    Text(stringResource(Labels.oblast(o)), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
private fun Checking(settings: WatchSettings, onSettings: (WatchSettings) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    SectionTitle(stringResource(R.string.alerts_title))
    Group {
        Column {
            Row(
                Modifier.fillMaxWidth().heightIn(min = 56.dp)
                    .toggleable(value = settings.nationwide, role = Role.Switch) { onSettings(settings.copy(nationwide = it)) }
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(stringResource(R.string.nationwide), style = MaterialTheme.typography.bodyLarge, color = scheme.onSurface)
                    Text(stringResource(R.string.nationwide_summary), style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
                }
                Switch(checked = settings.nationwide, onCheckedChange = null)
            }
            HorizontalDivider(color = scheme.outlineVariant)
            Text(stringResource(R.string.pace_title), style = MaterialTheme.typography.bodyLarge, color = scheme.onSurface,
                modifier = Modifier.padding(start = 4.dp, top = 12.dp, bottom = 8.dp))
            Segments(options = Rules.Pace.entries.map { it to Labels.pace(it) }, chosen = settings.pace) { onSettings(settings.copy(pace = it)) }
            Text(
                stringResource(R.string.pace_summary, settings.pace.activeSeconds.toInt(), settings.pace.quietSeconds.toInt()),
                style = MaterialTheme.typography.bodyMedium, color = scheme.onSurface, modifier = Modifier.padding(start = 4.dp, top = 8.dp),
            )
            Text(stringResource(R.string.pace_note), style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 8.dp))
        }
    }
}

/** The way to "About Surma", last in Settings. */
@Composable
private fun AboutRow(onClick: () -> Unit) {
    Spacer(Modifier.height(16.dp))
    Group {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 64.dp).clickable(role = Role.Button, onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.about_surma), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Text(stringResource(R.string.about_surma_summary), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = null,
                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.graphicsLayer { rotationZ = 180f })
        }
    }
}

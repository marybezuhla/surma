package ua.surma.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ua.surma.Labels
import ua.surma.R
import ua.surma.alerts.Rules
import ua.surma.alerts.ThreatType
import ua.surma.help.Help
import ua.surma.ui.theme.CardTitle
import ua.surma.ui.theme.Ornament
import ua.surma.watch.Alerts
import ua.surma.watch.FeedItem
import ua.surma.watch.Problem
import ua.surma.watch.WatchSettings
import ua.surma.watch.WatchState

/** Everything the home screen shows, read from the phone; made-up values in the screen tests. */
data class HomeUi(
    val settings: WatchSettings,
    val state: WatchState,
    val items: List<FeedItem>,
    val canNotify: Boolean = true,
    val exactAllowed: Boolean = true,
    val batteryFree: Boolean = true,
)

class HomeActions(
    val onWatch: (Boolean) -> Unit = {},
    val onSettings: () -> Unit = {},
    val onNotifySettings: () -> Unit = {},
    val onExact: () -> Unit = {},
    val onBattery: () -> Unit = {},
    /** Opens a "what to do" card by its id. */
    val onHelp: (String) -> Unit = {},
)

/** Watching on or off and how it goes, what to fix, then the latest threats, newest first. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(ui: HomeUi, actions: HomeActions, tabs: @Composable () -> Unit = {}) {
    val scheme = MaterialTheme.colorScheme
    Scaffold(
        containerColor = scheme.background,
        bottomBar = tabs,
        topBar = {
            Column(Modifier.background(scheme.background)) {
                CenterAlignedTopAppBar(
                    title = { Text(stringResource(R.string.app_name), color = scheme.primary, fontWeight = FontWeight.Bold) },
                    actions = {
                        IconButton(onClick = actions.onSettings) {
                            Icon(painterResource(R.drawable.ic_settings), contentDescription = stringResource(R.string.settings))
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = scheme.background),
                )
                Ornament(Modifier.padding(horizontal = 16.dp, vertical = 2.dp))
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            // A readable column on a tablet or an unfolded phone
            LazyColumn(
                Modifier.widthIn(max = 720.dp).fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item { WatchCard(ui, actions) }
                if (ui.settings.enabled) item { Hints(ui, actions) }
                item {
                    Text(
                        stringResource(R.string.recent_title), style = CardTitle, color = scheme.onSurface, textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp).semantics { heading() },
                    )
                }
                if (ui.items.isEmpty()) {
                    item {
                        Text(
                            stringResource(if (ui.settings.enabled) R.string.recent_empty else R.string.recent_empty_off),
                            style = MaterialTheme.typography.bodyLarge, color = scheme.onSurfaceVariant,
                            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        )
                    }
                }
                items(ui.items, key = { "${it.postId}/${it.threat.sourceLine}/${it.threat.type}" }) { ThreatCard(it, ui.settings, actions.onHelp) }
                item {
                    Text(
                        stringResource(R.string.disclaimer), style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun WatchCard(ui: HomeUi, actions: HomeActions) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val s = ui.settings
    val st = ui.state
    val status = when {
        !s.enabled -> stringResource(R.string.status_off)
        st.problem == Problem.NETWORK -> stringResource(R.string.status_network, Alerts.time(st.lastOk.takeIf { it > 0 } ?: st.lastCheck))
        st.problem == Problem.PAGE -> stringResource(R.string.status_page, Alerts.time(st.lastOk.takeIf { it > 0 } ?: st.lastCheck))
        st.lastCheck > 0 -> stringResource(R.string.status_checked, Alerts.time(st.lastCheck))
        else -> stringResource(R.string.status_starting)
    }
    val regions = if (s.oblasts.isEmpty()) stringResource(R.string.regions_all) else stringResource(
        R.string.regions_chosen,
        Labels.sortedOblasts(context).filter { it in s.oblasts }.joinToString(", ") { context.getString(Labels.oblast(it)) },
    )
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = scheme.surface,
        // On: a thick frame in the theme's accent, so the state is clear without telling colours apart
        border = BorderStroke(if (s.enabled) 2.dp else 1.dp, if (s.enabled) scheme.primary else scheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            // The whole row is the switch, read as one by TalkBack
            Row(
                Modifier.fillMaxWidth().heightIn(min = 56.dp).toggleable(value = s.enabled, role = Role.Switch, onValueChange = actions.onWatch),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.watch_switch), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface, modifier = Modifier.weight(1f).padding(end = 12.dp),
                )
                Switch(checked = s.enabled, onCheckedChange = null)
            }
            Text(
                status, style = MaterialTheme.typography.bodyMedium,
                color = if (s.enabled && st.problem != null) scheme.error else scheme.onSurfaceVariant,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
            Text(
                regions, style = MaterialTheme.typography.bodyMedium, color = scheme.primary, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable(role = Role.Button, onClick = actions.onSettings)
                    .padding(vertical = 12.dp),
            )
        }
    }
}

/**
 * What to fix for alerts to work well. Only the most important one shows; the rest wait behind one line, so the
 * home screen is about alerts, not about warnings.
 */
@Composable
private fun Hints(ui: HomeUi, actions: HomeActions) {
    val all = buildList<@Composable () -> Unit> {
        if (!ui.canNotify) add { Hint(stringResource(R.string.notify_denied), stringResource(R.string.notify_allow), true, actions.onNotifySettings) }
        if (!ui.exactAllowed) add { Hint(stringResource(R.string.exact_hint), stringResource(R.string.exact_allow), false, actions.onExact) }
        if (!ui.batteryFree) add { Hint(stringResource(R.string.battery_hint), stringResource(R.string.battery_open), false, actions.onBattery) }
    }
    if (all.isEmpty()) return
    var more by rememberSaveable { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        all.first()()
        val rest = all.size - 1
        if (rest > 0 && !more) {
            TextButton(onClick = { more = true }, modifier = Modifier.fillMaxWidth()) {
                Text(pluralStringResource(R.plurals.hints_more, rest, rest))
            }
        }
        if (more) all.drop(1).forEach { it() }
    }
}

/** Something to fix for alerts to work well, with the button that opens the right place. */
@Composable
private fun Hint(text: String, button: String, serious: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (serious) scheme.errorContainer else scheme.surface,
        border = BorderStroke(1.dp, if (serious) scheme.error else scheme.outline),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(text, style = MaterialTheme.typography.bodyMedium, color = if (serious) scheme.onErrorContainer else scheme.onSurface)
            TextButton(onClick = onClick, contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp)) { Text(button) }
        }
    }
}

@Composable
private fun ThreatCard(item: FeedItem, settings: WatchSettings, onHelp: (String) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val t = item.threat
    val yours = settings.oblasts.isNotEmpty() && Rules.concerns(t, settings.oblasts, settings.nationwide)
    val clear = t.type == ThreatType.ALL_CLEAR
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = scheme.surface,
        border = BorderStroke(if (yours) 2.dp else 1.dp, if (yours) scheme.primary else scheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    Labels.title(context, t), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                    color = if (clear) scheme.secondary else scheme.onSurface, modifier = Modifier.weight(1f),
                )
                Text(Alerts.time(item.epochSeconds), style = MaterialTheme.typography.labelLarge, color = scheme.onSurfaceVariant)
            }
            if (yours) {
                Text(
                    stringResource(R.string.yours), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold,
                    color = scheme.onPrimary,
                    modifier = Modifier.background(scheme.primary, RoundedCornerShape(50)).padding(horizontal = 10.dp, vertical = 2.dp),
                )
            }
            val where = Labels.where(context, t)
            if (where.isNotEmpty()) Text(where, style = MaterialTheme.typography.bodyLarge, color = scheme.onSurface)
            if (t.target == null && t.unresolvedTarget != null) {
                Text(stringResource(R.string.not_on_list), style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
            }
            Spacer(Modifier.heightIn(min = 2.dp))
            // What the post said, word for word: the reading above may be wrong
            Text(
                stringResource(R.string.as_written) + " " + t.sourceLine, style = MaterialTheme.typography.bodySmall,
                fontStyle = FontStyle.Italic, color = scheme.onSurfaceVariant,
            )
            // What to do about this kind of threat, word for word from Sprotyv
            Help.cardFor(t.type)?.let { id ->
                TextButton(onClick = { onHelp(id) }, contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp)) {
                    Text(stringResource(R.string.what_to_do), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

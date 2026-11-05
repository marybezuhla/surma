package ua.surma.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ua.surma.R
import ua.surma.help.Card
import ua.surma.help.Help
import ua.surma.ui.theme.CardTitle
import ua.surma.ui.theme.Ornament

/** The title bar every top-level page shares: the name in the middle over the motif, an optional way back. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TopBar(title: String, onBack: (() -> Unit)? = null, actions: @Composable () -> Unit = {}) {
    val scheme = MaterialTheme.colorScheme
    Column(Modifier.background(scheme.background)) {
        CenterAlignedTopAppBar(
            title = { Text(title, color = scheme.primary, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) },
            navigationIcon = {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = stringResource(R.string.back))
                    }
                }
            },
            actions = { actions() },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = scheme.background),
        )
        Ornament(Modifier.padding(horizontal = 16.dp, vertical = 2.dp))
    }
}

/** "What to do": 112 first, then the cards, each one tap away. */
@Composable
internal fun HelpScreen(help: Help, onCard: (String) -> Unit, onCall: (String) -> Unit, tabs: @Composable () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Scaffold(
        containerColor = scheme.background,
        topBar = { TopBar(stringResource(R.string.tab_help)) },
        bottomBar = tabs,
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            LazyColumn(
                Modifier.widthIn(max = 720.dp).fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item { CallButton("112", onCall) }
                items(help.cards, key = { it.id }) { card ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = scheme.surface,
                        border = BorderStroke(1.dp, scheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            Modifier.fillMaxWidth().heightIn(min = 64.dp).clickable(role = Role.Button) { onCard(card.id) }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(card.title, style = CardTitle, color = scheme.onSurface)
                                Text(stringResource(R.string.steps_count, card.steps.size), style = MaterialTheme.typography.bodyMedium,
                                    color = scheme.onSurfaceVariant)
                            }
                            Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = null, tint = scheme.primary,
                                modifier = Modifier.padding(start = 8.dp).size(24.dp).graphicsLayer { rotationZ = 180f })
                        }
                    }
                }
                item {
                    Text(stringResource(R.string.help_from), style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
                }
            }
        }
    }
}

/** One card, all steps at once: under stress a page that just scrolls is easiest; "why" sits quieter under each step. */
@Composable
internal fun CardPage(card: Card, help: Help, onBack: () -> Unit, onCall: (String) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Scaffold(containerColor = scheme.background, topBar = { TopBar(card.title, onBack) }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()), contentAlignment = Alignment.TopCenter) {
            Column(Modifier.widthIn(max = 720.dp).fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (card.call.isNotEmpty()) CallButton(card.call, onCall)
                card.steps.forEachIndexed { i, s ->
                    Row(verticalAlignment = Alignment.Top) {
                        StepNumber(i + 1)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(s.action, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = scheme.onSurface)
                            if (s.why.isNotBlank()) Text(s.why, style = MaterialTheme.typography.bodyLarge, color = scheme.onSurfaceVariant)
                            help.figures[s.figure]?.let { f ->
                                f.pictures.forEach { FigureImage(it, f.caption) }
                                if (f.caption.isNotBlank()) Text(f.caption, style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
                            }
                        }
                        pictogram(s.icon)?.let {
                            Icon(painterResource(it), contentDescription = null, tint = scheme.onSurface, modifier = Modifier.padding(start = 8.dp).size(44.dp))
                        }
                    }
                    if (i < card.steps.lastIndex) HorizontalDivider(color = scheme.outlineVariant)
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(stringResource(R.string.source_line, card.source), style = MaterialTheme.typography.bodyMedium, color = scheme.onSurface)
                    Text(stringResource(R.string.checked_on, card.checkedOn), style = MaterialTheme.typography.bodyMedium, color = scheme.onSurface)
                    Text(
                        if (card.reviewedBy.isBlank()) stringResource(R.string.not_reviewed) else stringResource(R.string.reviewed_by, card.reviewedBy),
                        style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant,
                    )
                }
                Text(stringResource(R.string.help_from), style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun CallButton(number: String, onCall: (String) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Button(
        onClick = { onCall(number) },
        colors = ButtonDefaults.buttonColors(containerColor = scheme.error, contentColor = scheme.onError),
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
    ) {
        Text(stringResource(R.string.call_number, number), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun StepNumber(n: Int) {
    val scheme = MaterialTheme.colorScheme
    Box(Modifier.size(36.dp).background(scheme.primary, CircleShape), contentAlignment = Alignment.Center) {
        Text("$n", color = scheme.onPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
    }
}

/** The guide's pictures are drawings on white; they stay on white in the dark themes too, so they read the same. */
@Composable
private fun FigureImage(name: String, caption: String) {
    val context = LocalContext.current
    val bitmap by produceState<Bitmap?>(null, name) {
        value = withContext(Dispatchers.IO) {
            try {
                context.assets.open("help/figures/$name.png").use { BitmapFactory.decodeStream(it) }
            } catch (_: Exception) {
                // A missing picture leaves the step's text, which says the same
                null
            }
        }
    }
    bitmap?.let {
        Image(
            bitmap = it.asImageBitmap(),
            // The caption under it says what it shows; read twice it helps no one
            contentDescription = if (caption.isBlank()) stringResource(R.string.figure) else null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(8.dp)).padding(8.dp),
        )
    }
}

private fun pictogram(name: String): Int? = when (name) {
    "press" -> R.drawable.pict_press
    else -> null
}

package ua.surma.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ua.surma.R
import ua.surma.ui.theme.CardTitle
import ua.surma.ui.theme.Ornament

/** One organisation: its name, what it does, and its official site (no tracking marks in the link). */
private class Cause(@StringRes val name: Int, @StringRes val what: Int, @StringRes val url: Int)

// Only official, public channels: no single foundation or person is singled out
private val DEFENCE = listOf(
    Cause(R.string.org_united24, R.string.org_united24_defence, R.string.org_united24_url),
    Cause(R.string.org_nbu, R.string.org_nbu_what, R.string.org_nbu_url),
    // Not a fundraiser: the state's defence-tech cluster, to learn about it
    Cause(R.string.org_brave1, R.string.org_brave1_what, R.string.org_brave1_url),
)

private val HUMANITARIAN = listOf(
    Cause(R.string.org_united24, R.string.org_united24_humanitarian, R.string.org_united24_url),
    Cause(R.string.org_redcross, R.string.org_redcross_what, R.string.org_redcross_url),
)

/**
 * "About Surma": what it is (and that it is not an official app), what others can see, how to support Ukraine
 * (only links to the organisations' own sites; Surma takes no part in the money), and the plain facts.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AboutPage(onBack: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val uri = LocalUriHandler.current
    val open = { url: String -> runCatching { uri.openUri(url) }; Unit }
    Scaffold(
        containerColor = scheme.background,
        topBar = {
            Column(Modifier.background(scheme.background)) {
                // The title in the middle, as on every page
                CenterAlignedTopAppBar(
                    title = { Text(stringResource(R.string.about_surma), color = scheme.primary, fontWeight = FontWeight.Bold) },
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
        Box(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter,
        ) {
            // A readable line length on a tablet or an unfolded phone
                        Column(Modifier.widthIn(max = 640.dp).fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                // What Surma is, and what it is not
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = scheme.surface),
                    border = BorderStroke(1.dp, scheme.outlineVariant),
                ) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        for ((title, text) in listOf(R.string.about_what_title to R.string.about_what, R.string.about_privacy_title to R.string.about_privacy)) {
                            Text(stringResource(title), style = CardTitle, color = scheme.onSurface, textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().semantics { heading() })
                            Text(stringResource(text), style = MaterialTheme.typography.bodyLarge, color = scheme.onSurface)
                        }
                        Text(stringResource(R.string.disclaimer), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold,
                            color = scheme.primary)
                    }
                }

                Spacer(Modifier.height(24.dp))
                Text(stringResource(R.string.support_title), style = CardTitle, color = scheme.onSurface, textAlign = TextAlign.Center,
                    modifier = Modifier.semantics { heading() })
                Spacer(Modifier.height(4.dp))
                Text(stringResource(R.string.support_note), style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(16.dp))
                CauseGroup(R.string.support_defence, DEFENCE, open)
                Spacer(Modifier.height(16.dp))
                CauseGroup(R.string.support_humanitarian, HUMANITARIAN, open)
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.support_any), style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth())

                Spacer(Modifier.height(24.dp))
                Text(stringResource(R.string.about_title), style = CardTitle, color = scheme.onSurface, textAlign = TextAlign.Center,
                    modifier = Modifier.semantics { heading() })
                Spacer(Modifier.height(4.dp))
                Text(stringResource(R.string.about_text), style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                // Where the places and their points come from, as the ODbL asks
                Text(stringResource(R.string.about_data), style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth())
                val context = LocalContext.current
                val version = remember {
                    runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull()
                }
                version?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(stringResource(R.string.about_version, it), style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth())
                }
                Row {
                    TextButton(onClick = { open(SOURCE_URL) }) { Text(stringResource(R.string.source_code)) }
                    TextButton(onClick = { open(LICENSE_URL) }) { Text(stringResource(R.string.license)) }
                }
            }
        }
    }
}

@Composable
private fun CauseGroup(@StringRes title: Int, causes: List<Cause>, open: (String) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Text(stringResource(title), style = MaterialTheme.typography.titleMedium, color = scheme.primary,
        fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, modifier = Modifier.semantics { heading() })
    Spacer(Modifier.height(8.dp))
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surface),
        border = BorderStroke(1.dp, scheme.outlineVariant),
    ) {
        for ((i, cause) in causes.withIndex()) {
            if (i > 0) Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(1.dp).background(scheme.outlineVariant))
            val url = stringResource(cause.url)
            Row(
                Modifier.fillMaxWidth().heightIn(min = 64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(role = Role.Button, onClickLabel = stringResource(R.string.open_site)) { open(url) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(cause.name), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold,
                            color = scheme.onSurface, textAlign = TextAlign.Center)
                        Spacer(Modifier.width(6.dp))
                        Icon(painterResource(R.drawable.ic_open), contentDescription = null, tint = scheme.primary, modifier = Modifier.size(18.dp))
                    }
                    Text(stringResource(cause.what), style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant,
                        textAlign = TextAlign.Center)
                    // Only the site's name: where it leads, readable at a glance
                    Text(url.removePrefix("https://").substringBefore('/'), style = MaterialTheme.typography.labelMedium, color = scheme.primary)
                }
            }
        }
    }
}

internal const val SOURCE_URL = "https://github.com/marybezuhla/surma"
internal const val LICENSE_URL = "$SOURCE_URL/blob/main/LICENSE"

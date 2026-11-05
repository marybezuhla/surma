package ua.surma.ui

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import ua.surma.R

/** The two tabs at the bottom: the threats, and what to do. */
@Composable
internal fun Tabs(page: MainActivity.Page, onPage: (MainActivity.Page) -> Unit) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        for ((p, label, icon) in listOf(
            Triple(MainActivity.Page.HOME, R.string.tab_threats, R.drawable.ic_notification),
            Triple(MainActivity.Page.HELP, R.string.tab_help, R.drawable.ic_first_aid),
        )) {
            NavigationBarItem(
                selected = page == p,
                onClick = { onPage(p) },
                icon = { Icon(painterResource(icon), contentDescription = null) },
                label = { Text(stringResource(label)) },
            )
        }
    }
}

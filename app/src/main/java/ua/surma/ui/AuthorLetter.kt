package ua.surma.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/*
 * The author's letter (how the app began, the war, the support) with her photo and name, and the
 * official ways to support Ukraine: the same in all of the author's apps. Links only open in the
 * browser; the app learns nothing of it.
 */

private val LETTER = listOf(ua.surma.R.string.author_letter_1, ua.surma.R.string.author_letter_2, ua.surma.R.string.author_letter_3, ua.surma.R.string.author_letter_4, ua.surma.R.string.author_letter_5, ua.surma.R.string.author_letter_6, ua.surma.R.string.author_letter_7)

/** The letter in a card: every paragraph in the middle, the signature, the photo and the name. */
@Composable
fun AuthorLetter(modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surface),
        border = BorderStroke(1.dp, scheme.outlineVariant),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            for (p in LETTER) {
                Text(stringResource(p), style = MaterialTheme.typography.bodyLarge, color = scheme.onSurface,
                    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
            Text(stringResource(ua.surma.R.string.author_letter_signature), style = MaterialTheme.typography.bodyLarge, fontStyle = FontStyle.Italic,
                color = scheme.primary, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Image(
                painterResource(ua.surma.R.drawable.author_photo),
                // The name below says who it is
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp).size(72.dp)
                    .clip(CircleShape).border(1.dp, scheme.outlineVariant, CircleShape),
            )
            Text(stringResource(ua.surma.R.string.author_author_name), style = MaterialTheme.typography.bodyMedium, color = scheme.onSurface,
                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
    }
}

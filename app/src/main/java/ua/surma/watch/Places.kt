package ua.surma.watch

import android.content.Context
import ua.surma.alerts.PlaceIndex

/** The register of places, read once from the app's own files (assets/places.tsv) and kept while Surma runs. */
object Places {
    @Volatile private var index: PlaceIndex? = null

    fun get(context: Context): PlaceIndex = index ?: synchronized(this) {
        index ?: try {
            context.assets.open("places.tsv").use { PlaceIndex.parse(it.readBytes().toString(Charsets.UTF_8)) }
        } catch (_: Exception) {
            // Without the list only regions are found; alerts still come
            PlaceIndex.EMPTY
        }.also { index = it }
    }
}

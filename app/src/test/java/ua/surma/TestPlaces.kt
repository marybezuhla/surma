package ua.surma

import ua.surma.alerts.PlaceIndex
import java.io.File

/** The app's own register of places, read straight from its assets (Gradle runs tests in the app folder). */
object TestPlaces {
    val index: PlaceIndex by lazy {
        val f = listOf("src/main/assets/places.tsv", "app/src/main/assets/places.tsv", System.getenv("SURMA_PLACES") ?: "")
            .map(::File).first { it.isFile }
        PlaceIndex.parse(f.readText())
    }
}

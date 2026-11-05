package ua.surma.alerts

/** A city, town, village or district as the register lists it. */
enum class PlaceKind(val code: Char) {
    CITY('M'), TOWN('X'), VILLAGE('C'), DISTRICT('R');

    companion object {
        fun of(code: Char): PlaceKind? = entries.firstOrNull { it.code == code }
    }
}

/**
 * Every place of the register, found by its name in the nominative (lower case, ASCII apostrophe).
 * Read from places.tsv (see scripts/places/build_places.py); an unreadable line is skipped.
 */
class PlaceIndex private constructor(private val byName: Map<String, List<Place>>) {
    val size: Int get() = byName.values.sumOf { it.size }

    fun named(nominative: String): List<Place> = byName[nominative].orEmpty()

    fun all(): List<Place> = byName.values.flatten()

    /**
     * Which of the places with this name is meant. The regions named in the post decide first; then a region's
     * centre, then the only city of that name. Several equal candidates: none, the name stays text.
     */
    fun choose(candidates: List<Place>, regions: Collection<Oblast>): Place? {
        if (candidates.isEmpty()) return null
        val inRegion = if (regions.isEmpty()) candidates else candidates.filter { it.oblast in regions }.ifEmpty {
            // A post can name a region and a city just over its border; then the region does not help
            candidates
        }
        inRegion.singleOrNull()?.let { return it }
        inRegion.filter { isRegionalCentre(it) }.singleOrNull()?.let { return it }
        // The only city of that name; towns and villages are never guessed between
        return inRegion.filter { it.kind == PlaceKind.CITY }.singleOrNull()
    }

    companion object {
        val EMPTY = PlaceIndex(emptyMap())

        /** The register's line: name, kind, region key, lat, lon, separated by tabs. */
        fun parse(tsv: String): PlaceIndex {
            val map = HashMap<String, MutableList<Place>>(40_000)
            for (line in tsv.lineSequence()) {
                if (line.isEmpty() || line.startsWith("#")) continue
                val f = line.split('\t')
                if (f.size != 5 || f[0].isBlank() || f[0].length > 80 || f[1].length != 1) continue
                val kind = PlaceKind.of(f[1][0]) ?: continue
                val oblast = Oblast.of(f[2]) ?: continue
                val lat = f[3].toDoubleOrNull()?.takeIf { it in 44.0..53.0 } ?: continue
                val lon = f[4].toDoubleOrNull()?.takeIf { it in 22.0..41.0 } ?: continue
                map.getOrPut(PostParser.normalize(f[0])) { ArrayList(1) } += Place(f[0], oblast, lat, lon, kind)
            }
            return PlaceIndex(map)
        }

        fun isRegionalCentre(p: Place): Boolean = p.kind == PlaceKind.CITY && p.name == p.oblast.centre
    }
}

package ua.surma.alerts

import java.util.regex.Pattern

/**
 * Where in a line a region or a place is. place: the place meant; name: a place of the register written here
 * whose name several places share (shown as text, no point); oblast: the region it is in, when known.
 */
internal class GeoMatch(val start: Int, val end: Int, val place: Place?, val oblast: Oblast?, val name: String? = null)

/** Finds regions and places in a line. norm is the line through [PostParser.normalize]: same length, same positions. */
internal object Gazetteer {
    // Letters, the apostrophe and the hyphen count as part of a word
    private const val L = "\\p{L}'-"

    private fun bounded(fragment: String): Pattern =
        Pattern.compile("(?<![$L])(?:$fragment)(?![$L])", Pattern.UNICODE_CHARACTER_CLASS)

    private class Entry(val pattern: Pattern, val oblast: Oblast)

    private val regions: List<Entry> = buildList {
        for (o in Oblast.entries) {
            for (s in o.stems) add(Entry(bounded(Pattern.quote(s) + "\\p{L}*"), o))
            // "Сумська область", "Сумської обл."
            o.adj?.let { add(Entry(bounded(Pattern.quote(it) + "\\p{L}*\\s+обл\\p{L}*\\.?"), o)) }
        }
    }

    /** Every region named, without overlaps (the longer match wins). */
    fun oblasts(norm: String): List<GeoMatch> {
        val all = mutableListOf<GeoMatch>()
        for (e in regions) {
            val m = e.pattern.matcher(norm)
            while (m.find()) all += GeoMatch(m.start(), m.end(), null, e.oblast)
        }
        all.sortWith(compareBy<GeoMatch> { it.start }.thenByDescending { it.end - it.start })
        val out = mutableListOf<GeoMatch>()
        for (g in all) if (out.none { g.start < it.end && it.start < g.end }) out += g
        return out
    }

    private val WORD = Regex("[\\p{L}'’ʼ`-]+")
    private val DISTRICT = Regex("^район\\p{L}{0,3}$")
    /** Words that are written with a capital letter in posts but are never meant as a place. */
    private val NEVER = setOf("україна", "україни", "україні", "сили", "повітряні", "увага", "загроза", "відбій")
    private const val MAX_WORDS = 3
    private const val MAX_KEYS = 400

    /**
     * Places of the register in the line. A village or town counts only right after "на", "курсом на", "в районі"…
     * ([cueEnds]); a city or a district counts anywhere. Several places of one name: [regions] decide.
     */
    fun places(line: String, norm: String, index: PlaceIndex, cueEnds: Set<Int>, regions: Collection<Oblast>, taken: List<GeoMatch>): List<GeoMatch> {
        val words = WORD.findAll(line).toList()
        val out = mutableListOf<GeoMatch>()
        var i = 0
        while (i < words.size) {
            val w = words[i]
            val start = w.range.first
            val isTaken = taken.any { start < it.end && it.start <= w.range.last } || !capitalised(w.value)
            if (isTaken) { i++; continue }
            val afterCue = start in cueEnds
            val match = district(words, i, norm, index, regions) ?: name(words, i, line, norm, index, regions, afterCue)
            if (match == null) { i++; continue }
            out += match.first
            i += match.second
        }
        return out
    }

    private fun capitalised(w: String): Boolean =
        w.length >= 2 && w[0].isUpperCase() && w.drop(1).any { it.isLowerCase() }

    /** "Конотопський район", "Конотопського району". */
    private fun district(words: List<MatchResult>, i: Int, norm: String, index: PlaceIndex, regions: Collection<Oblast>): Pair<GeoMatch, Int>? {
        val next = words.getOrNull(i + 1) ?: return null
        if (!DISTRICT.matches(norm.substring(next.range))) return null
        val w = words[i]
        val candidates = Declension.nominatives(norm.substring(w.range)).flatMap { index.named(it) }
            .filter { it.kind == PlaceKind.DISTRICT }.distinct()
        if (candidates.isEmpty()) return null
        return found(candidates, w.range.first, next.range.last + 1, norm.substring(w.range.first, next.range.last + 1), index, regions) to 2
    }

    private fun name(
        words: List<MatchResult>, i: Int, line: String, norm: String, index: PlaceIndex, regions: Collection<Oblast>, afterCue: Boolean,
    ): Pair<GeoMatch, Int>? {
        for (n in minOf(MAX_WORDS, words.size - i) downTo 1) {
            val group = words.subList(i, i + n)
            // Several words only if they stand next to each other and each starts with a capital ("Біла Церква")
            if (n > 1 && (group.zipWithNext().any { (a, b) -> line.substring(a.range.last + 1, b.range.first).isNotBlank() } ||
                    group.drop(1).any { !capitalised(it.value) })
            ) continue
            val keys = keys(group.map { norm.substring(it.range) })
            if (keys.any { it in NEVER }) return null
            var candidates = keys.flatMap { index.named(it) }.distinct().filter { it.kind != PlaceKind.DISTRICT }
            if (!afterCue) candidates = candidates.filter { it.kind == PlaceKind.CITY }
            if (candidates.isEmpty()) continue
            val end = group.last().range.last + 1
            return found(candidates, group.first().range.first, end, line.substring(group.first().range.first, end), index, regions) to n
        }
        return null
    }

    private fun found(candidates: List<Place>, start: Int, end: Int, written: String, index: PlaceIndex, regions: Collection<Oblast>): GeoMatch {
        val place = index.choose(candidates, regions)
        val oblast = place?.oblast ?: candidates.map { it.oblast }.distinct().singleOrNull()
        return GeoMatch(start, end, place, oblast, if (place == null) written else null)
    }

    /** Every nominative the words could come from, joined as the register writes them (a space between words). */
    private fun keys(words: List<String>): List<String> {
        var acc = listOf("")
        for (w in words) {
            val forms = Declension.nominatives(w)
            acc = acc.flatMap { a -> forms.map { if (a.isEmpty()) it else "$a $it" } }.take(MAX_KEYS)
        }
        return acc
    }
}

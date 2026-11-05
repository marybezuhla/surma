package ua.surma.alerts

/**
 * Reads the text of one Air Force post into threats.
 *
 * Line by line: each line may hold a threat of its own. A line without a kind ("Полтавщина — курсом на Миргород")
 * takes the kind from the line above it ("Ударні БпЛА:"), and a line that only adds the target to the threat
 * above it ("…на Сумщині" / "курсом на Шостку") is joined to it.
 * The words are Ukrainian because the channel writes in Ukrainian.
 */
object PostParser {
    private const val U = "(?U)"
    // No letter right before / right after: whole words only
    private const val NL = "(?<![\\p{L}])"
    private const val NR = "(?![\\p{L}])"

    /** Lower case and one kind of apostrophe; the same length as the input, so positions stay the same. */
    fun normalize(s: String): String = buildString(s.length) {
        for (c in s) append(if (c == '’' || c == 'ʼ' || c == '`' || c == '‘') '\'' else c.lowercaseChar())
    }

    /** The morning summaries ("збито/подавлено…"): no alert for those. */
    private val REPORT = Regex("$U$NL(збито|подавлено|знешкоджено|станом на|у ніч на|в ніч на|противник атакував)")

    private val TYPES: List<Pair<ThreatType, Regex>> = listOf(
        ThreatType.ALL_CLEAR to Regex("$U$NL(відбій|чисто)$NR"),
        ThreatType.BALLISTIC to Regex("$U${NL}балістич|${NL}балістик"),
        ThreatType.HIGH_SPEED to Regex("$U${NL}швидкісн\\p{L}*\\s+ціл"),
        ThreatType.MISSILE to Regex("$U$NL(ракет|крилат|калібр|кинджал|іскандер|х-101|х-59|х-22)"),
        ThreatType.JET_DRONE to Regex("$U${NL}реактивн\\p{L}*\\s+(ударн\\p{L}*\\s+)?бпла"),
        ThreatType.GUIDED_BOMB to Regex("$U$NL(каб\\p{L}{0,3}$NR|керован\\p{L}*\\s+авіа\\p{L}*\\s*бомб)"),
        ThreatType.DRONE to Regex("$U$NL(бпла|шахед|дрон|мопед)|🛵"),
        ThreatType.AVIATION to Regex("$U$NL(авіаці|міг-31|ту-95|ту-22|ту-160|су-34|су-35)"),
    )

    /** Clear words for "towards"; plain "на" only if none of them leads to a place. */
    private val STRONG_CUE = Regex(
        "$U$NL(курсом на|вектором руху на|напрямком на|в напрямку|у напрямку|у бік|в бік|в районі|у районі)\\s+((м|с|смт|н\\.\\s?п)\\.\\s*)?",
    )
    private val WEAK_CUE = Regex("$U${NL}на\\s+((м|с|смт|н\\.\\s?п)\\.\\s*)?")
    /** Where a village may be named: after a word for "towards" or "near". */
    private val NEAR_CUE = Regex("$U$NL(біля|поблизу|повз|через|над|до)\\s+((м|с|смт|н\\.\\s?п)\\.\\s*)?")

    private val COUNT = Regex("$U(\\d+)\\s*[хx×*]?\\s*(ударн|бпла|шахед|ракет|каб|крилат|реактивн|балістичн|ціл)")
    private val GROUP = Regex("$U${NL}груп")

    /** "з/зі/із …": where it comes from, up to the next "курсом/на/у/в". */
    private val FROM = Regex("$U$NL(з|зі|із)\\s+([^.,!;:()\\n]*)")
    private val FROM_STOP = Regex("$U\\s(курсом|вектором|на|у|в)\\s")
    private val DIRECTIONS: List<Pair<Direction, Regex>> = listOf(
        // Compound directions first, so "північно-східного" is not also read as "східного"
        Direction.NE to Regex("(?U)північно-східн\\p{L}*|північного сходу"),
        Direction.NW to Regex("(?U)північно-західн\\p{L}*|північного заходу"),
        Direction.SE to Regex("(?U)південно-східн\\p{L}*|південного сходу"),
        Direction.SW to Regex("(?U)південно-західн\\p{L}*|південного заходу"),
        Direction.N to Regex("(?U)північн\\p{L}*|півноч\\p{L}*"),
        Direction.S to Regex("(?U)південн\\p{L}*|півдн\\p{L}*"),
        Direction.E to Regex("(?U)східн\\p{L}*|сход\\p{L}*"),
        Direction.W to Regex("(?U)західн\\p{L}*|заход\\p{L}*"),
    )
    /** A capitalised word or two right after "на": a place Surma does not know yet. */
    private val UNKNOWN_PLACE = Regex("^(\\p{Lu}[\\p{L}'’ʼ-]*(?:\\s+\\p{Lu}[\\p{L}'’ʼ-]*)?)")
    private val DIRECTION_WORD = Regex("(?U)^(північ|півден|схід|східн|захід|західн|півноч|півдн)")

    fun isReport(text: String): Boolean = REPORT.containsMatchIn(normalize(text))

    /** [places]: the register of places (see [PlaceIndex]); without it only regions are found. */
    fun parse(text: String, places: PlaceIndex = PlaceIndex.EMPTY): List<Threat> {
        if (text.length > Rules.MAX_POST_CHARS || isReport(text)) return emptyList()
        // Regions named anywhere in the post tell which of several places of one name is meant
        val postRegions = Gazetteer.oblasts(normalize(text)).mapNotNull { it.oblast }.toSet()
        val out = mutableListOf<Threat>()
        var inherited: ThreatType? = null
        // A line with a kind but no place yet ("Ударні БпЛА:"): kept until the lines under it say where
        var header: Threat? = null
        var previousEmitted = false

        for (raw in text.split('\n')) {
            val line = raw.trim()
            if (line.isEmpty()) continue
            val norm = normalize(line)
            val ownType = TYPES.firstOrNull { it.second.containsMatchIn(norm) }?.first
            val type = ownType ?: inherited
            if (type == null) {
                previousEmitted = false
                continue
            }
            val t = parseLine(line, norm, type, places, postRegions)
            val hasPlace = t.target != null || t.unresolvedTarget != null || t.oblasts.isNotEmpty() || t.from.isNotEmpty()
            var emitted = false

            if (ownType != null) {
                header?.let { out += it }
                header = null
                inherited = ownType
                if (hasPlace || ownType == ThreatType.ALL_CLEAR) {
                    out += t
                    emitted = true
                } else {
                    header = t
                }
            } else if (hasPlace) {
                val prev = out.lastOrNull()
                if (header == null && previousEmitted && prev != null && prev.target == null && prev.unresolvedTarget == null) {
                    // A wrapped line: "…на Сумщині" / "курсом на Шостку" is one threat
                    out[out.lastIndex] = prev.copy(
                        target = t.target,
                        unresolvedTarget = t.unresolvedTarget,
                        oblasts = (prev.oblasts + t.oblasts).distinct(),
                        from = (prev.from + t.from).distinct(),
                        count = prev.count ?: t.count,
                        sourceLine = prev.sourceLine + " " + t.sourceLine,
                    )
                } else {
                    header = null
                    out += t
                }
                emitted = true
            }
            previousEmitted = emitted
        }
        header?.let { out += it }
        return out.take(Rules.MAX_THREATS_PER_POST)
    }

    private fun parseLine(line: String, norm: String, type: ThreatType, places: PlaceIndex, postRegions: Set<Oblast>): Threat {
        // Places after "з/зі/із" are where it comes from ("з Криму"), not where the threat is
        val origins = FROM.findAll(norm).map { m ->
            val seg = m.groups[2]!!.range
            val stop = FROM_STOP.find(" " + m.groupValues[2])?.range?.first?.let { seg.first + it - 1 }
            seg.first..(stop?.minus(1) ?: seg.last)
        }.toList()
        val regionMatches = Gazetteer.oblasts(norm)
        val cueEnds = listOf(STRONG_CUE, WEAK_CUE, NEAR_CUE).flatMap { c -> c.findAll(norm).map { it.range.last + 1 }.toList() }.toSet()
        val lineRegions = regionMatches.filter { g -> origins.none { g.start in it } }.mapNotNull { it.oblast }
        val placeMatches = Gazetteer.places(line, norm, places, cueEnds, lineRegions.ifEmpty { postRegions }, regionMatches)
        val geo = (regionMatches + placeMatches).filter { g -> origins.none { g.start in it } }.sortedBy { it.start }

        var target: Place? = null
        var unknown: String? = null
        val oblasts = LinkedHashSet<Oblast>()

        fun at(pos: Int): GeoMatch? = geo.firstOrNull { it.start == pos }
        fun capitalisedAt(pos: Int): String? {
            val w = UNKNOWN_PLACE.find(line.substring(pos))?.groupValues?.get(1) ?: return null
            return if (DIRECTION_WORD.containsMatchIn(normalize(w))) null else w
        }

        for (cue in listOf(STRONG_CUE, WEAK_CUE)) {
            for (m in cue.findAll(norm)) {
                val pos = m.range.last + 1
                val g = at(pos)
                when {
                    g?.place != null && target == null -> target = g.place
                    g?.name != null && target == null && unknown == null -> unknown = g.name
                    g != null && g.place == null && g.name == null -> g.oblast?.let { oblasts += it }
                    g == null && target == null && unknown == null -> unknown = capitalisedAt(pos)
                }
            }
            if (target != null) break
        }
        // A city or district named without "на": still the place of the threat
        if (target == null && unknown == null) geo.firstOrNull { it.place != null }?.let { target = it.place }
        if (target != null) unknown = null
        geo.forEach { g -> if (g.place == null) g.oblast?.let { oblasts += it } }
        target?.let { oblasts += it.oblast }

        val count = COUNT.find(norm)?.groupValues?.get(1)?.toIntOrNull()?.takeIf { it in 1..Rules.MAX_COUNT }
        return Threat(
            type = type,
            count = count,
            isGroup = count == null && GROUP.containsMatchIn(norm),
            oblasts = oblasts.toList(),
            target = target,
            unresolvedTarget = unknown,
            from = directions(norm),
            sourceLine = line.take(Rules.MAX_LINE_CHARS),
        )
    }

    private fun directions(norm: String): List<Direction> {
        val out = LinkedHashSet<Direction>()
        for (m in FROM.findAll(norm)) {
            var seg = m.groupValues[2]
            FROM_STOP.find(" $seg")?.let { seg = seg.substring(0, maxOf(0, it.range.first - 1)) }
            var rest = seg
            for ((d, re) in DIRECTIONS) {
                if (re.containsMatchIn(rest)) {
                    out += d
                    rest = re.replace(rest, " ")
                }
            }
        }
        return out.toList()
    }
}

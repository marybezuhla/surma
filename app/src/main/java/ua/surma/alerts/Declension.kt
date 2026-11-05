package ua.surma.alerts

/**
 * Ukrainian place names change their endings ("Іванівка", "на Іванівку", "в Іванівці"). The register lists each
 * place once, in the nominative. This turns a word as written back into the nominatives it could come from;
 * a wrong guess costs nothing, because only guesses that are real names in the register are used.
 * Words are lower case with ASCII apostrophes (see [PostParser.normalize]).
 */
object Declension {
    /** Ending as written -> possible endings of the nominative. Longer endings are tried too, never instead. */
    private val ENDINGS: List<Pair<String, List<String>>> = listOf(
        // -ка, -ця: Іванівки, Іванівку, Іванівці, Іванівкою; Вінниці, Вінницю
        "ки" to listOf("ка"), "ку" to listOf("ка"), "кою" to listOf("ка"), "ці" to listOf("ка", "ця", "ець"),
        "цю" to listOf("ця", "ець"), "цею" to listOf("ця"), "ця" to listOf("ець"), "цем" to listOf("ець"),
        "зі" to listOf("га", "г"), "сі" to listOf("ха", "х"),
        // -а, -я: Полтави, Полтаві, Полтаву, Полтавою; Олександрії, Олександрію; Запоріжжі, Запоріжжю
        "и" to listOf("а"), "і" to listOf("а", "я", ""), "у" to listOf("а", ""), "ою" to listOf("а"),
        "ї" to listOf("я"), "ю" to listOf("я"), "єю" to listOf("я"),
        // Adjectives, first word or alone: Кропивницького, Цвіткового, Білої, Великих
        "ого" to listOf("ий", "е", "ій"), "ому" to listOf("ий", "е", "ій"), "им" to listOf("ий", "е", "і"),
        "ім" to listOf("ий", "е", "ій"), "ього" to listOf("ій", "є"), "ьому" to listOf("ій", "є"),
        "ої" to listOf("а"), "ій" to listOf("а"), "ьої" to listOf("я"), "их" to listOf("і"), "ими" to listOf("і"),
        // Plurals: Сумах, Сумам, Броварів, Чернівцях
        "ах" to listOf("и"), "ам" to listOf("и"), "ями" to listOf("і"), "ях" to listOf("і"), "ям" to listOf("і"),
        "ів" to listOf("и", "і"), "ами" to listOf("и"),
        // Masculine nouns: Харкова, Харкову, Харкові, Харковом; Ковеля, Ковелі, Ковелю (-у and -і are above)
        "а" to listOf(""), "ові" to listOf(""), "ом" to listOf(""), "я" to listOf("ь"), "ем" to listOf("ь"), "еві" to listOf("ь"),
        "єм" to listOf("й"), "ю" to listOf("ь"), "і" to listOf("ь"),
    )

    private val VOWELS = "аеєиіїоуюя"
    // The last vowel before the final consonants: "харков" -> "харків", "києв" -> "київ", "рог" -> "ріг", "ірпен" -> "ірпінь"
    private val CLOSED = Regex("^(.*)([оеє])([^аеєиіїоуюяь']+ь?)$")

    fun nominatives(word: String): Set<String> {
        if (word.length < 2) return emptySet()
        val out = LinkedHashSet<String>()
        out += word
        // A plural without an ending: "Черкас", "Прилук", "Сум"
        if (word.last() !in VOWELS) out += word + "и"
        for ((ending, nominative) in ENDINGS) {
            if (!word.endsWith(ending) || word.length - ending.length < 2) continue
            val stem = word.dropLast(ending.length)
            for (n in nominative) {
                val w = stem + n
                out += w
                alternate(w)?.let { out += it }
            }
        }
        alternate(word)?.let { out += it }
        return out
    }

    /** "харков" -> "харків": in the nominative the last closed syllable often has і (ї after є). */
    private fun alternate(w: String): String? {
        val m = CLOSED.find(w) ?: return null
        val (head, vowel, tail) = m.destructured
        if (head.isEmpty()) return null
        return head + (if (vowel == "є") "ї" else "і") + tail
    }
}

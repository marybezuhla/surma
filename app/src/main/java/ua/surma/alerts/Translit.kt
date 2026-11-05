package ua.surma.alerts

/**
 * Ukrainian names in Latin letters by the official rules (Cabinet of Ministers resolution No. 55 of 27 January 2010),
 * as on passports and road signs: Київ -> Kyiv, Кропивницький -> Kropyvnytskyi, Згорани -> Zghorany.
 * Є, Ї, Й, Ю, Я are written one way at the start of a word and another inside it; ь and the apostrophe are left out.
 */
object Translit {
    private val PLAIN = mapOf(
        'а' to "a", 'б' to "b", 'в' to "v", 'г' to "h", 'ґ' to "g", 'д' to "d", 'е' to "e", 'ж' to "zh", 'з' to "z",
        'и' to "y", 'і' to "i", 'к' to "k", 'л' to "l", 'м' to "m", 'н' to "n", 'о' to "o", 'п' to "p", 'р' to "r",
        'с' to "s", 'т' to "t", 'у' to "u", 'ф' to "f", 'х' to "kh", 'ц' to "ts", 'ч' to "ch", 'ш' to "sh", 'щ' to "shch",
        'ь' to "", '\'' to "", '’' to "", 'ʼ' to "",
    )
    /** At the start of a word, then inside it. */
    private val POSITIONAL = mapOf('є' to ("ye" to "ie"), 'ї' to ("yi" to "i"), 'й' to ("y" to "i"), 'ю' to ("yu" to "iu"), 'я' to ("ya" to "ia"))

    fun latin(name: String): String {
        val out = StringBuilder(name.length + 8)
        var wordStart = true
        var prev = ' '
        for (c in name) {
            val lower = c.lowercaseChar()
            val latin = when {
                // "зг" is written "zgh", so it is not read as "zh"
                lower == 'г' && prev == 'з' -> "gh"
                lower in POSITIONAL -> POSITIONAL.getValue(lower).let { if (wordStart) it.first else it.second }
                else -> PLAIN[lower]
            }
            if (latin == null) {
                out.append(c)
            } else if (latin.isNotEmpty()) {
                out.append(if (c.isUpperCase()) latin.replaceFirstChar { it.uppercaseChar() } else latin)
            }
            wordStart = !(c.isLetter() || c in "'’ʼ")
            prev = lower
        }
        return out.toString()
    }
}

package ua.surma.alerts

import java.util.regex.Pattern

/**
 * A prototype list: regional centres and the towns the channel names most often.
 * TODO: generate the full list (every community, with its case forms) from the official KATOTTG register.
 * forms: exact word forms in lower case, apostrophe always ASCII '.
 */
internal class City(val place: Place, val forms: List<String>) {
    constructor(name: String, oblast: Oblast, lat: Double, lon: Double, forms: List<String>, approximate: Boolean = false) :
        this(Place(name, oblast, lat, lon, approximate), forms)
}

internal val CITIES = listOf(
    City("Київ", Oblast.KYIV_CITY, 50.4501, 30.5234, listOf("київ", "києва", "києві", "києву", "києвом")),
    City("Харків", Oblast.KHARKIV, 49.9935, 36.2304, listOf("харків", "харкова", "харкові", "харкову")),
    City("Дніпро", Oblast.DNIPRO, 48.4647, 35.0462, listOf("дніпро", "дніпра", "дніпрі", "дніпру")),
    City("Одеса", Oblast.ODESA, 46.4825, 30.7233, listOf("одеса", "одеси", "одесу", "одесі")),
    City("Запоріжжя", Oblast.ZAPORIZHZHIA, 47.8388, 35.1396, listOf("запоріжжя", "запоріжжі", "запоріжжю")),
    City("Львів", Oblast.LVIV, 49.8397, 24.0297, listOf("львів", "львова", "львові")),
    City("Суми", Oblast.SUMY, 50.9077, 34.7981, listOf("суми", "сумах", "сумам")),
    City("Полтава", Oblast.POLTAVA, 49.5883, 34.5514, listOf("полтава", "полтави", "полтаву", "полтаві")),
    City("Чернігів", Oblast.CHERNIHIV, 51.4982, 31.2893, listOf("чернігів", "чернігова", "чернігові")),
    City("Черкаси", Oblast.CHERKASY, 49.4444, 32.0598, listOf("черкаси", "черкас", "черкасах")),
    City("Кропивницький", Oblast.KIROVOHRAD, 48.5079, 32.2623, listOf("кропивницький", "кропивницького", "кропивницькому")),
    City("Миколаїв", Oblast.MYKOLAIV, 46.9750, 31.9946, listOf("миколаїв", "миколаєва", "миколаєві")),
    City("Херсон", Oblast.KHERSON, 46.6354, 32.6169, listOf("херсон", "херсона", "херсоні")),
    City("Вінниця", Oblast.VINNYTSIA, 49.2331, 28.4682, listOf("вінниця", "вінниці", "вінницю")),
    City("Житомир", Oblast.ZHYTOMYR, 50.2547, 28.6587, listOf("житомир", "житомира", "житомирі")),
    City("Рівне", Oblast.RIVNE, 50.6199, 26.2516, listOf("рівне", "рівного", "рівному")),
    City("Луцьк", Oblast.VOLYN, 50.7472, 25.3254, listOf("луцьк", "луцька", "луцьку")),
    City("Тернопіль", Oblast.TERNOPIL, 49.5535, 25.5948, listOf("тернопіль", "тернополя", "тернополі")),
    City("Хмельницький", Oblast.KHMELNYTSKYI, 49.4229, 26.9871, listOf("хмельницький", "хмельницького", "хмельницькому")),
    City("Ужгород", Oblast.ZAKARPATTIA, 48.6208, 22.2879, listOf("ужгород", "ужгорода", "ужгороді")),
    City("Чернівці", Oblast.CHERNIVTSI, 48.2921, 25.9358, listOf("чернівці", "чернівців", "чернівцях")),
    City("Івано-Франківськ", Oblast.IVANO_FRANKIVSK, 48.9226, 24.7111, listOf("івано-франківськ", "івано-франківська", "івано-франківську")),
    City("Кривий Ріг", Oblast.DNIPRO, 47.9105, 33.3918, listOf("кривий ріг", "кривого рогу", "кривому розі", "кривий ріг")),
    City("Біла Церква", Oblast.KYIV_OBLAST, 49.7968, 30.1311, listOf("біла церква", "білої церкви", "білу церкву", "білій церкві")),
    City("Бровари", Oblast.KYIV_OBLAST, 50.5110, 30.7909, listOf("бровари", "броварів", "броварах")),
    City("Бориспіль", Oblast.KYIV_OBLAST, 50.3527, 30.9550, listOf("бориспіль", "борисполя", "борисполі")),
    City("Конотоп", Oblast.SUMY, 51.2403, 33.2026, listOf("конотоп", "конотопа", "конотопі")),
    City("Шостка", Oblast.SUMY, 51.8636, 33.4697, listOf("шостка", "шостки", "шостку", "шостці")),
    City("Охтирка", Oblast.SUMY, 50.3100, 34.8988, listOf("охтирка", "охтирки", "охтирку", "охтирці")),
    City("Ніжин", Oblast.CHERNIHIV, 51.0480, 31.8869, listOf("ніжин", "ніжина", "ніжині")),
    City("Прилуки", Oblast.CHERNIHIV, 50.5935, 32.3876, listOf("прилуки", "прилук", "прилуках")),
    City("Кременчук", Oblast.POLTAVA, 49.0659, 33.4204, listOf("кременчук", "кременчука", "кременчуці")),
    City("Миргород", Oblast.POLTAVA, 49.9686, 33.6083, listOf("миргород", "миргорода", "миргороді")),
    City("Павлоград", Oblast.DNIPRO, 48.5348, 35.8706, listOf("павлоград", "павлограда", "павлограді")),
    City("Кам'янське", Oblast.DNIPRO, 48.5079, 34.6132, listOf("кам'янське", "кам'янського", "кам'янському")),
    City("Нікополь", Oblast.DNIPRO, 47.5714, 34.3963, listOf("нікополь", "нікополя", "нікополі")),
    City("Краматорськ", Oblast.DONETSK, 48.7389, 37.5848, listOf("краматорськ", "краматорська", "краматорську")),
    City("Слов'янськ", Oblast.DONETSK, 48.8539, 37.6053, listOf("слов'янськ", "слов'янська", "слов'янську")),
    City("Ізюм", Oblast.KHARKIV, 49.2106, 37.2486, listOf("ізюм", "ізюма", "ізюмі")),
    City("Куп'янськ", Oblast.KHARKIV, 49.7106, 37.6151, listOf("куп'янськ", "куп'янська", "куп'янську")),
    City("Чугуїв", Oblast.KHARKIV, 49.8353, 36.6880, listOf("чугуїв", "чугуєва", "чугуєві")),
    City("Лозова", Oblast.KHARKIV, 48.8890, 36.3172, listOf("лозова", "лозової", "лозову", "лозовій")),
    City("Умань", Oblast.CHERKASY, 48.7484, 30.2218, listOf("умань", "умані")),
    City("Цвіткове", Oblast.CHERKASY, 49.2050, 31.6000, listOf("цвіткове", "цвіткового", "цвітковому"), approximate = true),
    City("Олександрія", Oblast.KIROVOHRAD, 48.6697, 33.1159, listOf("олександрія", "олександрії", "олександрію")),
    City("Вознесенськ", Oblast.MYKOLAIV, 47.5679, 31.3367, listOf("вознесенськ", "вознесенська", "вознесенську")),
    City("Первомайськ", Oblast.MYKOLAIV, 48.0446, 30.8506, listOf("первомайськ", "первомайська", "первомайську")),
    City("Чорноморськ", Oblast.ODESA, 46.3017, 30.6550, listOf("чорноморськ", "чорноморська", "чорноморську")),
    City("Ізмаїл", Oblast.ODESA, 45.3516, 28.8365, listOf("ізмаїл", "ізмаїла", "ізмаїлі")),
    City("Ковель", Oblast.VOLYN, 51.2154, 24.7086, listOf("ковель", "ковеля", "ковелі")),
    City("Старокостянтинів", Oblast.KHMELNYTSKYI, 49.7553, 27.2153, listOf("старокостянтинів", "старокостянтинова", "старокостянтинові")),
    City("Шепетівка", Oblast.KHMELNYTSKYI, 50.1830, 27.0634, listOf("шепетівка", "шепетівки", "шепетівку", "шепетівці")),
)

internal class GeoMatch(val start: Int, val end: Int, val city: City?, val oblast: Oblast?)

/** Finds regions and towns in a normalised line (see [PostParser.normalize]). */
internal object Gazetteer {
    // Letters, the apostrophe and the hyphen count as part of a word
    private const val L = "\\p{L}'-"

    private fun bounded(fragment: String): Pattern =
        Pattern.compile("(?<![$L])(?:$fragment)(?![$L])", Pattern.UNICODE_CHARACTER_CLASS)

    private fun literal(form: String): String = form.split(' ').joinToString("\\s+") { Pattern.quote(it) }

    private class Entry(val pattern: Pattern, val city: City?, val oblast: Oblast)

    private val entries: List<Entry> = buildList {
        for (o in Oblast.entries) {
            for (s in o.stems) add(Entry(bounded(Pattern.quote(s) + "\\p{L}*"), null, o))
            // "Сумська область", "Сумської обл."
            o.adj?.let { add(Entry(bounded(Pattern.quote(it) + "\\p{L}*\\s+обл\\p{L}*\\.?"), null, o)) }
        }
        for (c in CITIES) for (f in c.forms.distinct()) add(Entry(bounded(literal(f)), c, c.place.oblast))
    }

    /** Every match, without overlaps: the longer one wins ("кривого рогу" over a shorter form). */
    fun find(norm: String): List<GeoMatch> {
        val all = mutableListOf<GeoMatch>()
        for (e in entries) {
            val m = e.pattern.matcher(norm)
            while (m.find()) all += GeoMatch(m.start(), m.end(), e.city, e.oblast)
        }
        all.sortWith(compareBy<GeoMatch> { it.start }.thenByDescending { it.end - it.start })
        val out = mutableListOf<GeoMatch>()
        for (g in all) if (out.none { g.start < it.end && it.start < g.end }) out += g
        return out
    }
}

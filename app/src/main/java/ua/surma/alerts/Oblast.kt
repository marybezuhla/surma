package ua.surma.alerts

/**
 * Regions. stems: the start of the region's short name in any case ("сумщин" finds Сумщина, Сумщині, Сумщину);
 * adj: the start of the adjective, counted only before the word "область" ("сумськ" + "обл").
 * The point is the regional centre (or the region's actual seat); centre: its name as the register writes it.
 */
enum class Oblast(val key: String, val lat: Double, val lon: Double, val stems: List<String>, val adj: String?, val centre: String?) {
    VINNYTSIA("vinnytsia", 49.2331, 28.4682, listOf("вінниччин"), "вінницьк", "Вінниця"),
    VOLYN("volyn", 50.7472, 25.3254, listOf("волин"), "волинськ", "Луцьк"),
    DNIPRO("dnipro", 48.4647, 35.0462, listOf("дніпропетровщин"), "дніпропетровськ", "Дніпро"),
    DONETSK("donetsk", 48.7389, 37.5848, listOf("донеччин"), "донецьк", "Донецьк"),
    ZHYTOMYR("zhytomyr", 50.2547, 28.6587, listOf("житомирщин"), "житомирськ", "Житомир"),
    ZAKARPATTIA("zakarpattia", 48.6208, 22.2879, listOf("закарпатт"), "закарпатськ", "Ужгород"),
    ZAPORIZHZHIA("zaporizhzhia", 47.8388, 35.1396, listOf("запоріжчин"), "запорізьк", "Запоріжжя"),
    IVANO_FRANKIVSK("ivano_frankivsk", 48.9226, 24.7111, listOf("івано-франківщин", "франківщин", "прикарпатт"), "івано-франківськ", "Івано-Франківськ"),
    KYIV_OBLAST("kyiv_oblast", 50.4501, 30.5234, listOf("київщин"), "київськ", null),
    KYIV_CITY("kyiv_city", 50.4501, 30.5234, emptyList(), null, "Київ"),
    KIROVOHRAD("kirovohrad", 48.5079, 32.2623, listOf("кіровоградщин", "кропивниччин"), "кіровоградськ", "Кропивницький"),
    LUHANSK("luhansk", 48.5740, 39.3078, listOf("луганщин"), "луганськ", "Луганськ"),
    LVIV("lviv", 49.8397, 24.0297, listOf("львівщин"), "львівськ", "Львів"),
    MYKOLAIV("mykolaiv", 46.9750, 31.9946, listOf("миколаївщин"), "миколаївськ", "Миколаїв"),
    ODESA("odesa", 46.4825, 30.7233, listOf("одещин"), "одеськ", "Одеса"),
    POLTAVA("poltava", 49.5883, 34.5514, listOf("полтавщин"), "полтавськ", "Полтава"),
    RIVNE("rivne", 50.6199, 26.2516, listOf("рівненщин"), "рівненськ", "Рівне"),
    SUMY("sumy", 50.9077, 34.7981, listOf("сумщин"), "сумськ", "Суми"),
    TERNOPIL("ternopil", 49.5535, 25.5948, listOf("тернопільщин"), "тернопільськ", "Тернопіль"),
    KHARKIV("kharkiv", 49.9935, 36.2304, listOf("харківщин"), "харківськ", "Харків"),
    KHERSON("kherson", 46.6354, 32.6169, listOf("херсонщин"), "херсонськ", "Херсон"),
    KHMELNYTSKYI("khmelnytskyi", 49.4229, 26.9871, listOf("хмельниччин"), "хмельницьк", "Хмельницький"),
    CHERKASY("cherkasy", 49.4444, 32.0598, listOf("черкащин"), "черкаськ", "Черкаси"),
    CHERNIVTSI("chernivtsi", 48.2921, 25.9358, listOf("чернівеччин", "буковин"), "чернівецьк", "Чернівці"),
    CHERNIHIV("chernihiv", 51.4982, 31.2893, listOf("чернігівщин"), "чернігівськ", "Чернігів"),
    CRIMEA("crimea", 44.9521, 34.1024, listOf("крим"), null, "Сімферополь");

    companion object {
        fun of(key: String?): Oblast? = entries.firstOrNull { it.key == key }
    }
}

package ua.surma

import org.junit.Assert.assertEquals
import org.junit.Test
import ua.surma.alerts.Translit

/** Examples from the official rules (resolution No. 55) and from road signs. */
class TranslitTest {
    @Test fun officialExamples() {
        for ((uk, en) in listOf(
            "Київ" to "Kyiv", "Харків" to "Kharkiv", "Запоріжжя" to "Zaporizhzhia", "Кропивницький" to "Kropyvnytskyi",
            "Хмельницький" to "Khmelnytskyi", "Згорани" to "Zghorany", "Розгон" to "Rozghon", "Єнакієве" to "Yenakiieve",
            "Гаївка" to "Haivka", "Їжакевич" to "Yizhakevych", "Кадиївка" to "Kadyivka", "Йосипівка" to "Yosypivka",
            "Стрий" to "Stryi", "Олексій" to "Oleksii", "Юрій" to "Yurii", "Корюківка" to "Koriukivka", "Яготин" to "Yahotyn",
            "Костянтин" to "Kostiantyn", "Знам'янка" to "Znamianka", "Феодосія" to "Feodosiia", "Щербухи" to "Shcherbukhy",
            "Гоща" to "Hoshcha", "Ґорґани" to "Gorgany", "Біла Церква" to "Bila Tserkva", "Кривий Ріг" to "Kryvyi Rih",
            "Івано-Франківськ" to "Ivano-Frankivsk", "Цвіткове" to "Tsvitkove", "Кам’янське" to "Kamianske",
        )) assertEquals(uk, en, Translit.latin(uk))
    }

    @Test fun otherCharactersStay() {
        assertEquals("Kyiv (2)", Translit.latin("Київ (2)"))
        assertEquals("", Translit.latin(""))
    }
}

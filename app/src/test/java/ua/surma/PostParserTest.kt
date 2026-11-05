package ua.surma

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import ua.surma.alerts.ChannelPage
import ua.surma.alerts.Direction
import ua.surma.alerts.Oblast
import ua.surma.alerts.PostParser
import ua.surma.alerts.Threat
import ua.surma.alerts.ThreatType

/**
 * REAL: posts of the channel word for word (4 November 2026).
 * STYLE: written in the channel's usual wording; to be replaced with real posts as they are collected.
 */
class PostParserTest {
    private fun parse(text: String) = PostParser.parse(text, TestPlaces.index)
    private fun one(text: String): Threat = parse(text).also { assertEquals(text, 1, it.size) }[0]

    // ── REAL ────────────────────────────────────────────────────────────
    @Test fun real_drones_two_directions() {
        val t = one("🛵 Ударні БпЛА на Цвіткове (Черкащина) з північного да східного напрямків.")
        assertEquals(ThreatType.DRONE, t.type)
        assertEquals("Цвіткове", t.target?.name)
        assertEquals(listOf(Oblast.CHERKASY), t.oblasts)
        assertEquals(listOf(Direction.N, Direction.E), t.from)
    }

    @Test fun real_jet_drone_from_east() {
        val t = one("🏍 Реактивний БпЛА на Кропивницький зі сходу.")
        assertEquals(ThreatType.JET_DRONE, t.type)
        assertEquals("Кропивницький", t.target?.name)
        assertEquals(listOf(Direction.E), t.from)
    }

    @Test fun real_jet_drones_vector() {
        val t = one("🏍 Реактивні БпЛА вектором руху на м. Запоріжжя з південного напрямку")
        assertEquals(ThreatType.JET_DRONE, t.type)
        assertEquals("Запоріжжя", t.target?.name)
        assertEquals(listOf(Direction.S), t.from)
    }

    // ── STYLE ───────────────────────────────────────────────────────────
    @Test fun group_on_oblast_course_to_city() {
        val t = one("Група ударних БпЛА на Сумщині, курсом на Конотоп.")
        assertEquals(ThreatType.DRONE, t.type)
        assertTrue(t.isGroup)
        assertEquals("Конотоп", t.target?.name)
        assertEquals(listOf(Oblast.SUMY), t.oblasts)
    }

    @Test fun ballistic_threat_no_place() {
        val t = one("⚠️ Загроза застосування балістичного озброєння з північно-східного напрямку!")
        assertEquals(ThreatType.BALLISTIC, t.type)
        assertNull(t.target)
        assertEquals(listOf(Direction.NE), t.from)
    }

    @Test fun high_speed_target_kyiv() {
        val t = one("🚀 Швидкісна ціль на Київ!")
        assertEquals(ThreatType.HIGH_SPEED, t.type)
        assertEquals("Київ", t.target?.name)
    }

    @Test fun missile_from_south() {
        val t = one("Ракета на Дніпро з півдня")
        assertEquals(ThreatType.MISSILE, t.type)
        assertEquals("Дніпро", t.target?.name)
        assertEquals(listOf(Direction.S), t.from)
    }

    @Test fun guided_bombs_oblast() {
        val t = one("Пуски КАБ на Сумщину")
        assertEquals(ThreatType.GUIDED_BOMB, t.type)
        assertEquals(listOf(Oblast.SUMY), t.oblasts)
    }

    @Test fun count_and_village_from_register() {
        val t = one("3х ударних БпЛА на Харківщині курсом на Кегичівку.")
        assertEquals(3, t.count)
        assertEquals(listOf(Oblast.KHARKIV), t.oblasts)
        assertEquals("Кегичівка", t.target?.name)
        assertNull(t.unresolvedTarget)
    }

    @Test fun unknown_place_stays_text() {
        val t = one("БпЛА на Харківщині курсом на Небувалівку.")
        assertNull(t.target)
        assertEquals("Небувалівку", t.unresolvedTarget)
    }

    @Test fun same_name_decided_by_region() {
        // There are many places called Іванівка; the region in the post picks the one
        val t = one("Ударні БпЛА на Іванівку (Львівщина)")
        assertEquals("Іванівка", t.target?.name)
        assertEquals(Oblast.LVIV, t.target?.oblast)
    }

    @Test fun villages_of_one_name_in_one_region_stay_text() {
        // Kharkiv region has eleven villages called Іванівка: no guess, but the region is known
        val t = one("Ударні БпЛА курсом на Іванівку (Харківщина)")
        assertNull(t.target)
        assertEquals("Іванівку", t.unresolvedTarget)
        assertEquals(listOf(Oblast.KHARKIV), t.oblasts)
    }

    @Test fun same_name_without_region_stays_text() {
        val t = one("Ударні БпЛА курсом на Іванівку")
        assertNull(t.target)
        assertEquals("Іванівку", t.unresolvedTarget)
    }

    @Test fun region_named_on_another_line() {
        val ts = parse("Львівщина:\nУдарні БпЛА курсом на Іванівку")
        assertEquals(Oblast.LVIV, ts.single().target?.oblast)
    }

    @Test fun district() {
        val t = one("Ударні БпЛА у Конотопському районі")
        assertEquals("Конотопський", t.target?.name)
        assertEquals(ua.surma.alerts.PlaceKind.DISTRICT, t.target?.kind)
        assertEquals(listOf(Oblast.SUMY), t.oblasts)
    }

    @Test fun regional_centre_wins_over_villages_of_the_same_name() {
        assertEquals(Oblast.MYKOLAIV, one("Ракета на Миколаїв").target?.oblast)
        assertEquals(Oblast.KYIV_CITY, one("Швидкісна ціль на Київ!").target?.oblast)
    }

    @Test fun village_needs_a_word_for_towards() {
        // "Мирне" is a village name too, but here it is only a word at the start of a line
        val t = one("БпЛА на Сумщині")
        assertNull(t.target)
        assertEquals(listOf(Oblast.SUMY), t.oblasts)
    }

    @Test fun city_without_cue() {
        assertEquals("Харків", one("Вибухи у Харкові — БпЛА").target?.name)
    }

    @Test fun all_clear_wins_over_ballistic() {
        assertEquals(ThreatType.ALL_CLEAR, one("Відбій загрози застосування балістики.").type)
    }

    @Test fun header_with_lines_inherits_type() {
        val ts = parse("Ударні БпЛА:\nЧернігівщина — курсом на Ніжин\nПолтавщина — курсом на Миргород")
        assertEquals(2, ts.size)
        assertTrue(ts.all { it.type == ThreatType.DRONE })
        assertEquals(listOf("Ніжин", "Миргород"), ts.map { it.target?.name })
        assertEquals(listOf(Oblast.CHERNIHIV), ts[0].oblasts)
    }

    @Test fun summary_report_is_skipped() {
        val text = "У ніч на 4 листопада противник атакував 120 ударними БпЛА.\n" +
            "Станом на 09:00 збито/подавлено 98 ворожих БпЛА на півночі, сході та півдні країни."
        assertTrue(PostParser.isReport(text))
        assertTrue(parse(text).isEmpty())
    }

    @Test fun multiword_city_genitive() {
        val t = one("Ворожі БпЛА в районі Кривого Рогу")
        assertEquals("Кривий Ріг", t.target?.name)
        assertEquals(listOf(Oblast.DNIPRO), t.oblasts)
    }

    @Test fun oblast_adjective_needs_word_oblast() {
        val t = one("БпЛА на півночі Сумської області")
        assertEquals(listOf(Oblast.SUMY), t.oblasts)
        assertNull(t.target)
    }

    @Test fun direction_word_is_not_a_target() {
        val t = one("Тактична авіація активна на Південно-Східному напрямку")
        assertEquals(ThreatType.AVIATION, t.type)
        assertNull(t.unresolvedTarget)
    }

    @Test fun direction_segment_stops_before_target() {
        val t = one("БпЛА з півночі курсом на Чернігів")
        assertEquals(listOf(Direction.N), t.from)
        assertEquals("Чернігів", t.target?.name)
    }

    @Test fun origin_is_not_threat_area() {
        val t = one("Швидкісна ціль з Криму на Миколаївщину!")
        assertEquals(listOf(Oblast.MYKOLAIV), t.oblasts)
    }

    @Test fun lines_without_a_kind_are_skipped() {
        assertTrue(parse("Слава Україні!\nДякуємо ППО").isEmpty())
    }

    @Test fun too_long_post_is_not_read() {
        assertTrue(parse("БпЛА на Київ ".repeat(500)).isEmpty())
    }
}

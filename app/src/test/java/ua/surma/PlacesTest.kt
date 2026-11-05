package ua.surma

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ua.surma.alerts.Declension
import ua.surma.alerts.Oblast
import ua.surma.alerts.PlaceIndex
import ua.surma.alerts.PlaceKind
import ua.surma.alerts.PostParser

class PlacesTest {
    private val index get() = TestPlaces.index

    /** Forms as the channel writes them -> the register's nominative. */
    private val forms = mapOf(
        "іванівку" to "іванівка", "іванівці" to "іванівка", "іванівки" to "іванівка",
        "харкова" to "харків", "харкові" to "харків", "харкову" to "харків", "києва" to "київ", "києві" to "київ",
        "миколаєва" to "миколаїв", "борисполя" to "бориспіль", "тернополі" to "тернопіль", "ковеля" to "ковель",
        "полтаву" to "полтава", "вінниці" to "вінниця", "олександрії" to "олександрія", "запоріжжі" to "запоріжжя",
        "кропивницького" to "кропивницький", "цвіткового" to "цвіткове", "рівному" to "рівне",
        "сумах" to "суми", "черкас" to "черкаси", "броварів" to "бровари", "чернівцях" to "чернівці",
        "ніжина" to "ніжин", "рогу" to "ріг", "розі" to "ріг", "білої" to "біла", "церкви" to "церква",
        "кам'янця" to "кам'янець", "ірпеня" to "ірпінь",
    )

    @Test fun formsLeadBackToTheNominative() {
        for ((written, nominative) in forms) {
            assertTrue("$written -> $nominative: ${Declension.nominatives(written)}", nominative in Declension.nominatives(written))
        }
    }

    @Test fun registerIsComplete() {
        // About 30 thousand places and every district; never a silently empty list
        assertTrue(index.size > 29_000)
        assertEquals(136, index.all().count { it.kind == PlaceKind.DISTRICT })
        assertTrue(index.named("кегичівка").any { it.oblast == Oblast.KHARKIV })
        assertTrue(index.named("конотопський").any { it.kind == PlaceKind.DISTRICT })
    }

    @Test fun everyRegionHasOneCentre() {
        // Kyiv region's centre is Kyiv, a region of its own; Crimea's points are mostly missing
        for (o in Oblast.entries - Oblast.KYIV_OBLAST - Oblast.CRIMEA) {
            val centres = index.all().filter { it.oblast == o }.filter { PlaceIndex.isRegionalCentre(it) }
            assertEquals("$o: ${centres.map { it.name }}", 1, centres.size)
        }
    }

    @Test fun choosingByRegion() {
        val ivanivky = index.named("іванівка")
        assertTrue(ivanivky.size > 50)
        assertEquals(null, index.choose(ivanivky, emptySet()))
        // Two cities called Миколаїв: the regional centre wins without a region; Lviv region picks the other
        val mykolaiv = index.named("миколаїв")
        assertEquals(Oblast.MYKOLAIV, index.choose(mykolaiv, emptySet())?.oblast)
        assertEquals(Oblast.LVIV, index.choose(mykolaiv, setOf(Oblast.LVIV))?.oblast)
        assertEquals(PlaceKind.CITY, index.choose(mykolaiv, setOf(Oblast.LVIV))?.kind)
    }

    @Test fun badLinesAreSkipped() {
        val i = PlaceIndex.parse("# x\nА\tM\tsumy\t50\t34\nБ\tQ\tsumy\t50\t34\nВ\tM\tnowhere\t50\t34\nГ\tM\tsumy\t0\t0\n\tM\tsumy\t50\t34\nshort\n")
        assertEquals(1, i.size)
        assertEquals(PostParser.normalize("А"), "а")
    }
}

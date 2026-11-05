package ua.surma

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ua.surma.alerts.ThreatType
import ua.surma.help.Help
import java.io.File

/** The "what to do" cards as shipped: complete in both languages, every picture there, only real numbers to call. */
class HelpTest {
    private fun read(lang: String) = Help.parse(File("src/main/assets/help/$lang.json").readText())

    @Test fun bothLanguagesHaveTheSameCards() {
        val uk = read("uk"); val en = read("en")
        assertEquals(listOf("air-alert", "ballistic", "drone", "bleeding", "unconscious"), uk.cards.map { it.id })
        assertEquals(uk.cards.map { it.id }, en.cards.map { it.id })
        assertEquals(uk.cards.map { it.steps.size }, en.cards.map { it.steps.size })
        assertEquals(uk.cards.map { it.call }, en.cards.map { it.call })
    }

    @Test fun everyCardSaysWhereItIsFrom() {
        for (h in listOf(read("uk"), read("en"))) for (c in h.cards) {
            assertTrue(c.id, c.source.isNotBlank())
            assertTrue(c.id, Regex("^\\d{4}-\\d{2}-\\d{2}$").matches(c.checkedOn))
            assertTrue(c.id, c.call.isEmpty() || c.call in Help.SERVICES)
        }
    }

    @Test fun everyPictureIsThere() {
        for (h in listOf(read("uk"), read("en"))) {
            for (c in h.cards) for (s in c.steps) if (s.figure.isNotEmpty()) assertTrue("${c.id} ${s.figure}", s.figure in h.figures)
            for (f in h.figures.values) for (p in f.pictures) assertTrue(p, File("src/main/assets/help/figures/$p.png").isFile)
        }
    }

    @Test fun threatsOpenAnExistingCard() {
        val ids = read("uk").cards.map { it.id }
        for (t in ThreatType.entries) Help.cardFor(t)?.let { assertTrue("$t -> $it", it in ids) }
        assertEquals(null, Help.cardFor(ThreatType.ALL_CLEAR))
    }

    @Test fun badCardsAreRefused() {
        val bad = """{"cards":[{"id":"x","title":"X","steps":[{"action":"a"}],"source":"s","checkedOn":"2026-01-01","call":"0800"}]}"""
        // An unknown number is never offered for calling
        assertEquals("", Help.parse(bad).cards.single().call)
    }
}

package ua.surma

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ua.surma.ui.theme.Look
import ua.surma.ui.theme.Mode
import ua.surma.ui.theme.Theme
import ua.surma.ui.theme.contrast
import ua.surma.ui.theme.mix

/** Every theme stays easy to read: text at least 4.5:1, shapes that must be seen at least 3:1 (WCAG AA). */
class PaletteTest {
    private val themes = Theme.entries.filter { it.palette != null }

    @Test
    fun textIsReadable() {
        for (t in themes) {
            val p = t.palette!!
            val primaryContainer = mix(p.surface, p.primary, 0.2)
            val secondaryContainer = mix(p.surface, p.secondary, 0.25)
            val pairs = mapOf(
                "onSurface/surface" to (p.onSurface to p.surface),
                "onSurface/background" to (p.onSurface to p.background),
                "onSurfaceVariant/surface" to (p.onSurfaceVariant to p.surface),
                "onSurfaceVariant/background" to (p.onSurfaceVariant to p.background),
                "onSurfaceVariant/surfaceVariant" to (p.onSurfaceVariant to p.surfaceVariant),
                "onPrimary/primary" to (p.onPrimary to p.primary),
                "primary/surface" to (p.primary to p.surface),
                "primary/background" to (p.primary to p.background),
                "error/surface" to (p.error to p.surface),
                "onSurface/primaryContainer" to (p.onSurface to primaryContainer),
                "onSurface/secondaryContainer" to (p.onSurface to secondaryContainer),
            )
            for ((name, c) in pairs) {
                val r = contrast(c.first, c.second)
                assertTrue("${t.key} $name is $r", r >= 4.5)
            }
        }
    }

    @Test
    fun shapesAreVisible() {
        for (t in themes) {
            val p = t.palette!!
            // The ornament and the progress bar
            assertTrue("${t.key} secondary/background", contrast(p.secondary, p.background) >= 3.0)
            assertTrue("${t.key} primary/background", contrast(p.primary, p.background) >= 3.0)
        }
    }

    @Test
    fun dayAndNightAreSeparate() {
        assertTrue(Theme.DAY.none { it.dark })
        assertTrue(Theme.NIGHT.all { it.dark })
        assertEquals(Theme.DAY.size, Theme.NIGHT.size)
    }

    @Test
    fun lookPicksByMode() {
        val look = Look(day = Theme.GLYNA, night = Theme.ZHAR)
        assertEquals(Theme.GLYNA, look.current(phoneIsDark = false))
        assertEquals(Theme.ZHAR, look.current(phoneIsDark = true))
        assertEquals(Theme.GLYNA, look.copy(mode = Mode.LIGHT).current(phoneIsDark = true))
        assertEquals(Theme.ZHAR, look.copy(mode = Mode.DARK).current(phoneIsDark = false))
        assertEquals(Theme.KREYDA, Theme.of("unknown", Theme.KREYDA))
    }

    @Test
    fun phoneColoursByDefault() {
        val look = Look()
        assertEquals(Mode.AUTO, look.mode)
        assertEquals(Theme.SYSTEM, look.day)
        assertEquals(Theme.SYSTEM_DARK, look.night)
        assertEquals(Theme.SYSTEM, Theme.DAY.first())
        assertEquals(Theme.SYSTEM_DARK, Theme.NIGHT.first())
    }

    @Test
    fun contrastMatchesWcag() {
        assertEquals(21.0, contrast(0xFF000000, 0xFFFFFFFF), 0.01)
        assertEquals(1.0, contrast(0xFF777777, 0xFF777777), 0.001)
    }
}

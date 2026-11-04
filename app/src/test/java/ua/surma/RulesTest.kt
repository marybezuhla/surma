package ua.surma

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import ua.surma.alerts.ChannelPost
import ua.surma.alerts.Oblast
import ua.surma.alerts.Rules
import ua.surma.alerts.Threat
import ua.surma.alerts.ThreatType

class RulesTest {
    @Test fun onlyTelegramOverHttps() {
        assertNotNull(Rules.allowedUri("https://t.me/s/kpszsu"))
        assertNotNull(Rules.allowedUri(Rules.pageUrl(123)))
        assertNull(Rules.allowedUri("http://t.me/s/kpszsu"))
        assertNull(Rules.allowedUri("https://t.me.evil.example/s/kpszsu"))
        assertNull(Rules.allowedUri("https://user@t.me/s/kpszsu"))
        assertNull(Rules.allowedUri("https://t.me:8443/s/kpszsu"))
        assertNull(Rules.allowedUri("https://telegram.org/"))
        assertNull(Rules.allowedUri("not a url"))
    }

    @Test fun pageUrl() {
        assertEquals("https://t.me/s/kpszsu", Rules.pageUrl())
        assertEquals("https://t.me/s/kpszsu", Rules.pageUrl(0))
        assertEquals("https://t.me/s/kpszsu?after=42", Rules.pageUrl(42))
    }

    @Test fun paceIsFasterWhileActive() {
        val p = Rules.Pace.NORMAL
        assertEquals(30, Rules.nextDelaySeconds(p, secondsSinceLastThreat = 60))
        assertEquals(90, Rules.nextDelaySeconds(p, secondsSinceLastThreat = 3 * 3600))
        assertEquals(90, Rules.nextDelaySeconds(p, secondsSinceLastThreat = null))
        assertEquals(20, Rules.nextDelaySeconds(Rules.Pace.FAST, 10))
        assertEquals(180, Rules.nextDelaySeconds(Rules.Pace.SAVER, null))
        assertEquals(Rules.Pace.NORMAL, Rules.Pace.of("nonsense"))
    }

    @Test fun backoffIsCapped() {
        val p = Rules.Pace.NORMAL
        assertEquals(180, Rules.nextDelaySeconds(p, null, failures = 1))
        assertEquals(Rules.MAX_BACKOFF_SECONDS, Rules.nextDelaySeconds(p, null, failures = 50))
        assertEquals(Rules.MAX_BACKOFF_SECONDS, Rules.nextDelaySeconds(Rules.Pace.SAVER, null, failures = 9))
    }

    @Test fun newPostsOldestFirst() {
        val posts = listOf(ChannelPost(5, 0, "a"), ChannelPost(3, 0, "b"), ChannelPost(7, 0, "c"), ChannelPost(7, 0, "c"))
        assertEquals(listOf(5L, 7L), Rules.newPosts(posts, lastId = 3).map { it.id })
        assertTrue(Rules.newPosts(posts, lastId = 7).isEmpty())
    }

    @Test fun onlyFreshPostsSound() {
        val now = 1_800_000_000L
        assertTrue(Rules.isFresh(ChannelPost(1, now - 60, ""), now))
        assertFalse(Rules.isFresh(ChannelPost(1, now - 3600, ""), now))
        assertFalse(Rules.isFresh(ChannelPost(1, null, ""), now))
        // A phone clock a little behind is fine; a post "from the future" by hours is not trusted
        assertTrue(Rules.isFresh(ChannelPost(1, now + 60, ""), now))
        assertFalse(Rules.isFresh(ChannelPost(1, now + 7200, ""), now))
    }

    private fun threat(type: ThreatType, vararg o: Oblast) = Threat(type, oblasts = o.toList(), sourceLine = "")

    @Test fun concerns() {
        val sumy = setOf(Oblast.SUMY)
        assertTrue(Rules.concerns(threat(ThreatType.DRONE, Oblast.SUMY), sumy, nationwide = true))
        assertFalse(Rules.concerns(threat(ThreatType.DRONE, Oblast.LVIV), sumy, nationwide = true))
        assertTrue(Rules.concerns(threat(ThreatType.BALLISTIC), sumy, nationwide = true))
        assertFalse(Rules.concerns(threat(ThreatType.BALLISTIC), sumy, nationwide = false))
        // A drone with no region is not a nationwide threat
        assertFalse(Rules.concerns(threat(ThreatType.DRONE), sumy, nationwide = true))
        // Nothing chosen: everything
        assertTrue(Rules.concerns(threat(ThreatType.DRONE, Oblast.LVIV), emptySet(), nationwide = false))
        // Kyiv: the city and the region go together
        assertTrue(Rules.concerns(threat(ThreatType.DRONE, Oblast.KYIV_OBLAST), setOf(Oblast.KYIV_CITY), nationwide = false))
        assertTrue(Rules.concerns(threat(ThreatType.DRONE, Oblast.KYIV_CITY), setOf(Oblast.KYIV_OBLAST), nationwide = false))
    }

    @Test fun keysAreUnique() {
        assertEquals(Oblast.entries.size, Oblast.entries.map { it.key }.toSet().size)
        assertEquals(ThreatType.entries.size, ThreatType.entries.map { it.key }.toSet().size)
        for (o in Oblast.entries) assertEquals(o, Oblast.of(o.key))
    }
}

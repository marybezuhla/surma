package ua.surma

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ua.surma.alerts.Direction
import ua.surma.alerts.Oblast
import ua.surma.alerts.Place
import ua.surma.alerts.Rules
import ua.surma.alerts.Threat
import ua.surma.alerts.ThreatType
import ua.surma.watch.Feed
import ua.surma.watch.FeedItem

/** The list kept for the home screen survives a round trip, and a damaged or absurd file cannot break it. */
class FeedTest {
    private val now = 1_800_000_000L

    private val item = FeedItem(
        postId = 42, epochSeconds = now - 60,
        threat = Threat(
            ThreatType.DRONE, count = 3, oblasts = listOf(Oblast.SUMY),
            target = Place("Конотоп", Oblast.SUMY, 51.24, 33.2), from = listOf(Direction.N, Direction.E),
            sourceLine = "3х ударних БпЛА на Сумщині курсом на Конотоп з півночі та сходу",
        ),
    )

    @Test fun roundTrip() {
        assertEquals(listOf(item), Feed.decode(Feed.encode(listOf(item)), now))
    }

    @Test fun oldAndDoubleItemsAreDropped() {
        val old = item.copy(postId = 1, epochSeconds = now - Rules.KEEP_SECONDS - 10)
        val kept = Feed.trim(listOf(old, item, item), now)
        assertEquals(listOf(item), kept)
    }

    @Test fun newestFirstAndCapped() {
        val many = (1..500).map { item.copy(postId = it.toLong()) }
        val kept = Feed.trim(many, now)
        assertEquals(Rules.MAX_KEPT, kept.size)
        assertEquals(500L, kept.first().postId)
    }

    @Test fun absurdValuesAreRefused() {
        val json = """[{"post":1,"time":${now - 5},"type":"drone","count":999999999,"oblasts":["nowhere","sumy"],
            "target":{"name":"X","oblast":"sumy","lat":0,"lon":0},"from":["up"],"line":"${"a".repeat(5000)}"},
            {"type":"unknown"}, 5, null]"""
        val items = Feed.decode(json, now)
        assertEquals(1, items.size)
        val t = items.single().threat
        assertEquals(null, t.count)
        assertEquals(listOf(Oblast.SUMY), t.oblasts)
        // A point outside Ukraine is not shown on a map
        assertEquals(null, t.target)
        assertTrue(t.from.isEmpty())
        assertEquals(Rules.MAX_LINE_CHARS, t.sourceLine.length)
    }
}

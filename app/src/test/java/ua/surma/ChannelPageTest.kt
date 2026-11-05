package ua.surma

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import ua.surma.alerts.ChannelPage
import ua.surma.alerts.PostParser

/** The channel's public page, in the shape t.me/s serves it (shortened). */
class ChannelPageTest {
    private val page = """
        <div class="tgme_widget_message_wrap js-widget_message_wrap"><div class="tgme_widget_message text_not_supported_wrap js-widget_message" data-post="kpszsu/101" data-view="x">
          <div class="tgme_widget_message_bubble">
            <div class="tgme_widget_message_text js-message_text" dir="auto"><i class="emoji" style="background-image:url('//telegram.org/img/emoji/40/F09F9BB5.png')"><b>🛵</b></i> Ударні БпЛА на Сумщині<br/>курсом на Шостку.</div>
            <div class="tgme_widget_message_footer"><a class="tgme_widget_message_date" href="https://t.me/kpszsu/101"><time datetime="2026-11-04T04:40:00+00:00" class="time">04:40</time></a></div>
          </div></div></div>
        <div class="tgme_widget_message js-widget_message" data-post="kpszsu/100">
            <div class="tgme_widget_message_text js-message_text" dir="auto">🚀 Швидкісна ціль на Київ&#33; &amp; ще &quot;щось&quot;</div>
            <time datetime="2026-11-04T04:30:00+00:00" class="time">04:30</time></div>
        <div class="tgme_widget_message js-widget_message" data-post="otherchannel/5">
            <div class="tgme_widget_message_text js-message_text">БпЛА на Київ</div></div>
        <div class="tgme_widget_message js-widget_message" data-post="kpszsu/102">
            <div class="tgme_widget_message_photo_wrap"></div>
            <time datetime="not a date">?</time></div>
    """.trimIndent()

    @Test fun readsPostsInOrder() {
        val posts = ChannelPage.parse(page)
        // The post of another channel and the post without text are left out
        assertEquals(listOf(100L, 101L), posts.map { it.id })
        assertEquals(1793767200L, posts[1].epochSeconds)
        assertEquals("🛵 Ударні БпЛА на Сумщині\nкурсом на Шостку.", posts[1].text)
        assertEquals("🚀 Швидкісна ціль на Київ! & ще \"щось\"", posts[0].text)
    }

    @Test fun wrappedLineBecomesOneThreat() {
        val post = ChannelPage.parse(page).single { it.id == 101L }
        assertEquals("Шостка", PostParser.parse(post.text, TestPlaces.index).single().target?.name)
    }

    @Test fun unreadableTimeIsNull() {
        val html = """<div data-post="kpszsu/7"><div class="tgme_widget_message_text">БпЛА</div><time datetime="yesterday-ish">x</time></div>"""
        assertNull(ChannelPage.parse(html).single().epochSeconds)
    }

    @Test fun nothingOnAnUnknownPage() {
        assertTrue(ChannelPage.parse("<html><body>Telegram is down</body></html>").isEmpty())
        assertTrue(ChannelPage.parse("").isEmpty())
    }

    @Test fun badEntitiesAreDropped() {
        assertEquals("ab", ChannelPage.plain("a&#xD800;&#9999999;b"))
        assertEquals("a\nb", ChannelPage.plain("a <br> b"))
    }
}

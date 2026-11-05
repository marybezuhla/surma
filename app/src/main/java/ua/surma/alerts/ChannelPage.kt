package ua.surma.alerts

import java.time.OffsetDateTime

/**
 * Reads the public web page of a Telegram channel (t.me/s/<channel>): each post's number, time and text.
 * A few plain patterns instead of an HTML library: the page is simple, and a library would be another
 * dependency to trust. If Telegram changes the page, this returns nothing and the tests show it.
 */
object ChannelPage {
    private val POST = Regex("data-post=\"([A-Za-z0-9_]{1,64})/(\\d{1,12})\"")
    private val TEXT = Regex("<div class=\"tgme_widget_message_text[^\"]*\"[^>]*>(.*?)</div>", RegexOption.DOT_MATCHES_ALL)
    private val TIME = Regex("<time[^>]*datetime=\"([^\"]{10,40})\"")
    private val BR = Regex("<br\\s*/?>", RegexOption.IGNORE_CASE)
    private val TAG = Regex("<[^>]{0,2000}>")
    private val ENTITY = Regex("&(#x[0-9a-fA-F]{1,6}|#\\d{1,7}|amp|lt|gt|quot|apos|nbsp);")

    fun parse(html: String, channel: String = Rules.CHANNEL): List<ChannelPost> {
        val starts = POST.findAll(html).toList()
        return starts.mapIndexedNotNull { i, m ->
            if (!m.groupValues[1].equals(channel, ignoreCase = true)) return@mapIndexedNotNull null
            val id = m.groupValues[2].toLongOrNull() ?: return@mapIndexedNotNull null
            // This post's part of the page: up to the next post
            val end = starts.getOrNull(i + 1)?.range?.first ?: html.length
            val block = html.substring(m.range.last + 1, end)
            val raw = TEXT.find(block)?.groupValues?.get(1) ?: return@mapIndexedNotNull null
            val text = plain(raw).take(Rules.MAX_POST_CHARS + 1)
            if (text.isBlank()) return@mapIndexedNotNull null
            ChannelPost(id, TIME.find(block)?.groupValues?.get(1)?.let(::epoch), text)
        }.distinctBy { it.id }.sortedBy { it.id }
    }

    /** HTML to plain text: line breaks kept, tags dropped, the usual entities turned back into characters. */
    fun plain(html: String): String {
        val noTags = TAG.replace(BR.replace(html, "\n"), "")
        return ENTITY.replace(noTags) { m ->
            when (val e = m.groupValues[1]) {
                "amp" -> "&"
                "lt" -> "<"
                "gt" -> ">"
                "quot" -> "\""
                "apos" -> "'"
                "nbsp" -> " "
                else -> {
                    val code = if (e.startsWith("#x")) e.drop(2).toIntOrNull(16) else e.drop(1).toIntOrNull()
                    if (code != null && Character.isValidCodePoint(code) && code !in 0xD800..0xDFFF) String(Character.toChars(code)) else ""
                }
            }
        }.lines().joinToString("\n") { it.trim() }.trim()
    }

    private fun epoch(s: String): Long? = try {
        OffsetDateTime.parse(s).toEpochSecond()
    } catch (_: Exception) {
        // A time that cannot be read only means the post is not "fresh": it is listed, without a sound
        null
    }
}

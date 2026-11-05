package ua.surma.net

import ua.surma.alerts.ChannelPage
import ua.surma.alerts.ChannelPost
import ua.surma.alerts.Rules
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URI
import javax.net.ssl.HttpsURLConnection

/** Telegram answered, but not with the channel's page (changed page, blocked, or "too many requests"). */
class PageException(message: String) : IOException(message)

/** Reads the channel's public page. Only t.me, only HTTPS, redirects followed by hand so any other host is refused. */
object Channel {
    fun posts(afterId: Long?): List<ChannelPost> {
        val body = fetch(Rules.pageUrl(afterId))
        val html = String(body, Charsets.UTF_8)
        val posts = ChannelPage.parse(html)
        // An empty page is normal with "after" when nothing is new; without it, the page itself is wrong
        if (posts.isEmpty() && afterId == null) throw PageException("No posts on the page")
        return posts
    }

    private fun fetch(start: String): ByteArray {
        var url = start
        repeat(Rules.MAX_REDIRECTS + 1) {
            val uri = Rules.allowedUri(url) ?: throw IOException("Refused to connect outside t.me")
            val conn = uri.toURL().openConnection() as HttpsURLConnection
            try {
                conn.instanceFollowRedirects = false
                conn.connectTimeout = Rules.TIMEOUT_MS
                conn.readTimeout = Rules.TIMEOUT_MS
                conn.useCaches = false
                // A neutral name: nothing about the phone or its owner. Android asks for a compressed page by itself.
                conn.setRequestProperty("User-Agent", "Surma")
                conn.setRequestProperty("Accept", "text/html")
                when (val code = conn.responseCode) {
                    HttpURLConnection.HTTP_OK -> return readLimited(conn.inputStream, Rules.MAX_PAGE_BYTES)
                    301, 302, 303, 307, 308 -> {
                        val next = conn.getHeaderField("Location")
                        if (next.isNullOrEmpty()) throw IOException("Redirect without a target")
                        url = try {
                            URI(url).resolve(next).toString()
                        } catch (_: IllegalArgumentException) {
                            throw IOException("Bad redirect")
                        }
                    }
                    else -> throw PageException("t.me answered $code")
                }
            } finally {
                conn.disconnect()
            }
        }
        throw IOException("Too many redirects")
    }

    private fun readLimited(input: InputStream, max: Int): ByteArray = input.use {
        val out = ByteArrayOutputStream()
        val buf = ByteArray(16 * 1024)
        while (true) {
            val n = it.read(buf)
            if (n < 0) break
            if (out.size() + n > max) throw IOException("Page too large")
            out.write(buf, 0, n)
        }
        out.toByteArray()
    }
}

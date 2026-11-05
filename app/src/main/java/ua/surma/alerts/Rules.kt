package ua.surma.alerts

import java.net.URI

/** Every limit and rule of Surma in one place, with no Android code, so it can be tested on a computer. */
object Rules {
    /** The Air Force of the Armed Forces of Ukraine, its public channel. */
    const val CHANNEL = "kpszsu"

    /** The only host Surma ever connects to. */
    val ALLOWED_HOSTS = setOf("t.me")

    /** The channel's public web page: the newest posts, or the ones after [afterId]. */
    fun pageUrl(afterId: Long? = null): String =
        "https://t.me/s/$CHANNEL" + (afterId?.takeIf { it > 0 }?.let { "?after=$it" } ?: "")

    /** Parsed once; the same object is used to connect, so the check and the connection agree. */
    fun allowedUri(url: String): URI? {
        val uri = try { URI(url) } catch (_: Exception) { return null }
        val ok = uri.scheme == "https" &&
            uri.host != null && uri.host.lowercase() in ALLOWED_HOSTS &&
            uri.rawUserInfo == null &&
            (uri.port == -1 || uri.port == 443)
        return uri.takeIf { ok }
    }

    /** The page is far smaller; anything bigger is not read. */
    const val MAX_PAGE_BYTES = 2 * 1024 * 1024
    const val MAX_REDIRECTS = 3
    const val TIMEOUT_MS = 15_000

    /** A post longer than this is not a real alert: it is not read. */
    const val MAX_POST_CHARS = 4_000
    const val MAX_LINE_CHARS = 500
    const val MAX_THREATS_PER_POST = 20
    const val MAX_COUNT = 999

    /** Posts older than this (for example, on the first check) are listed, but no alert sounds for them. */
    const val MAX_ALERT_AGE_SECONDS = 15 * 60L

    /** What the home screen keeps: the last hours, and never more than this many. */
    const val KEEP_SECONDS = 12 * 3600L
    const val MAX_KEPT = 150

    /** How often to check. Faster while threats come, slower when it is quiet. */
    enum class Pace(val key: String, val activeSeconds: Long, val quietSeconds: Long) {
        FAST("fast", 20, 45),
        NORMAL("normal", 30, 90),
        SAVER("saver", 60, 180);

        companion object {
            fun of(key: String?): Pace = entries.firstOrNull { it.key == key } ?: NORMAL
        }
    }

    /** "Active" lasts this long after the last threat of any kind. */
    const val ACTIVE_WINDOW_SECONDS = 30 * 60L

    /** After failed checks, wait longer, but never more than this: alerts matter more than the battery. */
    const val MAX_BACKOFF_SECONDS = 5 * 60L

    fun nextDelaySeconds(pace: Pace, secondsSinceLastThreat: Long?, failures: Int = 0): Long {
        val active = secondsSinceLastThreat != null && secondsSinceLastThreat in 0..ACTIVE_WINDOW_SECONDS
        val base = if (active) pace.activeSeconds else pace.quietSeconds
        if (failures <= 0) return base
        val shift = failures.coerceAtMost(4)
        return (base shl shift).coerceAtMost(MAX_BACKOFF_SECONDS).coerceAtLeast(base)
    }

    /** Posts not seen yet, oldest first. */
    fun newPosts(posts: List<ChannelPost>, lastId: Long): List<ChannelPost> =
        posts.filter { it.id > lastId }.distinctBy { it.id }.sortedBy { it.id }

    /** Only fresh posts sound: not the backlog on the first check or after the phone was off. */
    fun isFresh(post: ChannelPost, nowSeconds: Long): Boolean {
        val t = post.epochSeconds ?: return false
        return nowSeconds - t in -120..MAX_ALERT_AGE_SECONDS
    }

    /**
     * Whether a threat concerns the owner: one of the chosen regions, or (if wanted) a threat for the whole country
     * that names no region at all. Nothing chosen means every region.
     */
    fun concerns(threat: Threat, chosen: Set<Oblast>, nationwide: Boolean): Boolean {
        if (chosen.isEmpty()) return true
        if (threat.oblasts.any { it in chosen }) return true
        // Kyiv the city and the region around it are chosen separately, but a threat to one is news for the other
        if (Oblast.KYIV_CITY in chosen && Oblast.KYIV_OBLAST in threat.oblasts) return true
        if (Oblast.KYIV_OBLAST in chosen && Oblast.KYIV_CITY in threat.oblasts) return true
        return nationwide && threat.oblasts.isEmpty() && threat.type.nationwide
    }
}

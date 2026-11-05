package ua.surma.watch

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import ua.surma.alerts.Direction
import ua.surma.alerts.Oblast
import ua.surma.alerts.Place
import ua.surma.alerts.PlaceKind
import ua.surma.alerts.Rules
import ua.surma.alerts.Threat
import ua.surma.alerts.ThreatType
import java.io.File

/** One threat on the home screen: from which post, when. */
data class FeedItem(val postId: Long, val epochSeconds: Long, val threat: Threat)

/**
 * The latest threats, for the home screen: the last [Rules.KEEP_SECONDS], at most [Rules.MAX_KEPT].
 * Only what the public channel posted; kept in Surma's private files, never backed up.
 */
object Feed {
    private const val FILE = "feed.json"
    private const val MAX_FILE_BYTES = 512 * 1024L

    @Synchronized
    fun load(context: Context, nowSeconds: Long = System.currentTimeMillis() / 1000): List<FeedItem> = try {
        val f = File(context.filesDir, FILE)
        if (!f.exists() || f.length() > MAX_FILE_BYTES) emptyList() else decode(f.readText(), nowSeconds)
    } catch (_: Exception) {
        // A damaged file only empties the list; the next check fills it again
        emptyList()
    }

    @Synchronized
    fun add(context: Context, items: List<FeedItem>, nowSeconds: Long) {
        if (items.isEmpty()) return
        val all = trim(load(context, nowSeconds) + items, nowSeconds)
        try {
            val tmp = File(context.filesDir, "$FILE.tmp")
            tmp.writeText(encode(all))
            tmp.renameTo(File(context.filesDir, FILE))
        } catch (_: Exception) {
            // Only the list on screen: alerts were already sent
        }
    }

    /** Newest first, nothing older than the limit, nothing twice. */
    fun trim(items: List<FeedItem>, nowSeconds: Long): List<FeedItem> =
        items.filter { nowSeconds - it.epochSeconds <= Rules.KEEP_SECONDS }
            .distinctBy { Triple(it.postId, it.threat.sourceLine, it.threat.type) }
            .sortedWith(compareByDescending<FeedItem> { it.postId }.thenBy { it.threat.sourceLine })
            .take(Rules.MAX_KEPT)

    fun encode(items: List<FeedItem>): String = JSONArray().apply {
        for (i in items) put(JSONObject().apply {
            val t = i.threat
            put("post", i.postId)
            put("time", i.epochSeconds)
            put("type", t.type.key)
            t.count?.let { put("count", it) }
            if (t.isGroup) put("group", true)
            put("oblasts", JSONArray(t.oblasts.map { it.key }))
            t.target?.let { p ->
                put("target", JSONObject().put("name", p.name).put("oblast", p.oblast.key).put("lat", p.lat).put("lon", p.lon)
                    .put("kind", p.kind.code.toString()))
            }
            t.unresolvedTarget?.let { put("unknown", it) }
            put("from", JSONArray(t.from.map { it.key }))
            put("line", t.sourceLine)
        })
    }.toString()

    fun decode(json: String, nowSeconds: Long): List<FeedItem> {
        val a = JSONArray(json)
        val out = ArrayList<FeedItem>()
        for (n in 0 until minOf(a.length(), Rules.MAX_KEPT)) {
            val o = a.optJSONObject(n) ?: continue
            val type = ThreatType.of(o.optString("type")) ?: continue
            fun keys(name: String) = o.optJSONArray(name)?.let { arr -> (0 until minOf(arr.length(), 30)).map { arr.optString(it) } } ?: emptyList()
            val target = o.optJSONObject("target")?.let { p ->
                val oblast = Oblast.of(p.optString("oblast")) ?: return@let null
                val kind = p.optString("kind").singleOrNull()?.let { PlaceKind.of(it) } ?: PlaceKind.CITY
                Place(p.optString("name").take(80), oblast, p.optDouble("lat"), p.optDouble("lon"), kind)
                    .takeIf { it.name.isNotEmpty() && it.lat in 44.0..53.0 && it.lon in 22.0..41.0 }
            }
            out += FeedItem(
                postId = o.optLong("post"),
                epochSeconds = o.optLong("time"),
                threat = Threat(
                    type = type,
                    count = o.optInt("count", 0).takeIf { it in 1..Rules.MAX_COUNT },
                    isGroup = o.optBoolean("group"),
                    oblasts = keys("oblasts").mapNotNull { Oblast.of(it) },
                    target = target,
                    unresolvedTarget = o.optString("unknown", "").take(80).ifEmpty { null },
                    from = keys("from").mapNotNull { Direction.of(it) },
                    sourceLine = o.optString("line").take(Rules.MAX_LINE_CHARS),
                ),
            )
        }
        return trim(out, nowSeconds)
    }
}

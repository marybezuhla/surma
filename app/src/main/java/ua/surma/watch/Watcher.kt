package ua.surma.watch

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ua.surma.alerts.PostParser
import ua.surma.alerts.Rules
import ua.surma.net.Channel
import ua.surma.net.PageException
import java.io.IOException

/** One check of the channel: read new posts, keep them for the screen, alert about the ones that concern the owner. */
object Watcher {
    /** Changes after every check, so an open screen redraws. */
    private val _changed = MutableStateFlow(0L)
    val changed: StateFlow<Long> = _changed

    fun check(context: Context): WatchState {
        val now = System.currentTimeMillis() / 1000
        val before = Store.state(context)
        val settings = Store.settings(context)
        val after = try {
            val posts = Channel.posts(afterId = before.lastId.takeIf { it > 0 })
            val fresh = Rules.newPosts(posts, before.lastId)
            val items = fresh.flatMap { p -> PostParser.parse(p.text, Places.get(context)).map { FeedItem(p.id, p.epochSeconds ?: now, it) } }
            Feed.add(context, items, now)
            for (p in fresh) {
                if (!Rules.isFresh(p, now)) continue
                val mine = items.filter { it.postId == p.id && Rules.concerns(it.threat, settings.oblasts, settings.nationwide) }
                Alerts.threat(context, p.id, mine.map { it.threat })
            }
            before.copy(
                lastId = maxOf(before.lastId, fresh.maxOfOrNull { it.id } ?: 0),
                lastCheck = now,
                lastOk = now,
                // Any threat anywhere makes the next checks faster for a while
                lastThreat = maxOf(before.lastThreat, items.maxOfOrNull { it.epochSeconds.coerceAtMost(now) } ?: 0),
                failures = 0,
                problem = null,
            )
        } catch (e: PageException) {
            before.copy(lastCheck = now, failures = before.failures + 1, problem = Problem.PAGE)
        } catch (e: IOException) {
            before.copy(lastCheck = now, failures = before.failures + 1, problem = Problem.NETWORK)
        }
        Store.saveState(context, after)
        _changed.value = now
        return after
    }

    /** Seconds until the next check, by the chosen pace and how recent the last threat was. */
    fun nextDelay(settings: WatchSettings, state: WatchState, nowSeconds: Long): Long {
        val since = state.lastThreat.takeIf { it > 0 }?.let { nowSeconds - it }
        return Rules.nextDelaySeconds(settings.pace, since, state.failures)
    }
}

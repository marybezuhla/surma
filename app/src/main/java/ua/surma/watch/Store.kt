package ua.surma.watch

import android.content.Context
import ua.surma.alerts.Oblast
import ua.surma.alerts.Rules

/** What the owner chose. Watching is off until turned on. */
data class WatchSettings(
    val enabled: Boolean = false,
    val oblasts: Set<Oblast> = emptySet(),
    val nationwide: Boolean = true,
    val pace: Rules.Pace = Rules.Pace.NORMAL,
)

/** How watching went: the newest post read, when the last check and the last threat were, and any trouble. */
data class WatchState(
    val lastId: Long = 0,
    val lastCheck: Long = 0,
    val lastOk: Long = 0,
    val lastThreat: Long = 0,
    val failures: Int = 0,
    val problem: Problem? = null,
)

enum class Problem { NETWORK, PAGE }

/**
 * Kept on this phone only, in Surma's private storage (never backed up: see data_extraction_rules).
 * Unreadable means the defaults: Surma then starts as on the first day, with watching off.
 */
object Store {
    private const val FILE = "watch"

    private fun prefs(context: Context) = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun settings(context: Context): WatchSettings = try {
        val p = prefs(context)
        WatchSettings(
            enabled = p.getBoolean("enabled", false),
            oblasts = (p.getStringSet("oblasts", null) ?: emptySet()).mapNotNull { Oblast.of(it) }.toSet(),
            nationwide = p.getBoolean("nationwide", true),
            pace = Rules.Pace.of(p.getString("pace", null)),
        )
    } catch (_: Exception) {
        WatchSettings()
    }

    fun save(context: Context, s: WatchSettings) {
        try {
            prefs(context).edit()
                .putBoolean("enabled", s.enabled)
                .putStringSet("oblasts", s.oblasts.map { it.key }.toSet())
                .putBoolean("nationwide", s.nationwide)
                .putString("pace", s.pace.key)
                .apply()
        } catch (_: Exception) {
            // Only a preference: watching itself still follows the switch on screen until the app is closed
        }
    }

    fun state(context: Context): WatchState = try {
        val p = prefs(context)
        WatchState(
            lastId = p.getLong("last_id", 0),
            lastCheck = p.getLong("last_check", 0),
            lastOk = p.getLong("last_ok", 0),
            lastThreat = p.getLong("last_threat", 0),
            failures = p.getInt("failures", 0).coerceIn(0, 1000),
            problem = p.getString("problem", null)?.let { k -> Problem.entries.firstOrNull { it.name == k } },
        )
    } catch (_: Exception) {
        WatchState()
    }

    fun saveState(context: Context, s: WatchState) {
        try {
            prefs(context).edit()
                .putLong("last_id", s.lastId)
                .putLong("last_check", s.lastCheck)
                .putLong("last_ok", s.lastOk)
                .putLong("last_threat", s.lastThreat)
                .putInt("failures", s.failures)
                .putString("problem", s.problem?.name)
                .commit()
        } catch (_: Exception) {
            // The next check reads the page again; at worst a post is listed twice, never lost
        }
    }
}

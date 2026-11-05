package ua.surma.watch

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Watching: a foreground service (with its quiet notification) that checks the channel when an alarm wakes it.
 * Between checks nothing runs and the phone may sleep; each check keeps it awake for a few seconds only.
 */
class WatchService : Service() {
    // A check that fails in a way nobody foresaw is skipped, never a reason to close Surma
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO + CoroutineExceptionHandler { _, _ -> })
    private var job: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        try {
            Alerts.createChannels(this)
            val note = Alerts.watching(this, Store.state(this))
            // The "special use" type exists from Android 14; on Android 12 and 13 a type the phone does not
            // know is refused and would close Surma, so there the service starts without one
            if (Build.VERSION.SDK_INT >= 34) {
                startForeground(Alerts.WATCH_ID, note, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            } else {
                startForeground(Alerts.WATCH_ID, note)
            }
            running = this
        } catch (_: Exception) {
            // Android refused to start watching now (for example from the background): Surma stays open, and
            // watching starts the next time Surma is opened
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (running !== this || !Store.settings(this).enabled) {
            stop(this)
            return START_NOT_STICKY
        }
        tick()
        // Android may restart it after stopping it for memory; it then checks again at once
        return START_STICKY
    }

    /** One check now (unless one is already under way), then the next one is set. */
    fun tick() {
        if (job?.isActive == true) return
        val wake = getSystemService(PowerManager::class.java)
            ?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "surma:check")
            ?.apply { setReferenceCounted(false); acquire(WAKE_MS) }
        job = scope.launch {
            try {
                val state = try {
                    Watcher.check(this@WatchService)
                } catch (_: Exception) {
                    // A page Surma cannot make sense of must never stop watching: the next check comes as usual
                    Store.state(this@WatchService)
                }
                Alerts.updateWatching(this@WatchService, state)
                val settings = Store.settings(this@WatchService)
                if (settings.enabled) schedule(this@WatchService, Watcher.nextDelay(settings, state, System.currentTimeMillis() / 1000))
            } finally {
                if (wake?.isHeld == true) wake.release()
            }
        }
    }

    override fun onDestroy() {
        if (running === this) running = null
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val WAKE_MS = 45_000L

        /** The running service, if any: an alarm then asks it to check, without starting a service from the background. */
        @Volatile private var running: WatchService? = null

        fun start(context: Context) {
            try {
                context.startForegroundService(Intent(context, WatchService::class.java))
            } catch (_: Exception) {
                // Android refused to start it from the background (rare): it starts the next time Surma is opened
            }
        }

        fun stop(context: Context) {
            cancel(context)
            context.stopService(Intent(context, WatchService::class.java))
            context.getSystemService(android.app.NotificationManager::class.java)?.cancel(Alerts.WATCH_ID)
        }

        /** Called by the alarm. */
        internal fun onAlarm(context: Context) {
            if (!Store.settings(context).enabled) return
            running?.tick() ?: start(context)
        }

        fun canBeExact(context: Context): Boolean =
            context.getSystemService(AlarmManager::class.java)?.canScheduleExactAlarms() == true

        private fun alarmIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
            context, 0, Intent(context, TickReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        /**
         * The next check in [seconds], also while the phone sleeps. On time if the owner allowed exact alarms;
         * otherwise Android may move it later to save battery.
         */
        fun schedule(context: Context, seconds: Long) {
            val am = context.getSystemService(AlarmManager::class.java) ?: return
            val at = SystemClock.elapsedRealtime() + seconds * 1000
            try {
                if (am.canScheduleExactAlarms()) {
                    am.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, at, alarmIntent(context))
                } else {
                    am.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, at, alarmIntent(context))
                }
            } catch (_: SecurityException) {
                // Exact alarms taken back a moment ago
                am.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, at, alarmIntent(context))
            }
        }

        private fun cancel(context: Context) {
            context.getSystemService(AlarmManager::class.java)?.cancel(alarmIntent(context))
        }
    }
}

/** The alarm for the next check; only Surma's own PendingIntent reaches it. */
class TickReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        WatchService.onAlarm(context)
    }
}

/** After a restart or an update of Surma: watching goes on if it was on. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED && intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) return
        if (Store.settings(context).enabled) WatchService.start(context)
    }
}

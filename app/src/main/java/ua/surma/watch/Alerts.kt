package ua.surma.watch

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import ua.surma.Labels
import ua.surma.R
import ua.surma.alerts.Threat
import ua.surma.alerts.ThreatType
import ua.surma.ui.MainActivity
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** The notifications: loud ones for threats, and the quiet one that shows watching is on. */
object Alerts {
    private const val CHANNEL_ALERTS = "alerts"
    private const val CHANNEL_WATCH = "watch"
    const val WATCH_ID = 1

    fun createChannels(context: Context) {
        val nm = context.getSystemService(NotificationManager::class.java) ?: return
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_ALERTS, context.getString(R.string.channel_alerts), NotificationManager.IMPORTANCE_HIGH).apply {
                enableVibration(true)
                // The alarm sound, so it is heard over a quiet phone where Android allows it
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build(),
                )
                // On the lock screen only the kind of threat shows (see publicVersion below), never the region
                lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            },
        )
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_WATCH, context.getString(R.string.channel_watch), NotificationManager.IMPORTANCE_MIN).apply {
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            },
        )
    }

    fun canNotify(context: Context): Boolean =
        context.getSystemService(NotificationManager::class.java)?.areNotificationsEnabled() == true

    private fun openApp(context: Context): PendingIntent = PendingIntent.getActivity(
        context, 0,
        Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    /** One notification per post, with every threat of it that concerns the owner. */
    fun threat(context: Context, postId: Long, threats: List<Threat>) {
        if (threats.isEmpty() || !canNotify(context)) return
        val main = threats.firstOrNull { it.type != ThreatType.ALL_CLEAR } ?: threats.first()
        val title = Labels.title(context, main)
        val lines = threats.map { t ->
            val where = Labels.where(context, t)
            if (where.isEmpty()) Labels.title(context, t) else "${Labels.title(context, t)}: $where"
        }
        val source = threats.joinToString("\n") { it.sourceLine }.take(600)
        // What a locked screen shows: the kind of threat only
        val public = Notification.Builder(context, CHANNEL_ALERTS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notif_public, context.getString(Labels.type(main.type))))
            .setContentText(context.getString(R.string.notif_unlock))
            .build()
        val n = Notification.Builder(context, CHANNEL_ALERTS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(lines.first())
            .setStyle(Notification.BigTextStyle().bigText(lines.joinToString("\n") + "\n\n" + source))
            .setCategory(Notification.CATEGORY_ALARM)
            .setVisibility(Notification.VISIBILITY_PRIVATE)
            .setPublicVersion(public)
            .setContentIntent(openApp(context))
            .setAutoCancel(true)
            .setShowWhen(true)
            .build()
        try {
            context.getSystemService(NotificationManager::class.java)?.notify((postId % Int.MAX_VALUE).toInt() + 10, n)
        } catch (_: SecurityException) {
            // Notifications were turned off a moment ago: the home screen says so
        }
    }

    private val TIME = DateTimeFormatter.ofPattern("HH:mm")

    fun time(epochSeconds: Long): String = Instant.ofEpochSecond(epochSeconds).atZone(ZoneId.systemDefault()).format(TIME)

    /** The quiet notification Android requires while watching: when the last check was, or what is wrong. */
    fun watching(context: Context, state: WatchState): Notification {
        val text = when {
            state.problem == Problem.NETWORK -> context.getString(R.string.status_network, time(state.lastOk.takeIf { it > 0 } ?: state.lastCheck))
            state.problem == Problem.PAGE -> context.getString(R.string.status_page, time(state.lastOk.takeIf { it > 0 } ?: state.lastCheck))
            state.lastCheck > 0 -> context.getString(R.string.status_checked, time(state.lastCheck))
            else -> context.getString(R.string.status_starting)
        }
        return Notification.Builder(context, CHANNEL_WATCH)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notif_watching))
            .setContentText(text)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
            .setContentIntent(openApp(context))
            .build()
    }

    fun updateWatching(context: Context, state: WatchState) {
        try {
            context.getSystemService(NotificationManager::class.java)?.notify(WATCH_ID, watching(context, state))
        } catch (_: SecurityException) {
            // Notifications off: watching still works, the home screen shows the state
        }
    }
}

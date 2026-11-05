package ua.surma.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import ua.surma.ui.theme.Look
import ua.surma.ui.theme.LookStore
import ua.surma.ui.theme.SurmaTheme
import ua.surma.watch.Alerts
import ua.surma.watch.Feed
import ua.surma.watch.Store
import ua.surma.watch.WatchService
import ua.surma.watch.WatchSettings
import ua.surma.watch.WatchState
import ua.surma.watch.Watcher

class MainActivity : ComponentActivity() {
    private val look = mutableStateOf(Look())
    private val home = mutableStateOf(HomeUi(WatchSettings(), WatchState(), emptyList()))

    /** Android's own question about notifications, asked when watching is turned on. Watching works either way. */
    private val askNotify = registerForActivityResult(ActivityResultContracts.RequestPermission()) { refresh() }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppLanguage.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        look.value = LookStore.load(this)
        Alerts.createChannels(this)
        refresh()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Every finished check redraws the screen
                Watcher.changed.collect { refresh() }
            }
        }
        setContent {
            val theme = look.value.current(isSystemInDarkTheme())
            var page by rememberSaveable { mutableStateOf(Page.HOME) }
            BackHandler(enabled = page != Page.HOME) { page = if (page == Page.ABOUT) Page.SETTINGS else Page.HOME }
            LaunchedEffect(page) { if (page == Page.HOME) refresh() }
            SurmaTheme(theme) {
                when (page) {
                    Page.HOME -> HomeScreen(
                        home.value,
                        HomeActions(
                            onWatch = ::setWatching,
                            onSettings = { page = Page.SETTINGS },
                            onNotifySettings = { open(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, packageName)) },
                            onExact = { open(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName"))) },
                            onBattery = { open(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)) },
                        ),
                    )
                    Page.SETTINGS -> SettingsPage(
                        settings = home.value.settings,
                        onSettings = ::saveSettings,
                        look = look.value,
                        onLook = { look.value = it; LookStore.save(this, it) },
                        language = AppLanguage.current(this),
                        onLanguage = { AppLanguage.set(this, it) },
                        onAbout = { page = Page.ABOUT },
                        onBack = { page = Page.HOME },
                    )
                    Page.ABOUT -> AboutPage(onBack = { page = Page.SETTINGS })
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Back from Android's settings: the hints may be gone now. Opening Surma also checks at once.
        refresh()
        if (home.value.settings.enabled) WatchService.start(this)
    }

    private fun setWatching(on: Boolean) {
        saveSettings(home.value.settings.copy(enabled = on))
        if (on) {
            WatchService.start(this)
            if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                askNotify.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            WatchService.stop(this)
        }
    }

    private fun saveSettings(s: WatchSettings) {
        Store.save(this, s)
        home.value = home.value.copy(settings = s)
    }

    private fun refresh() {
        val power = getSystemService(PowerManager::class.java)
        home.value = HomeUi(
            settings = Store.settings(this),
            state = Store.state(this),
            items = Feed.load(this),
            canNotify = Alerts.canNotify(this),
            exactAllowed = WatchService.canBeExact(this),
            batteryFree = power?.isIgnoringBatteryOptimizations(packageName) == true,
        )
    }

    private fun open(intent: Intent) {
        try {
            startActivity(intent)
        } catch (_: Exception) {
            // Some phones lack that settings page: the app's own page in Settings is the next best
            runCatching { startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))) }
        }
    }

    private enum class Page { HOME, SETTINGS, ABOUT }
}

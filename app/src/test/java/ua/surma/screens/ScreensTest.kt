package ua.surma.screens

import android.app.Application
import android.content.ComponentName
import android.graphics.Bitmap
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import ua.surma.TestPlaces
import ua.surma.alerts.ChannelPost
import ua.surma.alerts.Oblast
import ua.surma.alerts.PostParser
import ua.surma.alerts.Rules
import ua.surma.help.Help
import ua.surma.ui.AboutPage
import ua.surma.ui.CardPage
import ua.surma.ui.HelpScreen
import ua.surma.ui.MainActivity
import ua.surma.ui.Tabs
import ua.surma.ui.AppLanguage
import ua.surma.ui.HomeActions
import ua.surma.ui.HomeScreen
import ua.surma.ui.HomeUi
import ua.surma.ui.SettingsPage
import ua.surma.ui.theme.Look
import ua.surma.ui.theme.SurmaTheme
import ua.surma.ui.theme.Theme
import ua.surma.watch.FeedItem
import ua.surma.watch.Problem
import ua.surma.watch.WatchSettings
import ua.surma.watch.WatchState
import java.io.File

/**
 * Draws Surma's screens with made-up states, in several themes and sizes, on the computer.
 * A screen that crashes fails the build; the pictures go to build/screens for review.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "uk-w400dp-h1600dp-xhdpi")
class ScreensTest {
    @get:Rule val compose = createEmptyComposeRule()

    private val now = System.currentTimeMillis() / 1000

    /** A believable night: posts in the channel's own words, read by the real parser. */
    private val feed: List<FeedItem> by lazy {
        listOf(
            "🛵 Ударні БпЛА на Цвіткове (Черкащина) з північного да східного напрямків." to 4L,
            "🏍 Реактивний БпЛА на Кропивницький зі сходу." to 9L,
            "Група ударних БпЛА на Сумщині, курсом на Конотоп." to 14L,
            "3х ударних БпЛА на Харківщині курсом на Кегичівку." to 22L,
            "⚠️ Загроза застосування балістичного озброєння з північно-східного напрямку!" to 31L,
            "Відбій загрози застосування балістики." to 40L,
        ).mapIndexed { i, (text, minutesAgo) ->
            val post = ChannelPost(1000L - i, now - minutesAgo * 60, text)
            PostParser.parse(post.text, TestPlaces.index).map { FeedItem(post.id, post.epochSeconds!!, it) }
        }.flatten()
    }

    private val on = WatchSettings(enabled = true, oblasts = setOf(Oblast.SUMY, Oblast.KYIV_CITY))
    private val checked = WatchState(lastId = 1000, lastCheck = now - 20, lastOk = now - 20, lastThreat = now - 240)

    @Before fun setUp() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        // The empty activity that hosts each screen (only known to this test, never to the app)
        shadowOf(app.packageManager).addActivityIfNotPresent(ComponentName(app, ComponentActivity::class.java))
    }

    private fun shot(name: String, theme: Theme, content: @Composable () -> Unit) =
        ActivityScenario.launch(ComponentActivity::class.java).use { scenario ->
            scenario.onActivity { it.setContent { SurmaTheme(theme) { content() } } }
            repeat(6) { shadowOf(Looper.getMainLooper()).idle(); compose.waitForIdle() }
            var bitmap: Bitmap? = null
            scenario.onActivity { activity ->
                val view = activity.window.decorView
                bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888).also { view.draw(android.graphics.Canvas(it)) }
            }
            val dir = File("build/screens").apply { mkdirs() }
            File(dir, "$name.png").outputStream().use { bitmap!!.compress(Bitmap.CompressFormat.PNG, 100, it) }
            Unit
        }

    private fun home(name: String, theme: Theme, ui: HomeUi) = shot(name, theme) { HomeScreen(ui, HomeActions()) }

    private fun settings(name: String, theme: Theme, s: WatchSettings = on) = shot(name, theme) {
        SettingsPage(s, {}, Look(), {}, AppLanguage.Choice.AUTO, {}, {}, {})
    }

    @Test fun homeWatching() = home("01-home-watching", Theme.SYSTEM, HomeUi(on, checked, feed))
    @Config(qualifiers = "+night")
    @Test fun homeWatchingDark() = home("02-home-watching-dark", Theme.ZORIANA_NICH, HomeUi(on, checked, feed))
    @Test fun homeOff() = home("03-home-off", Theme.NEBO, HomeUi(WatchSettings(), WatchState(), emptyList()))
    @Test fun homeQuiet() = home("04-home-quiet", Theme.KREYDA, HomeUi(on.copy(oblasts = emptySet()), checked, emptyList()))
    @Test fun homeHints() = home("05-home-hints", Theme.SYSTEM,
        HomeUi(on, checked, feed.take(2), canNotify = false, exactAllowed = false, batteryFree = false))
    @Test fun homeNoNetwork() = home("06-home-no-network", Theme.GLYNA,
        HomeUi(on, checked.copy(lastCheck = now, failures = 3, problem = Problem.NETWORK), feed))
    @Config(qualifiers = "+night")
    @Test fun homePodillia() = home("07-home-podillia", Theme.PODILLIA, HomeUi(on, checked, feed))

    @Config(qualifiers = "uk-w400dp-h3000dp-xhdpi")
    @Test fun settingsPage() = settings("08-settings", Theme.SYSTEM)
    @Config(qualifiers = "uk-w400dp-h3000dp-xhdpi")
    @Test fun settingsSaver() = settings("09-settings-saver", Theme.KALYNA, on.copy(pace = Rules.Pace.SAVER, nationwide = false))

    @Config(qualifiers = "uk-w400dp-h2600dp-xhdpi")
    @Test fun about() = shot("10-about", Theme.SYSTEM) { AboutPage(onBack = {}) }
    @Config(qualifiers = "en-w400dp-h2600dp-xhdpi")
    @Test fun aboutEnglish() = shot("11-about-english", Theme.PODILLIA) { AboutPage(onBack = {}) }

    private fun help(lang: String) = Help.parse(File("src/main/assets/help/$lang.json").readText())
    private val tabs: @Composable () -> Unit = { Tabs(MainActivity.Page.HELP) {} }

    @Test fun helpList() = shot("17-help", Theme.SYSTEM) { HelpScreen(help("uk"), {}, {}, tabs) }
    @Config(qualifiers = "uk-w400dp-h3400dp-xhdpi")
    @Test fun cardBleeding() = shot("18-card-bleeding", Theme.SYSTEM) { help("uk").let { h -> CardPage(h.card("bleeding")!!, h, {}, {}) } }
    @Config(qualifiers = "uk-w400dp-h2600dp-xhdpi")
    @Test fun cardBallistic() = shot("19-card-ballistic", Theme.NEBO) { help("uk").let { h -> CardPage(h.card("ballistic")!!, h, {}, {}) } }
    @Config(qualifiers = "en-w400dp-h3400dp-night-xhdpi")
    @Test fun cardUnconsciousDark() = shot("20-card-unconscious-dark", Theme.ZORIANA_NICH) { help("en").let { h -> CardPage(h.card("unconscious")!!, h, {}, {}) } }
    @Config(qualifiers = "uk-w340dp-h3400dp-xhdpi", fontScale = 1.3f)
    @Test fun cardNarrowLargeFont() = shot("21-card-air-alert-narrow-large-font", Theme.SYSTEM) { help("uk").let { h -> CardPage(h.card("air-alert")!!, h, {}, {}) } }
    @Test fun homeWithTabs() = shot("22-home-with-tabs", Theme.SYSTEM) { HomeScreen(HomeUi(on, checked, feed), HomeActions()) { Tabs(MainActivity.Page.HOME) {} } }

    @Config(qualifiers = "en-w400dp-h1600dp-xhdpi")
    @Test fun homeEnglish() = home("12-home-english", Theme.NEBO, HomeUi(on, checked, feed))

    // A narrow phone with a larger font, an unfolded phone and a tablet: nothing may wrap badly or hide
    @Config(qualifiers = "uk-w340dp-h1800dp-xhdpi", fontScale = 1.3f)
    @Test fun homeNarrowLargeFont() = home("13-home-narrow-large-font", Theme.SYSTEM, HomeUi(on, checked, feed))
    @Config(qualifiers = "uk-w340dp-h3400dp-xhdpi", fontScale = 1.3f)
    @Test fun settingsNarrowLargeFont() = settings("14-settings-narrow-large-font", Theme.SYSTEM)
    @Config(qualifiers = "uk-w673dp-h841dp-xhdpi")
    @Test fun foldable() = home("15-foldable-open", Theme.SYSTEM, HomeUi(on, checked, feed))
    @Config(qualifiers = "uk-w1280dp-h800dp-mdpi")
    @Test fun tablet() = home("16-tablet-landscape", Theme.GLYNA, HomeUi(on, checked, feed))
}

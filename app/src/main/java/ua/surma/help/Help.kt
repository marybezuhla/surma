package ua.surma.help

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import ua.surma.alerts.ThreatType

/** One step of a card: what to do now, and why (shown quieter). figure: a guide section whose picture shows it. */
data class Step(val action: String, val why: String, val figure: String, val icon: String, val wait: Int)

/** A "what to do" card, word for word from Sprotyv; reviewedBy is empty until a specialist has reviewed it. */
data class Card(
    val id: String,
    val title: String,
    val steps: List<Step>,
    val source: String,
    val checkedOn: String,
    val reviewedBy: String,
    /** The number to call from this card ("103"), or empty. */
    val call: String,
)

data class Figure(val pictures: List<String>, val caption: String)

data class Help(val cards: List<Card>, val figures: Map<String, Figure>) {
    fun card(id: String): Card? = cards.firstOrNull { it.id == id }

    companion object {
        val EMPTY = Help(emptyList(), emptyMap())

        /** Only the numbers these cards may offer to call; anything else is never dialled. */
        val SERVICES = setOf("101", "102", "103", "104", "112")
        private val ID = Regex("^[a-z0-9-]{1,40}$")
        private val PICTURE = Regex("^fig_[0-9a-f]{16}$")

        /** Which card a kind of threat should open: the shelter card for most, the ballistic one for ballistic missiles. */
        fun cardFor(type: ThreatType): String? = when (type) {
            ThreatType.BALLISTIC -> "ballistic"
            ThreatType.ALL_CLEAR -> null
            else -> "air-alert"
        }

        fun parse(json: String): Help {
            val o = JSONObject(json)
            val cards = o.getJSONArray("cards").let { a -> (0 until minOf(a.length(), 50)).map { card(a.getJSONObject(it)) } }
            val figs = o.optJSONObject("figures") ?: JSONObject()
            val figures = figs.keys().asSequence().take(50).associateWith { k ->
                val f = figs.getJSONObject(k)
                val pics = f.getJSONArray("pictures").let { a -> (0 until a.length()).map { a.getString(it) } }
                require(pics.all { PICTURE.matches(it) }) { "Bad picture name" }
                Figure(pics, f.optString("caption"))
            }
            return Help(cards, figures)
        }

        private fun card(o: JSONObject): Card {
            val steps = o.getJSONArray("steps").let { a ->
                (0 until minOf(a.length(), 12)).map {
                    val s = a.getJSONObject(it)
                    Step(s.getString("action"), s.optString("why"), s.optString("figure"), s.optString("icon"), s.optInt("wait").coerceIn(0, 240))
                }
            }
            val id = o.getString("id")
            require(ID.matches(id) && steps.isNotEmpty()) { "Bad card" }
            return Card(
                id = id,
                title = o.getString("title"),
                steps = steps,
                source = o.getString("source"),
                checkedOn = o.getString("checkedOn"),
                reviewedBy = o.optString("reviewedBy"),
                call = o.optString("call").takeIf { it in SERVICES } ?: "",
            )
        }

        @Volatile private var cached: Pair<String, Help>? = null

        /** The cards in the shown language (Ukrainian, or English for any other); read once. */
        fun get(context: Context): Help {
            val lang = if (context.resources.configuration.locales[0].language == "uk") "uk" else "en"
            cached?.takeIf { it.first == lang }?.let { return it.second }
            val help = try {
                context.assets.open("help/$lang.json").use { parse(it.readBytes().toString(Charsets.UTF_8)) }
            } catch (_: Exception) {
                // A damaged file only hides the cards; alerts are not touched
                EMPTY
            }
            cached = lang to help
            return help
        }
    }
}

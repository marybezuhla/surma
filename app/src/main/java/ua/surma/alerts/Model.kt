package ua.surma.alerts

/** A kind of threat as the Air Force channel names it. The order is the order of recognition. */
enum class ThreatType(val key: String) {
    ALL_CLEAR("all_clear"),
    BALLISTIC("ballistic"),
    HIGH_SPEED("high_speed"),
    MISSILE("missile"),
    JET_DRONE("jet_drone"),
    GUIDED_BOMB("guided_bomb"),
    DRONE("drone"),
    AVIATION("aviation");

    /** Threats that often come without a region and concern the whole country. */
    val nationwide: Boolean get() = this == BALLISTIC || this == HIGH_SPEED || this == MISSILE || this == ALL_CLEAR

    companion object {
        fun of(key: String?): ThreatType? = entries.firstOrNull { it.key == key }
    }
}

/** Where a threat comes FROM. bearing is in degrees, for an arrow on the map later. */
enum class Direction(val key: String, val bearing: Int) {
    N("n", 0), NE("ne", 45), E("e", 90), SE("se", 135), S("s", 180), SW("sw", 225), W("w", 270), NW("nw", 315);

    companion object {
        fun of(key: String?): Direction? = entries.firstOrNull { it.key == key }
    }
}

/** A city or village known to Surma. approximate: the point still has to be checked against the official register. */
data class Place(val name: String, val oblast: Oblast, val lat: Double, val lon: Double, val approximate: Boolean = false)

data class Threat(
    val type: ThreatType,
    /** "3х ударних БпЛА": only when the post gives a number. */
    val count: Int? = null,
    /** "Група БпЛА" without a number. */
    val isGroup: Boolean = false,
    val oblasts: List<Oblast> = emptyList(),
    /** The place it heads to, when Surma knows it. */
    val target: Place? = null,
    /** The place as written, when Surma does not know it: shown as text, never as a point on a map. */
    val unresolvedTarget: String? = null,
    val from: List<Direction> = emptyList(),
    /** The line of the post it was read from; always shown, since reading can be wrong. */
    val sourceLine: String,
)

/** One post of the channel, as read from its public page. */
data class ChannelPost(val id: Long, val epochSeconds: Long?, val text: String)

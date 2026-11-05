package ua.surma

import android.content.Context
import androidx.annotation.StringRes
import ua.surma.alerts.Direction
import ua.surma.alerts.Oblast
import ua.surma.alerts.Rules
import ua.surma.alerts.Threat
import ua.surma.alerts.ThreatType

/** The words people see for each kind of threat, region and direction, from the app's resources. */
object Labels {
    @StringRes fun type(t: ThreatType): Int = when (t) {
        ThreatType.ALL_CLEAR -> R.string.type_all_clear
        ThreatType.BALLISTIC -> R.string.type_ballistic
        ThreatType.HIGH_SPEED -> R.string.type_high_speed
        ThreatType.MISSILE -> R.string.type_missile
        ThreatType.JET_DRONE -> R.string.type_jet_drone
        ThreatType.GUIDED_BOMB -> R.string.type_guided_bomb
        ThreatType.DRONE -> R.string.type_drone
        ThreatType.AVIATION -> R.string.type_aviation
    }

    @StringRes fun oblast(o: Oblast): Int = when (o) {
        Oblast.VINNYTSIA -> R.string.oblast_vinnytsia
        Oblast.VOLYN -> R.string.oblast_volyn
        Oblast.DNIPRO -> R.string.oblast_dnipro
        Oblast.DONETSK -> R.string.oblast_donetsk
        Oblast.ZHYTOMYR -> R.string.oblast_zhytomyr
        Oblast.ZAKARPATTIA -> R.string.oblast_zakarpattia
        Oblast.ZAPORIZHZHIA -> R.string.oblast_zaporizhzhia
        Oblast.IVANO_FRANKIVSK -> R.string.oblast_ivano_frankivsk
        Oblast.KYIV_OBLAST -> R.string.oblast_kyiv_oblast
        Oblast.KYIV_CITY -> R.string.oblast_kyiv_city
        Oblast.KIROVOHRAD -> R.string.oblast_kirovohrad
        Oblast.LUHANSK -> R.string.oblast_luhansk
        Oblast.LVIV -> R.string.oblast_lviv
        Oblast.MYKOLAIV -> R.string.oblast_mykolaiv
        Oblast.ODESA -> R.string.oblast_odesa
        Oblast.POLTAVA -> R.string.oblast_poltava
        Oblast.RIVNE -> R.string.oblast_rivne
        Oblast.SUMY -> R.string.oblast_sumy
        Oblast.TERNOPIL -> R.string.oblast_ternopil
        Oblast.KHARKIV -> R.string.oblast_kharkiv
        Oblast.KHERSON -> R.string.oblast_kherson
        Oblast.KHMELNYTSKYI -> R.string.oblast_khmelnytskyi
        Oblast.CHERKASY -> R.string.oblast_cherkasy
        Oblast.CHERNIVTSI -> R.string.oblast_chernivtsi
        Oblast.CHERNIHIV -> R.string.oblast_chernihiv
        Oblast.CRIMEA -> R.string.oblast_crimea
    }

    @StringRes fun from(d: Direction): Int = when (d) {
        Direction.N -> R.string.from_n
        Direction.NE -> R.string.from_ne
        Direction.E -> R.string.from_e
        Direction.SE -> R.string.from_se
        Direction.S -> R.string.from_s
        Direction.SW -> R.string.from_sw
        Direction.W -> R.string.from_w
        Direction.NW -> R.string.from_nw
    }

    @StringRes fun pace(p: Rules.Pace): Int = when (p) {
        Rules.Pace.FAST -> R.string.pace_fast
        Rules.Pace.NORMAL -> R.string.pace_normal
        Rules.Pace.SAVER -> R.string.pace_saver
    }

    /** Regions sorted by their name in the shown language, the way its alphabet goes ("Івано-Франківська" after "Закарпатська"). */
    fun sortedOblasts(context: Context): List<Oblast> {
        val collator = java.text.Collator.getInstance(context.resources.configuration.locales[0])
        return Oblast.entries.sortedWith(compareBy(collator) { context.getString(oblast(it)) })
    }

    /** "Ударні БпЛА ×3", "Ударні БпЛА (група)". */
    fun title(context: Context, t: Threat): String {
        val name = context.getString(type(t.type))
        return when {
            t.count != null -> "$name ×${t.count}"
            t.isGroup -> "$name (${context.getString(R.string.count_group)})"
            else -> name
        }
    }

    /** "напрямок: Конотоп · Сумська область · з півночі": what is known about where, in one line (the name as the register writes it). */
    fun where(context: Context, t: Threat): String = buildList {
        val to = t.target?.label ?: t.unresolvedTarget
        to?.let { add(context.getString(R.string.to_target, it)) }
        t.oblasts.forEach { add(context.getString(oblast(it))) }
        t.from.forEach { add(context.getString(from(it))) }
    }.distinct().joinToString(" · ")
}

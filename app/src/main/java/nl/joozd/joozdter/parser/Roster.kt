package nl.joozd.joozdter.parser

import nl.joozd.joozdter.data.Day
import nl.joozd.joozdter.utils.extensions.atEndOfDay
import java.time.ZoneOffset

class Roster(
    val days: List<Day>
) {
    val period: ClosedRange<Long> by lazy {
        val start = days.minOfOrNull { it.date }?.atStartOfDay(ZoneOffset.UTC)?.toEpochSecond() ?: 0L
        val end = days.maxOfOrNull { it.date }?.atEndOfDay(ZoneOffset.UTC)?.toEpochSecond() ?: 0L
        start..end
    }
}
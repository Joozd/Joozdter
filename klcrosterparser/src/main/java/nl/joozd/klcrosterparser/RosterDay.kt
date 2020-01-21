package nl.joozd.klcrosterparser

import org.threeten.bp.*

data class RosterDay(val date: LocalDate, val events: List<KlcRosterEvent>, val extraInfo: String) {
    val startOfDay: Instant by lazy{
        LocalDateTime.of(date, LocalTime.MIDNIGHT).atZone(ZoneOffset.UTC).toInstant()
    }
    val endOfDay: Instant by lazy {
        LocalDateTime.of(date.plusDays(1), LocalTime.MIDNIGHT).atZone(ZoneOffset.UTC).toInstant()
    }
}
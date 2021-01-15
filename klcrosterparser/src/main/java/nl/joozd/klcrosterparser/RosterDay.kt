package nl.joozd.klcrosterparser

import java.time.*

data class RosterDay(val date: LocalDate, val events: List<KlcRosterEvent>, val extraInfo: String) {
    val startOfDay: Instant by lazy{
        LocalDateTime.of(date, LocalTime.MIDNIGHT).atZone(ZoneOffset.UTC).toInstant()
    }
    val endOfDay: Instant by lazy {
        LocalDateTime.of(date.plusDays(1), LocalTime.MIDNIGHT).atZone(ZoneOffset.UTC).toInstant()
    }

    /**
     * This day's checkinEvent, if any
     * NOTE: You can only check in once per day.
     */
    val checkinEvent: KlcRosterEvent? = events.firstOrNull{it.type == Activities.CHECKIN}

    override fun toString() = "Date: $date, events: $events, extraInfo: $extraInfo"
}
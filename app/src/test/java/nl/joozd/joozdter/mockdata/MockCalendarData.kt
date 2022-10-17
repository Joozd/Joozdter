package nl.joozd.joozdter.mockdata

import nl.joozd.joozdter.data.EventTypes
import nl.joozd.joozdter.data.events.Event
import nl.joozd.joozdter.data.events.actualEvents.LeaveEvent
import nl.joozd.joozdter.mockcalendar.MockCalendarEvent
import nl.joozd.joozdter.utils.extensions.endInstant
import java.time.Instant
import java.time.LocalDate

object MockCalendarData {
    private val now = Instant.now()
    private val minus1Hour = now.minusSeconds(60*60L)
    private val plusOneHour = now.plusSeconds(60*60L)

    private val tonightAtMidnight = LocalDate.now().endInstant()

    val data1 = MockCalendarEvent.of(
        Event("event 1 on 2", EventTypes.UNKNOWN_EVENT, now, plusOneHour, "info 1", "notes 1", 1), 2)
    val data2 = MockCalendarEvent.of(
            Event("event 2 on 2", EventTypes.UNKNOWN_EVENT, minus1Hour, now, "info 2", "notes 2", 2), 2)
    val allDayEvent = MockCalendarEvent.of(
        LeaveEvent(Event("event 3 on 2 ( all day)", EventTypes.LEAVE, minus1Hour, now, "info 3", "notes 3", 3)), 2)
    val deletedEvent = MockCalendarEvent.of(
        Event("event 4 on 2 (deleted)", EventTypes.UNKNOWN_EVENT, minus1Hour, now, "info 4", "notes 4", 4), 2)
    val eventOnAnotherCalendar = MockCalendarEvent.of(
        Event("event 5 on 10", EventTypes.UNKNOWN_EVENT, minus1Hour, now, "info 4", "notes 4", 5), 10)

    val allEvents = listOf(data1, data2, allDayEvent, deletedEvent, eventOnAnotherCalendar)



}




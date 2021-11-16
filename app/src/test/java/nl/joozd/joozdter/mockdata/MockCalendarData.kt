package nl.joozd.joozdter.mockdata

import nl.joozd.joozdter.mockcalendar.MockCalendarEvent
import nl.joozd.joozdter.utils.extensions.endInstant
import java.time.Duration
import java.time.Instant
import java.time.LocalDate

object MockCalendarData {
    private val now = Instant.now()
    private val minus1Hour = now.minusSeconds(60*60L)
    private val plusOneHour = now.plusSeconds(60*60L)

    private val tonightAtMidnight = LocalDate.now().endInstant()
    private val tomorrowAtMidnight = tonightAtMidnight.plus(Duration.ofDays(1))

    val data1 = MockCalendarEvent(1,2, "event 1 on 2", "loc1", "desc1", now.toEpochMilli(), plusOneHour.toEpochMilli(), 0, 0)
    val data2 = MockCalendarEvent(2,2, "event 2 on 2", "loc2", "desc2", minus1Hour.toEpochMilli(), now.toEpochMilli(), 0, 0)
    val allDayEvent = MockCalendarEvent (3,2, "event 3 on 2 (all day)", "loc3", "desc3", tonightAtMidnight.toEpochMilli(), tomorrowAtMidnight.toEpochMilli(), 1, 0)
    val deletedEvent = MockCalendarEvent(4,2, "event 4 on 2", "loc4", "desc4", minus1Hour.toEpochMilli(), now.toEpochMilli(), 0, 1)
    val eventOnAnotherCalendar = MockCalendarEvent(5,10, "event 5 on 10", "loc5", "desc5", minus1Hour.toEpochMilli(), now.toEpochMilli(), 0, 1)

    val allEvents = listOf(data1, data2, allDayEvent, deletedEvent, eventOnAnotherCalendar)



}




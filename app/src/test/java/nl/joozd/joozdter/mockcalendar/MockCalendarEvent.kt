package nl.joozd.joozdter.mockcalendar

import android.provider.CalendarContract
import nl.joozd.joozdter.data.events.AllDayevent
import nl.joozd.joozdter.data.events.Event

class MockCalendarEvent: MockCalendarEntity(){
    override val columnNames: Set<String> =
        ColumnNameSets.baseColumns +
        ColumnNameSets.syncColumns +
        ColumnNameSets.eventsColumns

    companion object{
        fun of(event: Event, calendarID: Long, deleted: Int = 0): MockCalendarEvent = MockCalendarEvent().apply{
            this[CalendarContract.Events._ID] = event.id.toString()
            this[CalendarContract.Events.CALENDAR_ID] = calendarID.toString()
            this[CalendarContract.Events.TITLE] = event.name
            this[CalendarContract.Events.EVENT_LOCATION] = event.info
            this[CalendarContract.Events.DESCRIPTION] = event.notes
            this[CalendarContract.Events.DTSTART] = event.startEpochMilli.toString()
            this[CalendarContract.Events.DTEND] = event.endEpochMilli.toString()
            this[CalendarContract.Events.ALL_DAY] = if (event is AllDayevent) "1" else "0"
            this[CalendarContract.Events.DELETED] = deleted.toString()
        }
    }
}

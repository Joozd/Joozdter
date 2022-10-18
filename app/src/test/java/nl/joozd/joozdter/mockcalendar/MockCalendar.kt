package nl.joozd.joozdter.mockcalendar

import android.provider.CalendarContract
import nl.joozd.joozdter.calendar.CalendarDescriptorOld

class MockCalendar: MockCalendarEntity(){
    override val columnNames: Set<String> =
        ColumnNameSets.baseColumns +
        ColumnNameSets.syncColumns +
        ColumnNameSets.calendarColumns

    companion object{
        fun of(calendar: CalendarDescriptorOld): MockCalendar = MockCalendar().apply{
            this[CalendarContract.Calendars._ID] = calendar.calID.toString()
            this[CalendarContract.Calendars.CALENDAR_DISPLAY_NAME] = calendar.displayName
            this[CalendarContract.Calendars.ACCOUNT_NAME] = calendar.accountName
            this[CalendarContract.Calendars.OWNER_ACCOUNT] = calendar.ownerName
            this[CalendarContract.Calendars.NAME] = calendar.name
            this[CalendarContract.Calendars.CALENDAR_COLOR] = calendar.color.toString()
        }
    }
}
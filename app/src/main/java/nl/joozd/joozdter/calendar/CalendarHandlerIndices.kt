package nl.joozd.joozdter.calendar

import android.provider.CalendarContract

object CalendarHandlerIndices {
    val CALENDAR_PROJECTION: Array<String> = arrayOf(
        CalendarContract.Calendars._ID,                     // 0
        CalendarContract.Calendars.ACCOUNT_NAME,            // 1
        CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,   // 2
        CalendarContract.Calendars.OWNER_ACCOUNT,           // 3
        CalendarContract.Calendars.NAME,                    // 4
        CalendarContract.Calendars.CALENDAR_COLOR           // 5
    )

    val EVENT_PROJECTION: Array<String> = arrayOf(
        CalendarContract.Events._ID,                        // 0
        CalendarContract.Events.CALENDAR_ID,                // 1
        CalendarContract.Events.TITLE,                      // 2
        CalendarContract.Events.EVENT_LOCATION,             // 3
        CalendarContract.Events.DESCRIPTION,                // 4
        CalendarContract.Events.DTSTART,                    // 5
        CalendarContract.Events.DTEND,                      // 6
        CalendarContract.Events.ALL_DAY,                    // 7
        CalendarContract.Events.DELETED                     // 8
    )

    val PROJECTION_ID_INDEX: Int = CALENDAR_PROJECTION.indexOf(CalendarContract.Calendars._ID)
    val PROJECTION_ACCOUNT_NAME_INDEX: Int = CALENDAR_PROJECTION.indexOf(CalendarContract.Calendars.ACCOUNT_NAME)
    val PROJECTION_DISPLAY_NAME_INDEX: Int = CALENDAR_PROJECTION.indexOf(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)
    val PROJECTION_OWNER_ACCOUNT_INDEX: Int = CALENDAR_PROJECTION.indexOf(CalendarContract.Calendars.OWNER_ACCOUNT)
    val PROJECTION_NAME_INDEX: Int = CALENDAR_PROJECTION.indexOf(CalendarContract.Calendars.NAME)
    val PROJECTION_CALENDAR_COLOR: Int = CALENDAR_PROJECTION.indexOf(CalendarContract.Calendars.CALENDAR_COLOR)

    val EVENT_ID_INDEX: Int = EVENT_PROJECTION.indexOf(CalendarContract.Events._ID)
    val EVENT_CALENDAR_ID_INDEX: Int = EVENT_PROJECTION.indexOf(CalendarContract.Events.CALENDAR_ID)
    val EVENT_TITLE_INDEX: Int = EVENT_PROJECTION.indexOf(CalendarContract.Events.TITLE)
    val EVENT_EVENT_LOCATION_INDEX: Int = EVENT_PROJECTION.indexOf(CalendarContract.Events.EVENT_LOCATION)
    val EVENT_DESCRIPTION_INDEX: Int = EVENT_PROJECTION.indexOf(CalendarContract.Events.DESCRIPTION)
    val EVENT_DTSTART_INDEX: Int = EVENT_PROJECTION.indexOf(CalendarContract.Events.DTSTART)
    val EVENT_DTEND_INDEX: Int = EVENT_PROJECTION.indexOf(CalendarContract.Events.DTEND)
    val EVENT_ALL_DAY_INDEX: Int = EVENT_PROJECTION.indexOf(CalendarContract.Events.ALL_DAY)
    val EVENT_DELETED_INDEX: Int = EVENT_PROJECTION.indexOf(CalendarContract.Events.DELETED)
}
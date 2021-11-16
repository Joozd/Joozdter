package nl.joozd.joozdter.mockcalendar

import android.provider.CalendarContract

object ColumnNameSets {
    // BaseColumns, SyncColumns, CalendarColumns
    val baseColumns = setOf(
        CalendarContract.Calendars._ID,
        CalendarContract.Calendars._COUNT
    )

    val syncColumns = setOf (
        CalendarContract.Calendars.CAL_SYNC1,
        CalendarContract.Calendars.CAL_SYNC2,
        CalendarContract.Calendars.CAL_SYNC3,
        CalendarContract.Calendars.CAL_SYNC4,
        CalendarContract.Calendars.CAL_SYNC5,
        CalendarContract.Calendars.CAL_SYNC6,
        CalendarContract.Calendars.CAL_SYNC7,
        CalendarContract.Calendars.CAL_SYNC8,
        CalendarContract.Calendars.CAL_SYNC9,
        CalendarContract.Calendars.CAL_SYNC10,
        CalendarContract.Calendars.ACCOUNT_NAME,
        CalendarContract.Calendars.ACCOUNT_TYPE,
        CalendarContract.Calendars._SYNC_ID,
        CalendarContract.Calendars.DIRTY,
        CalendarContract.Calendars.MUTATORS,
        CalendarContract.Calendars.DELETED,
        CalendarContract.Calendars.CAN_PARTIALLY_UPDATE
    )

    val calendarColumns = setOf(
        CalendarContract.Calendars.CALENDAR_COLOR,
        CalendarContract.Calendars.CALENDAR_COLOR_KEY, // when updated, should update CALENDAR_COLOR
        CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
        CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL,
        CalendarContract.Calendars.VISIBLE,
        CalendarContract.Calendars.CALENDAR_TIME_ZONE,
        CalendarContract.Calendars.SYNC_EVENTS,
        CalendarContract.Calendars.OWNER_ACCOUNT,
        CalendarContract.Calendars.CAN_ORGANIZER_RESPOND,
        CalendarContract.Calendars.CAN_MODIFY_TIME_ZONE,
        CalendarContract.Calendars.MAX_REMINDERS,
        CalendarContract.Calendars.ALLOWED_REMINDERS,
        CalendarContract.Calendars.ALLOWED_AVAILABILITY,
        CalendarContract.Calendars.ALLOWED_ATTENDEE_TYPES,
        CalendarContract.Calendars.IS_PRIMARY
    )

    val eventsColumns = setOf(
        CalendarContract.Events.CALENDAR_ID,
        CalendarContract.Events.TITLE,
        CalendarContract.Events.DESCRIPTION,
        CalendarContract.Events.EVENT_LOCATION,
        CalendarContract.Events.EVENT_COLOR,
        CalendarContract.Events.EVENT_COLOR_KEY, // should change EVENT_COLOR when changed
        CalendarContract.Events.DISPLAY_COLOR,
        CalendarContract.Events.STATUS,
        CalendarContract.Events.SELF_ATTENDEE_STATUS,
        CalendarContract.Events.SYNC_DATA1,
        CalendarContract.Events.SYNC_DATA2,
        CalendarContract.Events.SYNC_DATA3,
        CalendarContract.Events.SYNC_DATA4,
        CalendarContract.Events.SYNC_DATA5,
        CalendarContract.Events.SYNC_DATA6,
        CalendarContract.Events.SYNC_DATA7,
        CalendarContract.Events.SYNC_DATA8,
        CalendarContract.Events.SYNC_DATA9,
        CalendarContract.Events.SYNC_DATA10,
        CalendarContract.Events.LAST_SYNCED,
        CalendarContract.Events.DTSTART,
        CalendarContract.Events.DTEND,
        CalendarContract.Events.DURATION,
        CalendarContract.Events.EVENT_TIMEZONE,
        CalendarContract.Events.EVENT_END_TIMEZONE,
        CalendarContract.Events.ALL_DAY,
        CalendarContract.Events.ACCESS_LEVEL,
        CalendarContract.Events.AVAILABILITY,
        CalendarContract.Events.HAS_ALARM,
        CalendarContract.Events.HAS_EXTENDED_PROPERTIES,
        CalendarContract.Events.RRULE,
        CalendarContract.Events.RDATE,
        CalendarContract.Events.EXRULE,
        CalendarContract.Events.EXDATE,
        CalendarContract.Events.ORIGINAL_ID,
        CalendarContract.Events.ORIGINAL_SYNC_ID,
        CalendarContract.Events.ORIGINAL_INSTANCE_TIME,
        CalendarContract.Events.ORIGINAL_ALL_DAY,
        CalendarContract.Events.LAST_DATE,
        CalendarContract.Events.HAS_ATTENDEE_DATA,
        CalendarContract.Events.GUESTS_CAN_MODIFY,
        CalendarContract.Events.GUESTS_CAN_INVITE_OTHERS,
        CalendarContract.Events.GUESTS_CAN_SEE_GUESTS,
        CalendarContract.Events.ORGANIZER,
        CalendarContract.Events.IS_ORGANIZER,
        CalendarContract.Events.CAN_INVITE_OTHERS,
        CalendarContract.Events.CUSTOM_APP_PACKAGE,
        CalendarContract.Events.CUSTOM_APP_URI,
        CalendarContract.Events.UID_2445
        )
}
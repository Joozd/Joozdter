package nl.joozd.joozdter.data.utils

import nl.joozd.joozdcalendarapi.CalendarDescriptor
import nl.joozd.joozdter.data.sharedPrefs.JoozdterPrefs

suspend fun getPickedCalendarFromList(calendars: List<CalendarDescriptor>) =
    calendars.firstOrNull { it.displayName == JoozdterPrefs.pickedCalendar() }
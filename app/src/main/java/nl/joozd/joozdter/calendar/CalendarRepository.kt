package nl.joozd.joozdter.calendar

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import nl.joozd.joozdcalendarapi.CalendarDescriptor
import nl.joozd.joozdcalendarapi.getCalendars
import nl.joozd.joozdter.App

class CalendarRepository(val context:Context = App.instance) {

    private val _calendarsFlow = MutableStateFlow<List<CalendarDescriptor>>(emptyList())
    val calendarsFlow: StateFlow<List<CalendarDescriptor>> get() = _calendarsFlow

    @RequiresPermission(Manifest.permission.READ_CALENDAR)
    suspend fun updateCalendarsList(){
        _calendarsFlow.value = context.getCalendars()
    }

    @RequiresPermission(Manifest.permission.WRITE_CALENDAR)
    suspend fun switchCalendars(oldCalendar: CalendarDescriptor?, calendar: CalendarDescriptor) {
        TODO()
    }
}
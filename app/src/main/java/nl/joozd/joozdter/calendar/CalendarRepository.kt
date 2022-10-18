package nl.joozd.joozdter.calendar

import android.Manifest
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.joozd.joozdcalendarapi.CalendarDescriptor
import nl.joozd.joozdcalendarapi.CalendarEvent
import nl.joozd.joozdcalendarapi.EventsExtractor
import nl.joozd.joozdcalendarapi.getCalendars
import nl.joozd.joozdter.App
import nl.joozd.joozdter.data.Day
import nl.joozd.joozdter.data.events.Event
import nl.joozd.joozdter.data.room.EventsDatabase
import nl.joozd.joozdter.data.room.RoomEvent
import nl.joozd.joozdter.data.utils.getPickedCalendarFromList
import nl.joozd.joozdter.exceptions.NoCalendarSelectedException
import nl.joozd.joozdter.exceptions.NotARosterException
import nl.joozd.joozdter.model.filterEvents
import nl.joozd.joozdter.utils.extensions.atEndOfDay
import java.time.Instant
import java.time.ZoneOffset

/**
 * This is the one-stop-shop for data going to calendar, and keeping track of what data should be in calendar.
 * It uses [EventsDatabase] to keep track of which entries are ours.
 */
class CalendarRepository(val context:Context = App.instance) {
    private var selectedCalendar: CalendarDescriptor? = null
    private val eventsDao = EventsDatabase.getDatabase(context).eventDao()

    private val _calendarsFlow = MutableStateFlow<List<CalendarDescriptor>>(emptyList())
    val calendarsFlow: StateFlow<List<CalendarDescriptor>> get() = _calendarsFlow

    // This loads the list of calendars on the device, and tries to set active calendar to one of them.
    // Call this before performing actions, but after checking permission.
    @RequiresPermission(Manifest.permission.READ_CALENDAR)
    suspend fun updateCalendarsList(){
        _calendarsFlow.value = context.getCalendars().also{ cals ->
            selectedCalendar = getPickedCalendarFromList(cals)
        }
    }


    // Moving calendar will wipe all data from old calendar, and clear EventsDatabase
    @RequiresPermission(Manifest.permission.WRITE_CALENDAR)
    suspend fun switchCalendarTo(calendar: CalendarDescriptor) {
        val oldCalendar = selectedCalendar
        val savedEntries = eventsDao.getAll()
        selectedCalendar = calendar
        //async move all entries in old calendar to new one
        MainScope().launch{
            savedEntries.forEach{
                findEventInCalendar(it, oldCalendar)?.copy(calendarID = selectedCalendar!!.ID)?.save(context) // move all entries to new calendar
            }
        }
    }

    /*
     * - Get events from EventsDatabase
     * - Make 3 lists:
     *      - events that get to be deleted
     *      - events that can be left alone
     *      - new events.
     * - delete the ones to be deleted (separate thread)
     * - Store the new events (separate thread)
     * - Check the ones that can stay if they are still in calendar, add them if not (from Days) (separate thread)
     *
     * Every one of these actions should also update EventsDatabase.
     */
    @RequiresPermission(Manifest.permission.WRITE_CALENDAR)
    suspend fun mergeRosterDaysIntoCalendar(days: List<Day>){
        val period = getPeriodFromDays(days) ?: throw(NotARosterException("Empty list of days passed to CalendarRepository"))
        val eventsInDB = getEventsFromDb(period)
        val wantedEventsInDays = filterEvents(days.map { it.events }.flatten())

        val eventsToBeDeleted = eventsInDB.filter { dbEvent -> dbEvent !in wantedEventsInDays.toRoomEvents() } // RoomEvent
        deleteEvents(eventsToBeDeleted) // runs async

        val eventsToBeLeftAlone = getKnownEvents(days, eventsInDB) // Event
        checkEventsToBeLeftAlone(eventsToBeLeftAlone) // runs async

        val eventsToSave = wantedEventsInDays.filter { it !in eventsToBeLeftAlone }
        saveToCalendarAndDB(eventsToSave) // does not run async

    }

    private suspend fun findEventInCalendar(event: RoomEvent, calendar: CalendarDescriptor? = null): CalendarEvent? =
        EventsExtractor{
            fromCalendar(calendar ?: selectedCalendar ?: throw (NoCalendarSelectedException("No calendar selected in repository")))
            startAtEpochMilli(event.startTime)
            endAtEpochMilli(event.endTime)
        }
            .extract(context)
            .firstOrNull { it.title == event.title }

    private suspend fun findEventInCalendar(event: Event): CalendarEvent? =
        EventsExtractor{
            fromCalendar(selectedCalendar ?: throw (NoCalendarSelectedException("No calendar selected in repository")))
            startAtEpochMilli(event.startEpochMilli!!)
            endAtEpochMilli(event.endEpochMilli!!)
        }
            .extract(context)
            .firstOrNull { it.title == event.name }


    private fun List<Event>.toRoomEvents() =
        mapNotNull{ it.toRoomEvent() }

    private fun getPeriodFromDays(days: List<Day>): ClosedRange<Instant>?{
        val start = days.minOfOrNull { it.date }?.atStartOfDay(ZoneOffset.UTC)?.toInstant() ?: return null
        val end = days.maxOfOrNull { it.date }?.atEndOfDay(ZoneOffset.UTC)?.toInstant() ?: return null
        return start..end
    }

    private suspend fun getEventsFromDb(period: ClosedRange<Instant>): List<RoomEvent> =
        eventsDao.getEvents(
            earliestStart = period.start.toEpochMilli(),
            latestStart = period.endInclusive.toEpochMilli()
        )

    //Deletes events in it's own coroutine.
    @RequiresPermission(Manifest.permission.WRITE_CALENDAR)
    private fun deleteEvents(eventsToBeDeleted: List<RoomEvent>){
        MainScope().launch {
            val calendarEvents = eventsToBeDeleted.mapNotNull { findEventInCalendar(it) }
            Log.d(this::class.simpleName, "Found ${calendarEvents.size} events to delete...")
            deleteEventsFromCalendar(calendarEvents)
        }
    }

    // Checks events in it's own coroutine, adds missing ones.
    @RequiresPermission(Manifest.permission.WRITE_CALENDAR)
    private fun checkEventsToBeLeftAlone(eventsToBeLeftAlone: List<Event>){
        MainScope().launch {
            val eventsInCalendar = eventsToBeLeftAlone.filter { findEventInCalendar(it) != null }
            val eventsNotInCalendar = eventsToBeLeftAlone.filter { it !in eventsInCalendar}
            deleteEventsFromDatabase(eventsNotInCalendar.map { it.toRoomEvent() })
            saveToCalendarAndDB(eventsNotInCalendar)
        }
    }

    @RequiresPermission(Manifest.permission.WRITE_CALENDAR)
    private suspend fun deleteEventsFromCalendar(
        eventsToDelete: List<CalendarEvent>
    ) = withContext(Dispatchers.IO){
        eventsToDelete.forEach {
            it.delete(context)
        }
        Unit
    }

    private suspend fun deleteEventsFromDatabase(
        eventsToDelete: List<RoomEvent>
    ) = withContext(Dispatchers.IO){
        eventsDao.deleteEvents(eventsToDelete)
    }

    private fun getKnownEvents(days: List<Day>, knownEvents: List<RoomEvent>): List<Event>{
        val events = days.map { it.events }.flatten()
        return events.filter{ event ->
            knownEvents.any{ k -> k.equals(event) } // doing it like this because `RoomEvent.equals()` only works one way.
        }
    }

    @RequiresPermission(Manifest.permission.WRITE_CALENDAR)
    private suspend fun saveToCalendarAndDB(events: List<Event>){
        events.map {it.toCalendarEvent()}.filterNotNull().forEach {
            it.save(context, selectedCalendar)
            saveEventToDb(it)
        }
    }

    private suspend fun saveEventToDb(event: CalendarEvent){
        eventsDao.insertEvents(RoomEvent(event))
    }


    companion object{
        val instance by lazy { CalendarRepository() }
    }
}
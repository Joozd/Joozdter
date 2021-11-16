package nl.joozd.joozdter.calendar

import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import android.util.Log
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import nl.joozd.joozdter.App
import nl.joozd.joozdter.data.EventTypes
import nl.joozd.joozdter.data.JoozdterPrefs
import nl.joozd.joozdter.data.events.AllDayevent
import nl.joozd.joozdter.data.events.Event
import nl.joozd.joozdter.exceptions.NoCalendarSelectedException
import nl.joozd.joozdter.utils.InstantRange
import nl.joozd.joozdter.utils.extensions.addNotNull
import java.time.Instant

class CalendarHandler {
    private val context: Context get() = App.instance

    private var cachedCalendars: List<CalendarDescriptor>? = null
    private var cachedActiveCalendar: CalendarDescriptor? = null

    /**
     * Returns a list of calendars on this device
     * Caches them as well.
     */
    @RequiresPermission(Manifest.permission.WRITE_CALENDAR)
    suspend fun getCalendarsFromDisk(): List<CalendarDescriptor> = withContext(Dispatchers.IO) {
        getCalendarDescriptors().also{
            cachedCalendars = it
        }
    }

    @RequiresPermission(Manifest.permission.WRITE_CALENDAR)
    suspend fun getCalendars() = cachedCalendars ?: getCalendarsFromDisk()

    private val activeCalendarMutex = Mutex()
    /**
     * Get the active calendar, or null if not found
     */
    @RequiresPermission(Manifest.permission.WRITE_CALENDAR)
    suspend fun activeCalendar(): CalendarDescriptor? = activeCalendarMutex.withLock {
        cachedActiveCalendar
            ?: getCalendars().singleOrNull { it.name == JoozdterPrefs.pickedCalendar }
                ?.also{ cachedActiveCalendar = it }
    }

    /**
     * Insert events into [activeCalendar]
     * returns list of Events with updated IDs
     * Will throw a [NoCalendarSelectedException] if no active calendar present
     */
    @RequiresPermission(Manifest.permission.WRITE_CALENDAR)
    suspend fun insertEvents(eventsList: Collection<Event>): List<Event?> = withContext(Dispatchers.IO){
        val calendar = activeCalendar() ?: throw(NoCalendarSelectedException("No calendar selected"))

        println("CalendarHandlerNew.insertEvents()")
        println("calendar: $calendar")
        println("received ${eventsList.size} events to save")

        eventsList.map{ event ->
            val insertedID = insertEventAndReturnEventID(event, calendar)
            event.copy(id = insertedID)
        }
    }


    @RequiresPermission(Manifest.permission.WRITE_CALENDAR)
    suspend fun moveEventsToNewCalendar(events: Collection<Event>, newCalendarID: Long){
        events.forEach{ event ->
            putEventInNewCalendar(event, newCalendarID)
        }
    }

    @RequiresPermission(Manifest.permission.WRITE_CALENDAR)
    private suspend fun putEventInNewCalendar(event: Event, calID: Long){
        val updateUri = makeUriForEvent(event)
        val values = makeValuesForNewCalendar(calID)
        println("Changing event ${event.id}...")
        println("found in calendar as ${getEventByID(event.id!!)}")
        context.contentResolver.update(updateUri, values, null, null).let{
            println("changed $it lines")
        }
    }


    private fun makeUriForEvent(event: Event) =
        ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, event.id!!)

    private fun makeValuesForNewCalendar(calID: Long) = ContentValues().apply {
            // The new title for the event
            put(CalendarContract.Events.CALENDAR_ID, calID)
        }

    /**
     * Insert [event] into [calendar]
     * @return the ID of the event after being inserted (it can be found back by this ID afterwards)
     */
    @RequiresPermission(Manifest.permission.WRITE_CALENDAR)
    private fun insertEventAndReturnEventID(
        event: Event,
        calendar: CalendarDescriptor
    ) = insertEventIntoCalendar(event, calendar)?.lastPathSegment?.toLong()

    /**
     * Insert [event] into [calendar]
     * @return the URI of the event being inserted
     */
    @RequiresPermission(Manifest.permission.WRITE_CALENDAR)
    private fun insertEventIntoCalendar( event: Event, calendar: CalendarDescriptor): Uri? =
        context.contentResolver.insert(
            CalendarContract.Events.CONTENT_URI,
            makeContentValuesForEvent(event, calendar.calID))

    @RequiresPermission(Manifest.permission.WRITE_CALENDAR)
    suspend fun deleteEvents(eventsToDelete: Collection<Event>) {
        eventsToDelete.forEach { event ->
            event.id?.let { eventID ->
                deleteEventByID(eventID)
            }
        }
    }

    @RequiresPermission(Manifest.permission.WRITE_CALENDAR)
    suspend fun deleteEventByID(id: Long) = withContext(Dispatchers.IO){
        val deleteUri: Uri =
            ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, id)
        context.contentResolver.delete(deleteUri, null, null).let{
            Log.d("CalendarHandler", "deleted $it rows")
        }
    }


    /**
     * DO NOT USE THIS UNLESS REALLY SURE A CALENDAR CAN BE NUKED
     *
     */

    @RequiresPermission(Manifest.permission.WRITE_CALENDAR)
    suspend fun nukeCalendar(range: InstantRange) {
        println("NUKING CALENDAR ${activeCalendar()} FOR PERIOD $range")
        oldGetEventIDSInRange(range).forEach{
            println("NUKING $it")
            deleteEventByID(it)
        }
    }



    @RequiresPermission(Manifest.permission.WRITE_CALENDAR)
    suspend fun showCalendar(range: InstantRange, knownEvents: Collection<Event>? = null) {
        println("showCalendar started")
        val ids = getAllIDStartingInRange(range)
        println("found ${ids.size} events in range ${range.startDate} .. ${range.endDate}")

        ids.forEach{
            println("FOUND: ")
            println(getEventByID(it))
            if (knownEvents != null) {
                println("EXPECTED: ")
                println(knownEvents.firstOrNull { e -> e.id == it })
            }
            println("-o0o-\n\n")
        }
    }




    /**
     * Remove Joozdter Legacy Events from calendar
     */
    @RequiresPermission(Manifest.permission.WRITE_CALENDAR)
    suspend fun removeLegacyEvents(range: InstantRange) {
        getLegacyIDsStartingInRange(range).forEach{ idOfLegacyEvent ->
            deleteEventByID(idOfLegacyEvent)
        }
    }

    /**
     * This function is only used in nuking entire blocks of Calendar Events.
     */

    @RequiresPermission(Manifest.permission.WRITE_CALENDAR)
    private suspend fun getAllIDStartingInRange(range: InstantRange): List<Long> = withContext(Dispatchers.IO){
        val foundEvents: MutableList<Long> = mutableListOf()

        activeCalendar()?.let { calendar ->
            buildEventsCursorWithInstantRange(calendar, range)?.use { cur ->
                while (cur.moveToNext()) {
                    if (cur.getInt(CalendarHandlerIndices.EVENT_DELETED_INDEX) == 0)
                        foundEvents.add(cur.getLong(CalendarHandlerIndices.EVENT_ID_INDEX) )
                }
            }
        } ?: error ("No active calendar x")
        foundEvents
    }

    /**
     * Get an event from calendar by ID
     */
    @RequiresPermission(Manifest.permission.WRITE_CALENDAR)
    private suspend fun getEventByID(id: Long): Event? = withContext(Dispatchers.IO){
        val foundEvents: MutableList<Event> = mutableListOf()
        buildEventsByIDCursor(id)?.use { cur ->
                while (cur.moveToNext()) {
                    foundEvents.addNotNull(cur.buildEvent())
                }
        } ?: return@withContext null
        println("Found ${foundEvents.size} events (should be 1)")
        foundEvents.firstOrNull()
    }


    private suspend fun getCalendarDescriptors(): List<CalendarDescriptor>{
        val results = ArrayList<CalendarDescriptor>()
        buildCalendarCursor()?.use { cur ->
            while (cur.moveToNext()) {
                results.add(cur.getCalendarDescriptor())
            }
        }
        return results
    }

    private fun makeContentValuesForEvent(event: Event, calendarID: Long): ContentValues =
        ContentValues().apply {
            put(CalendarContract.Events.DTSTART, event.startEpochMilli)
            put(CalendarContract.Events.DTEND, event.endEpochMilli)
            put(CalendarContract.Events.TITLE, event.name)
            put(CalendarContract.Events.DESCRIPTION, event.notes)
            put(CalendarContract.Events.CALENDAR_ID, calendarID)
            put(CalendarContract.Events.EVENT_TIMEZONE, "UTC")
            put(CalendarContract.Events.EVENT_LOCATION, event.info)
            if (event is AllDayevent) put(
                CalendarContract.Events.ALL_DAY,
                1
            )
        }

    /*
    TODO figure out why this doesn't work
    @RequiresPermission(Manifest.permission.WRITE_CALENDAR)
    private suspend fun getLegacyIDsStartingInRange(range: InstantRange): List<Long> = withContext(Dispatchers.IO) {
        val foundEvents: MutableList<Long> = mutableListOf()
        activeCalendar()?.let { calendar ->
            buildEventsCursorWithInstantRange(calendar, range)?.use { cur ->
                while (cur.moveToNext()) {
                    foundEvents.addNotNull(cur.getIdFromLegacyEventInActiveCalendar(calendar))
                }
            }
        } ?: error ("No active calendar x")
        foundEvents
    }
    */

    /**
     * Get IDs of all events starting in [range] that have [LEGACY_IDENTIFIER] in their description
     */
    @RequiresPermission(Manifest.permission.WRITE_CALENDAR)
    private suspend fun getLegacyIDsStartingInRange(range: InstantRange): List<Long> = withContext(Dispatchers.IO) {
        val foundEvents: MutableList<Long> = mutableListOf()

        activeCalendar()?.let { cal ->
            val uri: Uri = CalendarContract.Events.CONTENT_URI
            val selection: String = "((${CalendarContract.Events.CALENDAR_ID} = ?) AND (" +
                    "${CalendarContract.Events.DTSTART} >= ?) AND (" +
                    "${CalendarContract.Events.DTSTART} < ?))"
            val selectionArgs: Array<String> = arrayOf(
                cal.calID.toString(),
                range.start.toEpochMilli().toString(),
                range.endInclusive.toEpochMilli().toString()
            )
            context.contentResolver.query(
                uri,
                CalendarHandlerIndices.EVENT_PROJECTION,
                selection,
                selectionArgs,
                null
            )?.use { cur ->
                while (cur.moveToNext()) {
                    if (LEGACY_IDENTIFIER in cur.getString(CalendarHandlerIndices.EVENT_DESCRIPTION_INDEX)
                        && cur.getInt(CalendarHandlerIndices.EVENT_DELETED_INDEX) == 0
                    ) {
                        foundEvents.add(cur.getLong(CalendarHandlerIndices.EVENT_ID_INDEX))
                    }
                }
            }
        }
        foundEvents
    }


    /**
     * Legacy function for debugging (this worked somewhere else)
     */
@RequiresPermission(Manifest.permission.WRITE_CALENDAR)
private suspend fun oldGetEventIDSInRange(range: InstantRange): List<Long> = withContext (Dispatchers.IO){
    val foundEvents: MutableList<Long> = mutableListOf()

    activeCalendar()?.let { calendar ->
        val uri: Uri = CalendarContract.Events.CONTENT_URI
        val selection: String = "((${CalendarContract.Events.CALENDAR_ID} = ?) AND (" +
                "${CalendarContract.Events.DTSTART} >= ?) AND (" +
                "${CalendarContract.Events.DTSTART} < ?))"
        val selectionArgs: Array<String> = arrayOf(
            calendar.calID.toString(),
            range.start.toEpochMilli().toString(),
            range.endInclusive.toEpochMilli().toString()
        )
        context.contentResolver.query(
            uri,
            CalendarHandlerIndices.EVENT_PROJECTION,
            selection,
            selectionArgs,
            null
        )?.use { cur ->
            while (cur.moveToNext()) {
                if (//cur.getLong(EVENT_CALENDAR_ID_INDEX) == calendar.calID
                    cur.getInt(CalendarHandlerIndices.EVENT_DELETED_INDEX) == 0)
                {
                    foundEvents.add(
                            cur.getLong(CalendarHandlerIndices.EVENT_ID_INDEX)
                        )
                }
            }
        }
    }
    foundEvents
}


/**
 * Builds a cursor for getting items from calendar in a specific range.
 */
private fun buildEventsCursorWithInstantRange(calendar: CalendarDescriptor, range: InstantRange)
: Cursor?{
    println("Building cursor for calendar $calendar and range $range")
    val uri: Uri = CalendarContract.Events.CONTENT_URI
    val selection: String = "((${CalendarContract.Events.CALENDAR_ID} = ?) AND (" +
            "${CalendarContract.Events.DTSTART} >= ?) AND (" +
            "${CalendarContract.Events.DTSTART} < ?))"
    val selectionArgs: Array<String> = arrayOf(
        calendar.calID.toString(),
        range.startMillisString(),
        range.endMillisString()
    )
    return context.contentResolver.query(uri,CalendarHandlerIndices.EVENT_PROJECTION,selection,selectionArgs,null)
}

    private fun buildEventsByIDCursor(id: Long): Cursor?{
        val uri: Uri = CalendarContract.Events.CONTENT_URI
        val selection: String = "(${CalendarContract.Events._ID} = ?)"
        val selectionArgs: Array<String> = arrayOf(
            id.toString()
        )
        return context.contentResolver.query(uri,CalendarHandlerIndices.EVENT_PROJECTION, selection, selectionArgs, null)
    }


    private fun buildCalendarCursor(): Cursor?{
        val uri: Uri = CalendarContract.Calendars.CONTENT_URI
        return context.contentResolver.query(uri, CalendarHandlerIndices.CALENDAR_PROJECTION, null, null, null)
    }

    /**
     * get ID from an event if it has [LEGACY_IDENTIFIER] in it's description, else return null
     */
    @RequiresPermission(Manifest.permission.WRITE_CALENDAR)
    private suspend fun Cursor.getIdFromLegacyEventInActiveCalendar(calendar: CalendarDescriptor)
    : Long? = withContext(Dispatchers.IO) {
        if (getLong(CalendarHandlerIndices.EVENT_CALENDAR_ID_INDEX) == calendar.calID
            && LEGACY_IDENTIFIER in getString(CalendarHandlerIndices.EVENT_DESCRIPTION_INDEX)
            && getInt(CalendarHandlerIndices.EVENT_DELETED_INDEX) == 0
        )
            getLong(CalendarHandlerIndices.EVENT_ID_INDEX)
        else null
    }

    private suspend fun Cursor.getCalendarDescriptor(): CalendarDescriptor = withContext(Dispatchers.IO){
        val calID: Long = getLong(CalendarHandlerIndices.PROJECTION_ID_INDEX)
        val displayName: String = getString(CalendarHandlerIndices.PROJECTION_DISPLAY_NAME_INDEX)
        val accountName: String = getString(CalendarHandlerIndices.PROJECTION_ACCOUNT_NAME_INDEX)
        val ownerName: String = getString(CalendarHandlerIndices.PROJECTION_OWNER_ACCOUNT_INDEX)
        val name: String = getString(CalendarHandlerIndices.PROJECTION_NAME_INDEX)
        val color: Int = getInt(CalendarHandlerIndices.PROJECTION_CALENDAR_COLOR)

        CalendarDescriptor(
            calID,
            displayName,
            accountName,
            ownerName,
            name,
            color)
    }

    private suspend fun Cursor.buildEvent(): Event = withContext(Dispatchers.IO) {
        Event(getString(CalendarHandlerIndices.EVENT_TITLE_INDEX),
        EventTypes.UNKNOWN_EVENT,
        Instant.ofEpochMilli(getLong(CalendarHandlerIndices.EVENT_DTSTART_INDEX)),
        Instant.ofEpochMilli(getLong(CalendarHandlerIndices.EVENT_DTEND_INDEX)),
        getString(CalendarHandlerIndices.EVENT_EVENT_LOCATION_INDEX),
        "CalendarID = ${getLong(CalendarHandlerIndices.EVENT_CALENDAR_ID_INDEX)}",
        getLong(CalendarHandlerIndices.EVENT_ID_INDEX)
        )
    }


    companion object {
        private const val LEGACY_IDENTIFIER = "Inserted by Joozdter"
    }


}
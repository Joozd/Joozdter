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
import nl.joozd.joozdter.data.JoozdterPrefs
import nl.joozd.joozdter.data.events.AllDayevent
import nl.joozd.joozdter.data.events.Event
import nl.joozd.joozdter.exceptions.NoCalendarSelectedException
import nl.joozd.joozdter.utils.InstantRange
import nl.joozd.joozdter.utils.extensions.addNotNull

class CalendarHandlerNew {
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
            Log.d(this::class.simpleName, "deleted $it rows")
        }
    }


    /**
     * DO NOT USE THIS UNLESS REALLY SURE A CALENDAR CAN BE NUKED
     *
     */
    /*
    @RequiresPermission(Manifest.permission.WRITE_CALENDAR)
    suspend fun nukeCalendar(range: InstantRange) {
        getAllIDStartingInRange(range).forEach{
            println("NUKING $it")
            deleteEventByID(it)
        }
    }

     */

    /**
     * Remove Joozdter Legacy Events from calendar
     */
    @RequiresPermission(Manifest.permission.WRITE_CALENDAR)
    suspend fun removeLegacyEvents(range: InstantRange) {
        getLegacyIDsStartingInRange(range).forEach{ idOFLegacyEvent ->
            deleteEventByID(idOFLegacyEvent)
        }
    }

    /**
     * This ugly function is only used in nuking entire blocks of Calendar Events.
     */
    /*
    @RequiresPermission(Manifest.permission.WRITE_CALENDAR)
    private suspend fun getAllIDStartingInRange(range: InstantRange): List<Long> = withContext(Dispatchers.IO){
        val foundEvents: MutableList<Long> = mutableListOf()

        activeCalendar()?.let { calendar ->
            buildCalendarCursorWithInstantRange(calendar, range)?.use { cur ->
                while (cur.moveToNext()) {
                    if (cur.getLong(EVENT_CALENDAR_ID_INDEX) == calendar.calID
                        && cur.getInt(EVENT_DELETED_INDEX) == 0)
                            foundEvents.add( cur.getLong(EVENT_ID_INDEX) )
                }
            }
        } ?: error ("No active calendar x")
        foundEvents
    }
    */

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

    /**
     * Builds a cursor for getting items from calendar in a specific range.
     */
    private fun buildEventsCursorWithInstantRange(calendar: CalendarDescriptor, range: InstantRange)
    : Cursor?{
        val uri: Uri = CalendarContract.Events.CONTENT_URI
        val selection: String = "((${CalendarContract.Events.CALENDAR_ID} = ?) AND (" +
                "${CalendarContract.Events.DTSTART} >= ?) AND (" +
                "${CalendarContract.Events.DTSTART} < ?))"
        val selectionArgs: Array<String> = arrayOf(
            calendar.calID.toString(),
            range.startMillisString(),
            range.endMillisString()
        )
        return context.contentResolver.query(uri,EVENT_PROJECTION,selection,selectionArgs,null)
    }


    private fun buildCalendarCursor(): Cursor?{
        val uri: Uri = CalendarContract.Calendars.CONTENT_URI
        return context.contentResolver.query(uri, CALENDAR_PROJECTION, null, null, null)
    }

    private suspend fun Cursor.getIdFromLegacyEventInActiveCalendar(calendar: CalendarDescriptor)
    : Long? = withContext(Dispatchers.IO) {
        if (getLong(EVENT_CALENDAR_ID_INDEX) == calendar.calID
            && LEGACY_IDENTIFIER in getString(EVENT_DESCRIPTION_INDEX)
            && getInt(EVENT_DELETED_INDEX) == 0
        )
            getLong(EVENT_ID_INDEX)
        else null
    }

    private suspend fun Cursor.getCalendarDescriptor(): CalendarDescriptor = withContext(Dispatchers.IO){
        val calID: Long = getLong(PROJECTION_ID_INDEX)
        val displayName: String = getString(PROJECTION_DISPLAY_NAME_INDEX)
        val accountName: String = getString(PROJECTION_ACCOUNT_NAME_INDEX)
        val ownerName: String = getString(PROJECTION_OWNER_ACCOUNT_INDEX)
        val name: String = getString(PROJECTION_NAME_INDEX)
        val color: Int = getInt(PROJECTION_CALENDAR_COLOR)

        CalendarDescriptor(
            calID,
            displayName,
            accountName,
            ownerName,
            name,
            color)
    }


    companion object {
        // The indices for the projection array below.
        private const val PROJECTION_ID_INDEX: Int = 0
        private const val PROJECTION_ACCOUNT_NAME_INDEX: Int = 1
        private const val PROJECTION_DISPLAY_NAME_INDEX: Int = 2
        private const val PROJECTION_OWNER_ACCOUNT_INDEX: Int = 3
        private const val PROJECTION_NAME_INDEX: Int = 4
        private const val PROJECTION_CALENDAR_COLOR: Int = 5

        private const val EVENT_ID_INDEX: Int = 0
        private const val EVENT_CALENDAR_ID_INDEX: Int = 1
        private const val EVENT_TITLE_INDEX: Int = 2
        private const val EVENT_EVENT_LOCATION_INDEX: Int = 3
        private const val EVENT_DESCRIPTION_INDEX: Int = 4
        private const val EVENT_DTSTART_INDEX: Int = 5
        private const val EVENT_DTEND_INDEX: Int = 6
        private const val EVENT_ALL_DAY_INDEX: Int = 7
        private const val EVENT_DELETED_INDEX: Int = 8

        private const val LEGACY_IDENTIFIER = "Inserted by Joozdter"

        private val CALENDAR_PROJECTION: Array<String> = arrayOf(
            CalendarContract.Calendars._ID,                     // 0
            CalendarContract.Calendars.ACCOUNT_NAME,            // 1
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,   // 2
            CalendarContract.Calendars.OWNER_ACCOUNT,           // 3
            CalendarContract.Calendars.NAME,                    // 4
            CalendarContract.Calendars.CALENDAR_COLOR           // 5
        )

        private val EVENT_PROJECTION: Array<String> = arrayOf(
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
    }


}
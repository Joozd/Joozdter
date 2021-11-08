package nl.joozd.joozdter.calendar

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import nl.joozd.joozdter.App
import nl.joozd.joozdter.data.JoozdterPrefs
import nl.joozd.joozdter.model.Event
import nl.joozd.klcrosterparser.Activities

class CalendarHandlerNew {
    private val context: Context get() = App.instance

    private var cachedCalendars: List<CalendarDescriptor>? = null
    private var cachedActiveCalendar: CalendarDescriptor? = null


    /**
     * Returns a list of calendars on this device
     * Caches them as well, if [allowCache] is true it will use cache instead of getting actual data
     */
    suspend fun getCalendars(allowCache: Boolean = false): List<CalendarDescriptor>? = withContext(Dispatchers.IO) {
        if (allowCache && cachedCalendars != null) return@withContext cachedCalendars

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR)
            != PackageManager.PERMISSION_GRANTED
        ) return@withContext null

        val results = ArrayList<CalendarDescriptor>()
        val uri: Uri = CalendarContract.Calendars.CONTENT_URI
        context.contentResolver.query(uri, CALENDAR_PROJECTION, null, null, null)?.use { cur ->
            while (cur.moveToNext()) {
                // Get the field values
                val calID: Long = cur.getLong(PROJECTION_ID_INDEX)
                val displayName: String = cur.getString(PROJECTION_DISPLAY_NAME_INDEX)
                val accountName: String = cur.getString(PROJECTION_ACCOUNT_NAME_INDEX)
                val ownerName: String = cur.getString(PROJECTION_OWNER_ACCOUNT_INDEX)
                val name: String = cur.getString(PROJECTION_NAME_INDEX)
                val color: Int = cur.getInt(PROJECTION_CALENDAR_COLOR)
                results.add(
                    CalendarDescriptor(
                        calID,
                        displayName,
                        accountName,
                        ownerName,
                        name,
                        color
                    )
                )
            }
        } ?: error { "ERROR HTP GRGR" }
        results.also{
            cachedCalendars = it
        }
    }

    private val activeCalendarMutex = Mutex()
    /**
     * Get the active calendar, or null if not found
     */
    suspend fun activeCalendar(): CalendarDescriptor? = activeCalendarMutex.withLock {
        cachedActiveCalendar
            ?: getCalendars(true)?.singleOrNull { it.name == JoozdterPrefs.pickedCalendar }
    }

    /**
     * Set the active calendar. Returns ots descriptor, or null if not found on device
     */
    suspend fun setActiveCalendar(name: String): CalendarDescriptor? = activeCalendarMutex.withLock {
        cachedActiveCalendar = getCalendars(false)?.singleOrNull { it.name == name }
        return cachedActiveCalendar
    }

    /**
     * Insert events into [activeCalendar]
     */
    suspend fun insertEvents(eventsList: List<Event>) = withContext(Dispatchers.IO){
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR)
            != PackageManager.PERMISSION_GRANTED) return@withContext
        eventsList.forEach{ event ->
            val values = ContentValues().apply {
                put(CalendarContract.Events.DTSTART, event.startInstant)
                put(CalendarContract.Events.DTEND, event.endInstant)
                put(CalendarContract.Events.TITLE, event.description)
                put(CalendarContract.Events.DESCRIPTION, event.notes)
                put(CalendarContract.Events.CALENDAR_ID, activeCalendar()!!.calID)
                put(CalendarContract.Events.EVENT_TIMEZONE, "UTC")
                put(CalendarContract.Events.EVENT_LOCATION, event.extraData)
                if (event.eventType == Activities.LEAVE)put(
                    CalendarContract.Events.ALL_DAY,
                    1
                )
            }
            context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
        }
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

        private const val IDENTIFIER = "Inserted by Joozdter"

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
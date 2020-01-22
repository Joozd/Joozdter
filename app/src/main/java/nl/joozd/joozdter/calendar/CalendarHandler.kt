package nl.joozd.joozdter.calendar

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import android.util.Log
import androidx.core.content.ContextCompat
import nl.joozd.joozdter.data.Event
import nl.joozd.joozdter.data.SharedPrefKeys
import nl.joozd.klcrosterparser.Activities
import org.jetbrains.anko.doAsync
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.threeten.bp.temporal.ChronoUnit

class CalendarHandler(private val context: Context){

    val calendarsList: MutableList<CalendarDescriptor> = mutableListOf()
    var activeCalendar: CalendarDescriptor? = null

    class OnInit(val f: ()-> Unit){
        fun init(){
            f()
        }
    }
    var onInit: OnInit? = null
    var initialized=false

    companion object{
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




    fun initialize() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR)
            != PackageManager.PERMISSION_GRANTED) return
        val uri: Uri = CalendarContract.Calendars.CONTENT_URI
        context.contentResolver.query(uri, CALENDAR_PROJECTION, null, null, null)!!.use {cur ->
            while (cur.moveToNext()) {
                // Get the field values
                val calID: Long = cur.getLong(PROJECTION_ID_INDEX)
                val displayName: String = cur.getString(PROJECTION_DISPLAY_NAME_INDEX)
                val accountName: String = cur.getString(PROJECTION_ACCOUNT_NAME_INDEX)
                val ownerName: String = cur.getString(PROJECTION_OWNER_ACCOUNT_INDEX)
                val name: String = cur.getString(PROJECTION_NAME_INDEX)
                val color: Int = cur.getInt(PROJECTION_CALENDAR_COLOR)
                calendarsList.add(
                    CalendarDescriptor(
                        calID,
                        displayName,
                        accountName,
                        ownerName,
                        name,
                        color
                    )
                ) //, name))
                Log.d(
                    "Calendar found",
                    "id=$calID, displayName=$displayName, accountName=$accountName, ownerName=$ownerName, name=$name, color=$color"
                )
                onInit?.init()
                initialized = true
            }
        }
    }

    fun findCalendarByName(name: String?): CalendarDescriptor? = calendarsList.singleOrNull{it.name == name}

    fun getEventsTouching(dateInstant: Instant): List<Event> {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR)
            != PackageManager.PERMISSION_GRANTED) return emptyList()
        val foundEvents: MutableList<Event> = mutableListOf()

        activeCalendar?.let {
            val uri: Uri = CalendarContract.Events.CONTENT_URI
            val selection: String = "((${CalendarContract.Events.CALENDAR_ID} = ?) AND (" +
                    "${CalendarContract.Events.DTEND} > ?) AND (" +
                    "${CalendarContract.Events.DTSTART} < ?))"
            val selectionArgs: Array<String> = arrayOf(activeCalendar!!.calID.toString(), dateInstant.toEpochMilli().toString(), dateInstant.plus(1, ChronoUnit.DAYS).toEpochMilli().toString())
            val cur: Cursor = context.contentResolver.query(uri, EVENT_PROJECTION, selection, selectionArgs, null)!!
            while (cur.moveToNext()) {
                if (cur.getLong(EVENT_CALENDAR_ID_INDEX) == it.calID){
                    foundEvents.add(Event(cur.getString(EVENT_TITLE_INDEX),
                        cur.getString(EVENT_TITLE_INDEX),
                        Instant.ofEpochMilli(cur.getLong(EVENT_DTSTART_INDEX)),
                        Instant.ofEpochMilli(cur.getLong(EVENT_DTEND_INDEX)),
                        cur.getString(EVENT_EVENT_LOCATION_INDEX),
                        "",
                        cur.getLong(EVENT_ID_INDEX)))
                }
            }
            cur.close()
        }
        return foundEvents
    }


    fun getEventsStartingOn(dateInstant: Instant): List<Event> {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR)
            != PackageManager.PERMISSION_GRANTED) return emptyList()
        val foundEvents: MutableList<Event> = mutableListOf()

        activeCalendar?.let {
            val uri: Uri = CalendarContract.Events.CONTENT_URI
            val selection: String = "((${CalendarContract.Events.CALENDAR_ID} = ?) AND (" +
                    "${CalendarContract.Events.DTSTART} >= ?) AND (" +
                    "${CalendarContract.Events.DTSTART} < ?))"
            val selectionArgs: Array<String> = arrayOf(activeCalendar!!.calID.toString(), dateInstant.toEpochMilli().toString(), dateInstant.plus(1, ChronoUnit.DAYS).toEpochMilli().toString())
            val cur: Cursor = context.contentResolver.query(uri, EVENT_PROJECTION, selection, selectionArgs, null)!!
            while (cur.moveToNext()) {
                if (cur.getLong(EVENT_CALENDAR_ID_INDEX) == it.calID &&  IDENTIFIER in cur.getString(EVENT_DESCRIPTION_INDEX) && cur.getInt(EVENT_DELETED_INDEX) == 0){
                    foundEvents.add(Event(cur.getString(EVENT_TITLE_INDEX),
                        cur.getString(EVENT_TITLE_INDEX),
                        Instant.ofEpochMilli(cur.getLong(EVENT_DTSTART_INDEX)),
                        Instant.ofEpochMilli(cur.getLong(EVENT_DTEND_INDEX)),
                        cur.getString(EVENT_EVENT_LOCATION_INDEX),
                        "",
                        cur.getLong(EVENT_ID_INDEX)))
                }
            }
            cur.close()
        }
        return foundEvents
    }

    fun getEventsEndingOn(dateInstant: Instant): List<Event> {

        val foundEvents: MutableList<Event> = mutableListOf()

        activeCalendar?.let {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) return emptyList()
            val uri: Uri = CalendarContract.Events.CONTENT_URI
            val selection: String = "((${CalendarContract.Events.CALENDAR_ID} = ?) AND (" +
                    "${CalendarContract.Events.DTEND} > ?) AND (" +
                    "${CalendarContract.Events.DTEND} < ?))"
            val selectionArgs: Array<String> = arrayOf(activeCalendar!!.calID.toString(), dateInstant.toEpochMilli().toString(), dateInstant.plus(1, ChronoUnit.DAYS).toEpochMilli().toString())
            context.contentResolver.query(uri, EVENT_PROJECTION, selection, selectionArgs, null)!!.use { cur ->
                while (cur.moveToNext()) {
                    if (cur.getLong(EVENT_CALENDAR_ID_INDEX) == it.calID && IDENTIFIER in cur.getString(
                            EVENT_DESCRIPTION_INDEX
                        )
                    ) {
                        foundEvents.add(
                            Event(
                                cur.getString(EVENT_TITLE_INDEX),
                                cur.getString(EVENT_TITLE_INDEX),
                                Instant.ofEpochMilli(cur.getLong(EVENT_DTSTART_INDEX)),
                                Instant.ofEpochMilli(cur.getLong(EVENT_DTEND_INDEX)),
                                cur.getString(EVENT_EVENT_LOCATION_INDEX),
                                "",
                                cur.getLong(EVENT_ID_INDEX)
                            )
                        )
                    }
                }
            }
        }
        return foundEvents
    }

    fun deleteEvents(eventsList: List<Event>){
        val DEBUG_TAG = "DELETERT"
        eventsList.forEach {
            if (it._id != null) {
                val eventID: Long = it._id
                val deleteUri: Uri =
                    ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventID)
                val rows: Int = context.contentResolver.delete(deleteUri, null, null)
                Log.i(DEBUG_TAG, "Rows deleted: $rows")
            }
        }
    }

    fun insertEvents(eventsList: List<Event>, sharedPreferences: SharedPreferences){
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR)
            != PackageManager.PERMISSION_GRANTED) return
        eventsList.forEach{ event ->
            // check event type and if that is to be calendarized:
            var putItIn = true
            when (event.eventType){
                Activities.LEAVE, Activities.CLICK -> putItIn = sharedPreferences.getBoolean(SharedPrefKeys.SHOW_FREE_TIME, true) // shouldnt need default value
                Activities.HOTEL -> putItIn = sharedPreferences.getBoolean(SharedPrefKeys.SHOW_HOTEL, true) // shouldnt need default value
                Activities.TAXI -> putItIn = sharedPreferences.getBoolean(SharedPrefKeys.SHOW_TAXI, true) // shouldnt need default value
                Activities.CHECKIN, Activities.CHECKOUT -> putItIn = sharedPreferences.getBoolean(SharedPrefKeys.SHOW_FLIGHT_DAY, true) // shouldnt need default value
                Activities.FLIGHT -> putItIn = sharedPreferences.getBoolean(SharedPrefKeys.SHOW_FLIGHTS, true) // shouldnt need default value
                Activities.OTHER_DUTY -> putItIn = sharedPreferences.getBoolean(SharedPrefKeys.SHOW_OTHER, true) // shouldnt need default value
                Activities.SIM -> putItIn = sharedPreferences.getBoolean(SharedPrefKeys.SHOW_FREE_TIME, true) // shouldnt need default value
                Activities.ACTUALSIM -> putItIn = sharedPreferences.getBoolean(SharedPrefKeys.SHOW_FREE_TIME, true) // shouldnt need default value
            }
            if (putItIn) {
                doAsync {
                    val values = ContentValues().apply {
                        put(CalendarContract.Events.DTSTART, event.startInstant)
                        put(CalendarContract.Events.DTEND, event.endInstant)
                        put(CalendarContract.Events.TITLE, event.description)
                        put(
                            CalendarContract.Events.DESCRIPTION,
                            "${event.notes}\n\n" + IDENTIFIER
                        )
                        put(CalendarContract.Events.CALENDAR_ID, activeCalendar!!.calID)
                        put(CalendarContract.Events.EVENT_TIMEZONE, "UTC")
                        put(CalendarContract.Events.EVENT_LOCATION, event.extraData)
                        if (event.eventType == Activities.LEAVE)put(CalendarContract.Events.ALL_DAY, 1)
                    }
                    context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
                }
            }
        }
    }

    fun getHotelEvent(dateInstant: Instant): Event? = getEventsEndingOn(dateInstant).firstOrNull{it.eventType == Activities.HOTEL}

    fun getHotelEvent(date: LocalDate): Event? = getEventsEndingOn(date.atStartOfDay().atZone(ZoneId.of("UTC")).toInstant()).firstOrNull{it.eventType == Activities.HOTEL}





}
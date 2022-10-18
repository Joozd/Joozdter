package nl.joozd.joozdter.calendar

import android.content.Context

/**
 * CalendarHandler will take care of handling things in devices calendar.
 * @param context: context to get contentResolver from
 */
@Deprecated("Deprecated")
class CalendarHandlerOld(private val context: Context) {
    /*
    private val _calendarsList: MutableList<CalendarDescriptorOld> = mutableListOf()
    val calendarsList: List<CalendarDescriptorOld>
        get() = _calendarsList
    var activeCalendar: CalendarDescriptorOld? = null
        private set

    fun interface OnInit {
        fun init()
    }

    /*
    interface Observer<T> {
        /**
         * Called when the data is changed.
         * @param t  The new data
         */
        fun onChanged(t: T)
    }

     */

    private var _onInit: OnInit? = null
    var initialized=false

    suspend fun initialize() = withContext(Dispatchers.IO) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR)
            != PackageManager.PERMISSION_GRANTED
        ) return@withContext
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
                _calendarsList.add(
                    CalendarDescriptorOld(
                        calID,
                        displayName,
                        accountName,
                        ownerName,
                        name,
                        color
                    )
                ) //, name))
                _onInit?.init()
                initialized = true
            }
        } ?: error { "ERROR HTP GRGR" }
        setActiveCalendar(JoozdterPrefs.pickedCalendar)
    }


    suspend fun initialize(onInit: OnInit){
        _onInit = onInit
        initialize()
    }

    fun findCalendarByName(name: String?): CalendarDescriptorOld? = if (name == null) null else calendarsList.singleOrNull{it.name == name}

    fun setActiveCalendar(name: String?): Boolean = findCalendarByName(name)?.let{
        activeCalendar = it
        true
    } ?: false

    /*
    fun getEventsTouching(dateInstant: Instant): List<Event> {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR)
            != PackageManager.PERMISSION_GRANTED) return emptyList()
        val foundEvents: MutableList<Event> = mutableListOf()

        activeCalendar?.let {
            val uri: Uri = CalendarContract.Events.CONTENT_URI
            val selection: String = "((${CalendarContract.Events.CALENDAR_ID} = ?) AND (" +
                    "${CalendarContract.Events.DTEND} > ?) AND (" +
                    "${CalendarContract.Events.DTSTART} < ?))"
            val selectionArgs: Array<String> = arrayOf(
                activeCalendar!!.calID.toString(),
                dateInstant.toEpochMilli().toString(),
                dateInstant.plus(
                    1,
                    ChronoUnit.DAYS
                ).toEpochMilli().toString()
            )
            val cur: Cursor? = context.contentResolver.query(
                uri,
                EVENT_PROJECTION,
                selection,
                selectionArgs,
                null
            )
            while (cur?.moveToNext() == true) {
                if (cur.getLong(EVENT_CALENDAR_ID_INDEX) == it.calID){
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
            cur?.close()
        }
        return foundEvents
    }
    */

    /**
     * Will return a list of all [Event]s starting in [range]
     * @param range The range in which events should start to be returned by this function
     * Returns null if no permission to read calendar
     */
    fun getEventsStartingInRange(range: InstantRange): List<Event>?{
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR)
            != PackageManager.PERMISSION_GRANTED) return null
        val foundEvents: MutableList<Event> = mutableListOf()

        activeCalendar?.let {
            val uri: Uri = CalendarContract.Events.CONTENT_URI
            val selection: String = "((${CalendarContract.Events.CALENDAR_ID} = ?) AND (" +
                    "${CalendarContract.Events.DTSTART} >= ?) AND (" +
                    "${CalendarContract.Events.DTSTART} < ?))"
            val selectionArgs: Array<String> = arrayOf(
                activeCalendar!!.calID.toString(),
                range.start.toEpochMilli().toString(),
                range.endInclusive.toEpochMilli().toString()
            )
            context.contentResolver.query(
                uri,
                EVENT_PROJECTION,
                selection,
                selectionArgs,
                null
            )?.use { cur ->
                while (cur.moveToNext()) {
                    if (cur.getLong(EVENT_CALENDAR_ID_INDEX) == it.calID
                    && IDENTIFIER in cur.getString(EVENT_DESCRIPTION_INDEX)
                    && cur.getInt(EVENT_DELETED_INDEX) == 0)
                    {
                        foundEvents.add(Event(cur.getString(EVENT_TITLE_INDEX),
                            cur.getString(EVENT_TITLE_INDEX),
                            Instant.ofEpochMilli(cur.getLong(EVENT_DTSTART_INDEX)),
                            Instant.ofEpochMilli(cur.getLong(EVENT_DTEND_INDEX)),
                            cur.getString(EVENT_EVENT_LOCATION_INDEX),
                            "",
                            cur.getLong(EVENT_ID_INDEX)))
                    }
                }
            }
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
            val selectionArgs: Array<String> = arrayOf(
                activeCalendar!!.calID.toString(),
                dateInstant.toEpochMilli().toString(),
                dateInstant.plus(
                    1,
                    ChronoUnit.DAYS
                ).toEpochMilli().toString()
            )
            val cur: Cursor? = context.contentResolver.query(
                uri,
                EVENT_PROJECTION,
                selection,
                selectionArgs,
                null
            )
            while (cur?.moveToNext() == true) {
                if (cur.getLong(EVENT_CALENDAR_ID_INDEX) == it.calID &&  IDENTIFIER in cur.getString(
                        EVENT_DESCRIPTION_INDEX
                    ) && cur.getInt(EVENT_DELETED_INDEX) == 0){
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
            cur?.close()
        }
        return foundEvents
    }

    /* fun getEventsEndingOn(dateInstant: Instant): List<Event> {
        val foundEvents: MutableList<Event> = mutableListOf()

        activeCalendar?.let {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) return emptyList()
            val uri: Uri = CalendarContract.Events.CONTENT_URI
            val selection: String = "((${CalendarContract.Events.CALENDAR_ID} = ?) AND (" +
                    "${CalendarContract.Events.DTEND} > ?) AND (" +
                    "${CalendarContract.Events.DTEND} < ?))"
            val selectionArgs: Array<String> = arrayOf(
                activeCalendar!!.calID.toString(),
                dateInstant.toEpochMilli().toString(),
                dateInstant.plus(
                    1,
                    ChronoUnit.DAYS
                ).toEpochMilli().toString()
            )
            context.contentResolver.query(uri, EVENT_PROJECTION, selection, selectionArgs, null)?.use { cur ->
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
    */

    suspend fun deleteEvents(eventsList: List<Event>) = withContext (Dispatchers.IO){
        eventsList.forEach {
            if (it._id != null) {
                val eventID: Long = it._id
                val deleteUri: Uri =
                    ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventID)
                context.contentResolver.delete(deleteUri, null, null)
            }
        }
    }

    suspend fun insertEvents(eventsList: List<Event>) = withContext(Dispatchers.IO){
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR)
            != PackageManager.PERMISSION_GRANTED) return@withContext
        eventsList.forEach{ event ->
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
                if (event.eventType == Activities.LEAVE)put(
                    CalendarContract.Events.ALL_DAY,
                    1
                )
            }
            context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
        }
    }

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

     */
}
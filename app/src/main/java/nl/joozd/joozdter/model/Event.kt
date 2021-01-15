package nl.joozd.joozdter.model

import nl.joozd.klcrosterparser.KlcRosterEvent
import java.time.Instant
import java.time.Duration

/**
 * Event that will be put into calendar
 * @param eventType: Type of event as defined in Activities object that comes with KlcRosterParser
 * @param description: Description of activity. This will be the calendar item's name
 * @param startTime: Start time of event
 * @param endTime: End time of event
 * @param extraData: Extra data, this till end up in event's location so it will show up on overview
 * @param notes: Notes for activity, this will include "Inserted by Joozdter" marker (inserted by CalendarHandler)
 * @param _id: id for keeping track of retrieved events from calendar, to be able to delete them.
 */

data class Event (val eventType: String, val description: String, val startTime: Instant, val endTime: Instant, val extraData: String = "", val notes: String = "", val _id: Long? = null){
    val startInstant: Long
        get() = startTime.toEpochMilli()
    val endInstant: Long
        get() = endTime.toEpochMilli()
    val duration: Duration = Duration.between(startTime, endTime)
    override fun toString() = "Event: $eventType / $description / start: $startTime / end: $endTime"
}
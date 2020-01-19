package nl.joozd.joozdter.data

import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.temporal.ChronoUnit

// event_type as in EventTypes object
// description: any string
// start_time: ISO start time string without "Z"
// end_time: same
// extra_data: any string
// _id: Long, to be used for extracting events from calendar

data class Event (val event_type: String, val description: String, val start_time: String, val end_time: String, val extra_data: String, val notes: String, val _id: Long? = null){
    var startInstant: Long? = null
    var endInstant: Long? = null
    var duration: Duration? = null
    init{
        if (start_time.isNotEmpty()) startInstant = Instant.parse(start_time + "Z").toEpochMilli()
        if (end_time.isNotEmpty()) endInstant = Instant.parse(end_time + "Z").toEpochMilli()
        if (startInstant != null && endInstant != null) duration = Duration.of(endInstant!! - startInstant!!, ChronoUnit.MILLIS)
    }
}
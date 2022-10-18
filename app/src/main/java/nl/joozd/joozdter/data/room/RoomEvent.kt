package nl.joozd.joozdter.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import nl.joozd.joozdcalendarapi.CalendarEvent
import nl.joozd.joozdter.data.events.Event

/**
 * Data class to keep track of which events are stored.
 *
 * An event is primarily stored in the devices calendar.
 * This class keeps track of what events are stored, so they can be retrieved and deleted/updated.
 * An event is considered the same if name, start and end times are the same,
 * so those are the things we store.
 * @param id: Unique ID for the database, not used otherwise
 * @param title: The name of this entry, also the text shown in the calendar for this event
 * @param startTime: Start time of this event (epoch millis)
 * @param endTime: End time of this event (epoch millis)
 */
@Entity
data class RoomEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val startTime: Long, // Instant.epochSecond; no Events without times in DB
    val endTime: Long,   // Instant.epochSecond; no Events without times in DB
){
    constructor(event: CalendarEvent): this(0, event.title, event.startEpochMillis, event.endEpochMillis)
    // When comparing with event, use roomEvent == event, not the other way around!!!
    override fun equals(other: Any?): Boolean = when(other) {
        is RoomEvent -> title == other.title
                && startTime == other.startTime
                && endTime == other.endTime
        is Event -> title == other.name
                && startTime/1000 == other.startEpochMilli
                && endTime/1000 == other.endEpochMilli
        else -> false
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + startTime.hashCode()
        result = 31 * result + endTime.hashCode()
        return result
    }
}
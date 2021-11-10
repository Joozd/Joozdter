package nl.joozd.joozdter.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import nl.joozd.joozdter.data.events.Event
import nl.joozd.joozdter.data.EventTypes
import java.time.Instant

/**
 * Data class to store an [nl.joozd.joozdter.data.Event]
 */
@Entity
data class RoomEvent(
    @PrimaryKey val id: Long,
                val name: String,
                val type: Int,       // EventType.value
                val startTime: Long?, // Instant.epochSecond; no Events without times in DB
                val endTime: Long?,   // Instant.epochSecond; no Events without times in DB
                val info: String,
                val notes: String)
{
    /**
     * convert this RoomEvent to a (subclass of) Event
     */
    fun toEvent(): Event = Event(
        name,
        EventTypes.of(type),
        startTime?.let { Instant.ofEpochSecond(it)},
        endTime?.let { Instant.ofEpochSecond(it)},
        info,
        notes
    ).withTypeInstance()
}
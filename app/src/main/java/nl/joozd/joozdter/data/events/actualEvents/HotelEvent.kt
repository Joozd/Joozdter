package nl.joozd.joozdter.data.events.actualEvents

import nl.joozd.joozdter.data.Day
import nl.joozd.joozdter.data.EventTypes
import nl.joozd.joozdter.data.events.CompleteableEvent
import nl.joozd.joozdter.data.events.Event
import nl.joozd.joozdter.data.events.EventConstructorData
import nl.joozd.joozdter.data.events.getLastTypedEvent
import nl.joozd.joozdter.utils.extensions.endInstant
import nl.joozd.joozdter.utils.extensions.startInstant
import java.time.Instant

/**
 * a HOTEL Event
 */
class HotelEvent(name: String,
                 startTime: Instant,
                 endTime: Instant,
                 info: String = "",
                 notes: String = "",
                 id: Long? = null
): Event(name, EventTypes.HOTEL, startTime, endTime, info, notes, id), CompleteableEvent {
    internal constructor(constructorData: EventConstructorData) :
            this(constructorData.name(), constructorData.dayStart(), constructorData.dayend(), constructorData.hotelInfo())

    override fun completeTimes(today: Day, nextDay: Day?): Event {
        val startTime: Instant = today.getLastTypedEvent(eventsBeforeHotel)?.endTime
                ?: today.date.startInstant()
        val endTime = nextDay?.startOfDay() ?: today.date.endInstant()
        return this.copy(startTime = startTime, endTime = endTime)
    }

    /**
     * Works like copy in a data class
     */
    override fun copy(name: String,
                      type: EventTypes,
                      startTime: Instant,
                      endTime: Instant,
                      info: String,
                      notes: String,
                      id: Long?
    ): HotelEvent {
        require (type == this.type) { "Cannot copy a typed Event to another type"}
        return HotelEvent(name, startTime, endTime, info, notes, id)
    }

    companion object{
        val eventsBeforeHotel = listOf(EventTypes.DUTY, EventTypes.CHECK_OUT)
    }
}
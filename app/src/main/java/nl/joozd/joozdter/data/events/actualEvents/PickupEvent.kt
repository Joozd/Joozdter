package nl.joozd.joozdter.data.events.actualEvents

import nl.joozd.joozdter.data.Day
import nl.joozd.joozdter.data.EventTypes
import nl.joozd.joozdter.data.events.CompleteableEvent
import nl.joozd.joozdter.data.events.Event
import nl.joozd.joozdter.data.events.EventConstructorData
import nl.joozd.joozdter.data.events.getFirstTypedEvent
import java.time.Instant

class PickupEvent(name: String,
                  startTime: Instant,
                  endTime: Instant,
                  info: String = "",
                  notes: String = "",
                  id: Long? = null
): Event(name, EventTypes.PICK_UP, startTime, endTime, info, notes, id), CompleteableEvent {
    internal constructor(d: EventConstructorData) :
            this(FIXED_NAME, d.pickupTimeStart(), d.pickupTimeStart().plusSeconds(30*60))

    override fun completeTimes(today: Day, nextDay: Day?): Event {
        val endTime: Instant = today.getFirstTypedEvent(eventsAfterPickup)?.startTime
            ?: this.startTime.plusSeconds(STANDARD_DURATION)
        return this.copy(endTime = endTime)
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
    ): PickupEvent {
        require (type == this.type) { "Cannot copy a typed Event to another type"}
        return PickupEvent(name, startTime, endTime, info, notes, id)
    }

    companion object{
        private val eventsAfterPickup = listOf(EventTypes.DUTY, EventTypes.CHECK_IN)

        private const val FIXED_NAME = "Pick Up"
        private const val STANDARD_DURATION = 60*30L // seconds (30 minutes)

    }
}
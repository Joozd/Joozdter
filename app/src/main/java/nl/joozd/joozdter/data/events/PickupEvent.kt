package nl.joozd.joozdter.data.events

import nl.joozd.joozdter.data.Day
import nl.joozd.joozdter.data.EventTypes
import java.time.Instant

class PickupEvent(name: String,
                  startTime: Instant,
                  endTime: Instant?,
                  info: String = "",
                  notes: String = ""
): Event(name, EventTypes.PICK_UP, startTime, endTime, info, notes), CompleteableEvent{
    internal constructor(d: EventConstructorData) :
            this(d.name(), d.pickupTimeStart(), null)

    override fun completeTimes(today: Day, nextDay: Day?): Event {
        val endTime: Instant = today.getFirstTypedEvent(eventsAfterPickup)?.startTime
            ?: this.startTime!!.plusSeconds(STANDARD_DURATION)
        return this.copy(endTime = endTime)
    }

    /**
     * Works like copy in a data class
     */
    override fun copy(name: String,
                      type: EventTypes,
                      startTime: Instant?,
                      endTime: Instant?,
                      info: String,
                      notes: String,
                      id: Long?
    ): PickupEvent{
        require (type == this.type) { "Cannot copy a typed Event to another type"}
        require (startTime != null) { "a PickupEvent must have a start time"}
        return PickupEvent(name, startTime, endTime, info, notes)
    }

    companion object{
        private val eventsAfterPickup = listOf(EventTypes.DUTY, EventTypes.CHECK_IN)
        private const val STANDARD_DURATION = 60*30L // seconds (30 minutes)
    }
}
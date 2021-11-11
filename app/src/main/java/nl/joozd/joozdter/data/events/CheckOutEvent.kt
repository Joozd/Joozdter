package nl.joozd.joozdter.data.events

import nl.joozd.joozdter.data.Day
import nl.joozd.joozdter.data.EventTypes
import java.time.Instant

class CheckOutEvent(name: String,
                    startTime: Instant?,
                    endTime: Instant,
                    info: String = "",
                    notes: String = "",
                    id: Long? = null
): Event(name, EventTypes.CHECK_OUT, startTime, endTime, info, notes, id), CompleteableEvent{
    internal constructor(d: EventConstructorData) :
            this(d.name(), null, d.checkOutTimeEnd())
    internal constructor(e: Event): this (e.name, e.startTime, e.endTime!!, e.info, e.notes, e.id)

    override fun completeTimes(today: Day, nextDay: Day?): Event {
        val startTime: Instant? = today.getLastEventBefore(this.endTime!!)?.endTime
            ?: this.endTime.minusSeconds(STANDARD_DURATION)
        //if no startTime for checkout found it will be 30 minutes
        return this.copy(startTime = startTime)
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
    ): CheckOutEvent{
        require (type == this.type) { "Cannot copy a typed Event to another type"}
        require (endTime != null) { "a CheckOutEvent must have an end time"}
        return CheckOutEvent(name, startTime, endTime, info, notes, id)
    }

    companion object{
        private const val STANDARD_DURATION = 30*60L // seconds (60 minutes)
    }
}
package nl.joozd.joozdter.data.events

import nl.joozd.joozdter.data.Day
import nl.joozd.joozdter.data.EventTypes
import java.time.Instant

class CheckinEvent(name: String,
                   startTime: Instant,
                   endTime: Instant?,
                   info: String = "",
                   notes: String = "",
                   id: Long? = null
): Event(name, EventTypes.CHECK_IN, startTime, endTime, info, notes, id), CompleteableEvent{
    internal constructor(d: EventConstructorData) :
            this(d.name(), d.checkInTimeStart(), null)
    internal constructor(e: Event): this (e.name, e.startTime!!, e.endTime, e.info, e.notes, e.id)

    override fun completeTimes(today: Day, nextDay: Day?): Event {
        //checkin ends at first event after checkin
        val endTime: Instant = today.getFirstEventAfter(this.startTime!!)?.startTime
                ?: this.startTime.plusSeconds(STANDARD_DURATION)

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
                      id: Long?): CheckinEvent{
        require (type == this.type) { "Cannot copy a typed Event to another type"}
        require (startTime != null) { "a CheckinEvent must have a start time"}
        return CheckinEvent(name, startTime, endTime, info, notes, id)
    }

    companion object{
        private const val STANDARD_DURATION = 60*60L // seconds (60 minutes)
    }
}
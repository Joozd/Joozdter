package nl.joozd.joozdter.data.events.actualEvents

import nl.joozd.joozdter.data.Day
import nl.joozd.joozdter.data.EventTypes
import nl.joozd.joozdter.data.events.*
import nl.joozd.joozdter.data.events.EventConstructorData
import nl.joozd.joozdter.data.events.getFirstEventAfter
import java.time.Instant

class CheckinEvent(name: String,
                   startTime: Instant,
                   endTime: Instant,
                   info: String = "",
                   notes: String = "",
                   id: Long? = null
): Event(name, EventTypes.CHECK_IN, startTime, endTime, info, notes, id), CompleteableEvent, MainEvent {
    internal constructor(d: EventConstructorData) :
            this(d.name(), d.checkInTimeStart(), d.checkInTimeStart().plusSeconds(30*60))

    override fun completeTimes(today: Day, nextDay: Day?): Event {
        //checkin ends at first event after checkin
        val endTime: Instant = today.getFirstEventAfter(this.startTime)?.startTime
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
                      id: Long?): CheckinEvent {
        require (type == this.type) { "Cannot copy a typed Event to another type"}
        return CheckinEvent(name, startTime, endTime, info, notes, id)
    }

    companion object{
        private const val STANDARD_DURATION = 60*60L // seconds (60 minutes)
    }
}
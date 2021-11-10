package nl.joozd.joozdter.data.events

import nl.joozd.joozdter.data.Day
import nl.joozd.joozdter.data.EventTypes
import java.time.Instant

class CheckinEvent(name: String,
                   startTime: Instant,
                   endTime: Instant?,
                   info: String = "",
                   notes: String = ""
): CompleteableEvent(name, EventTypes.CHECK_IN, startTime, endTime, info, notes){
    internal constructor(d: EventConstructorData) :
            this(d.name(), d.checkInTimeStart(), null)

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
                  notes: String
    ): CheckinEvent{
        require (type == this.type) { "Cannot copy a typed Event to another type"}
        require (startTime != null) { "a CheckinEvent must have a start time"}
        return CheckinEvent(name, startTime, endTime, info, notes)
    }

    companion object{
        private const val STANDARD_DURATION = 60*60L // seconds (60 minutes)
    }
}
package nl.joozd.joozdter.data.events

import nl.joozd.joozdter.data.Day
import nl.joozd.joozdter.data.EventTypes
import nl.joozd.joozdter.utils.extensions.replaceValue
import java.time.Instant

class CheckOutEvent(name: String,
                    startTime: Instant?,
                    endTime: Instant,
                    info: String = "",
                    notes: String = ""
): CompleteableEvent(name, EventTypes.CHECK_OUT, startTime, endTime, info, notes){
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
                      notes: String
    ): CheckOutEvent{
        require (type == this.type) { "Cannot copy a typed Event to another type"}
        require (endTime != null) { "a CheckOutEvent must have an end time"}
        return CheckOutEvent(name, startTime, endTime, info, notes)
    }

    companion object{
        private const val STANDARD_DURATION = 30*60L // seconds (60 minutes)
    }
}
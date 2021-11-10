package nl.joozd.joozdter.data.events

import nl.joozd.joozdter.data.Day
import nl.joozd.joozdter.data.EventTypes
import java.time.Instant

/**
 * Events that need their time completed after initial parsing must implement this.
 */
abstract class CompleteableEvent(name: String,
                                 type: EventTypes,
                                 startTime: Instant?,
                                 endTime: Instant?,
                                 info: String = "",
                                 notes: String = ""
): Event(name, type, startTime, endTime, info, notes){
    abstract fun completeTimes(today: Day, nextDay: Day?): Event
}
package nl.joozd.joozdter.data.events

import nl.joozd.joozdter.data.Day
import nl.joozd.joozdter.data.EventTypes
import java.time.Instant

/**
 * Events that need their time completed after initial parsing must implement this.
 */
interface CompleteableEvent{
    abstract fun completeTimes(today: Day, nextDay: Day?): Event
}
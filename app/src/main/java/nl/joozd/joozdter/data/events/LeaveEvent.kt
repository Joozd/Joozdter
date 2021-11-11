package nl.joozd.joozdter.data.events

import nl.joozd.joozdter.data.EventTypes
import java.time.Instant

class LeaveEvent(name: String,
                 startTime: Instant?,
                 endTime: Instant?,
                 info: String = "",
                 notes: String = "",
                 id: Long? = null
): Event(name, EventTypes.LEAVE, startTime, endTime, info, notes, id), AllDayevent{
    internal constructor(d: EventConstructorData):
            this(d.name(), d.dayStart(), d.dayend())
    internal constructor(e: Event): this (e.name, e.startTime, e.endTime, e.info, e.notes, e.id)
}
package nl.joozd.joozdter.data.events

import nl.joozd.joozdter.data.EventTypes
import java.time.Instant

class DutyEvent(name: String,
                startTime: Instant?,
                endTime: Instant?,
                info: String = "",
                notes: String = "",
                id: Long? = null
): Event(name, EventTypes.DUTY, startTime, endTime, info, notes, id), MainEvent {
    //Not applicable, Duty is never constructed using EventConstructorData
    // internal constructor(d: EventConstructorData)

    internal constructor(e: Event) : this(e.name, e.startTime, e.endTime, e.info, e.notes, e.id)
}
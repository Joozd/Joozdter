package nl.joozd.joozdter.data.events.actualEvents

import nl.joozd.joozdter.data.EventTypes
import nl.joozd.joozdter.data.events.Event
import nl.joozd.joozdter.data.events.EventConstructorData
import java.time.Instant

class ClickEvent(name: String,
                 startTime: Instant?,
                 endTime: Instant?,
                 info: String = "",
                 notes: String = "",
                 id: Long? = null
): Event(name, EventTypes.CLICK, startTime, endTime, info, notes, id){
    internal constructor(d: EventConstructorData):
            this(d.name(), d.clickTimeStart(), d.clickTimeEnd())
    internal constructor(e: Event): this (e.name, e.startTime, e.endTime, e.info, e.notes, e.id)
}
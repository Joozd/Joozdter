package nl.joozd.joozdter.data.events

import nl.joozd.joozdter.data.EventTypes
import java.time.Instant

class ClickEvent(name: String,
                 startTime: Instant?,
                 endTime: Instant?,
                 info: String = "",
                 notes: String = ""
): Event(name, EventTypes.CLICK, startTime, endTime, info, notes){
    internal constructor(d: EventConstructorData):
            this(d.name(), d.clickTimeStart(), d.clickTimeEnd())
}
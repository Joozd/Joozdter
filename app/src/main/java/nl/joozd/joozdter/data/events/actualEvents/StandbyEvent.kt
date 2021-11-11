package nl.joozd.joozdter.data.events.actualEvents

import nl.joozd.joozdter.data.EventTypes
import nl.joozd.joozdter.data.events.Event
import nl.joozd.joozdter.data.events.EventConstructorData
import nl.joozd.joozdter.data.events.MainEvent
import java.time.Instant

class StandbyEvent(name: String,
                   startTime: Instant?,
                   endTime: Instant?,
                   info: String = "",
                   notes: String = "",
                   id: Long? = null
): Event(name, EventTypes.STANDBY, startTime, endTime, info, notes, id), MainEvent{
    internal constructor(d: EventConstructorData):
            this(d.name(), d.standbyTimeStart(), d.standbyTimeEnd())
    internal constructor(e: Event): this (e.name, e.startTime, e.endTime, e.info, e.notes, e.id)
}
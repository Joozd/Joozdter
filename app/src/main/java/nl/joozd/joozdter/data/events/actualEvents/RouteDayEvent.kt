package nl.joozd.joozdter.data.events.actualEvents

import nl.joozd.joozdter.data.EventTypes
import nl.joozd.joozdter.data.events.AllDayevent
import nl.joozd.joozdter.data.events.Event
import nl.joozd.joozdter.data.events.EventConstructorData
import nl.joozd.joozdter.data.events.MainEvent
import java.time.Instant

class RouteDayEvent(name: String,
                    startTime: Instant,
                    endTime: Instant,
                    info: String = "",
                    notes: String = "",
                    id: Long? = null
): Event(name, EventTypes.ROUTE_DAY, startTime, endTime, info, notes, id), AllDayevent, MainEvent {
    internal constructor(d: EventConstructorData):
            this(d.name(), d.dayStart(), d.dayend())
}
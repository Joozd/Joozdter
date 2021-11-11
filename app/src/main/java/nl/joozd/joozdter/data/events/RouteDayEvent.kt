package nl.joozd.joozdter.data.events

import nl.joozd.joozdter.data.EventTypes
import java.time.Instant

class RouteDayEvent(name: String,
                    startTime: Instant?,
                    endTime: Instant?,
                    info: String = "",
                    notes: String = ""
): Event(name, EventTypes.ROUTE_DAY, startTime, endTime, info, notes), AllDayevent{
    internal constructor(d: EventConstructorData):
            this(d.name(), d.dayStart(), d.dayend())
}
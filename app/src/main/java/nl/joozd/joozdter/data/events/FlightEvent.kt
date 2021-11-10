package nl.joozd.joozdter.data.events

import nl.joozd.joozdter.data.EventTypes
import java.time.Instant

class FlightEvent(name: String,
                  startTime: Instant?,
                  endTime: Instant?,
                  info: String = "",
                  notes: String = ""
): Event(name, EventTypes.FLIGHT, startTime, endTime, info, notes){
    internal constructor(d: EventConstructorData):
            this(d.flightName(), d.flightTimeStart(), d.flightTimeEnd(), info = d.flightInfo())
}
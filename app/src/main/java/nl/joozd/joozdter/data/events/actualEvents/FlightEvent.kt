package nl.joozd.joozdter.data.events.actualEvents

import nl.joozd.joozdter.data.EventTypes
import nl.joozd.joozdter.data.events.Event
import nl.joozd.joozdter.data.events.EventConstructorData
import java.time.Instant

class FlightEvent(name: String,
                  startTime: Instant,
                  endTime: Instant,
                  info: String = "",
                  notes: String = "",
                  id: Long? = null
): Event(name, EventTypes.FLIGHT, startTime, endTime, info, notes, id){
    internal constructor(d: EventConstructorData):
            this(d.flightName(), d.flightTimeStart(), d.flightTimeEnd(), info = d.flightInfo())
}
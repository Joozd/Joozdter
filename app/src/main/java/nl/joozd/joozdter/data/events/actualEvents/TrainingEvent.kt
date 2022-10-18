package nl.joozd.joozdter.data.events.actualEvents

import nl.joozd.joozdter.data.EventTypes
import nl.joozd.joozdter.data.events.Event
import nl.joozd.joozdter.data.events.EventConstructorData
import nl.joozd.joozdter.data.events.MainEvent
import java.time.Instant

class TrainingEvent(name: String,
                    startTime: Instant,
                    endTime: Instant,
                    info: String = "",
                    notes: String = "",
                    id: Long? = null
): Event(name, EventTypes.TRAINING, startTime, endTime, info, notes, id), MainEvent{
    internal constructor(d: EventConstructorData):
            this(d.name(), d.trainingTimeStart(), d.trainingTimeEnd())
}
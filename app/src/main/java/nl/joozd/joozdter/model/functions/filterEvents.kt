package nl.joozd.joozdter.model

import nl.joozd.joozdter.data.EventTypes
import nl.joozd.joozdter.data.sharedPrefs.JoozdterPrefs

/**
 * Filter [events] by preferences set in JoozdterPrefs
 */
//TODO do this in a non-blocking way or suspended or something. Probably will be rewritten anyway.
suspend fun filterEvents(events: List<nl.joozd.joozdter.data.events.Event>): List<nl.joozd.joozdter.data.events.Event>{
    val allowedEvents = buildList {
        if (JoozdterPrefs.leave()) add(EventTypes.LEAVE)
        if (JoozdterPrefs.taxi()) add(EventTypes.PICK_UP)
        if (JoozdterPrefs.standby()) add(EventTypes.STANDBY)
        if (JoozdterPrefs.training()) add(EventTypes.TRAINING)
        if (JoozdterPrefs.simulator()) add(EventTypes.SIMULATOR_DUTY)
        if (JoozdterPrefs.otherDuty()) add(EventTypes.UNKNOWN_EVENT)
        if (JoozdterPrefs.checkIn()) add(EventTypes.CHECK_IN)
        if (JoozdterPrefs.checkOut()) add(EventTypes.CHECK_OUT)
        if (JoozdterPrefs.flight()) add(EventTypes.FLIGHT)
        if (JoozdterPrefs.hotel()) add(EventTypes.HOTEL)
    }

    return events.filter { it.type in allowedEvents }
}
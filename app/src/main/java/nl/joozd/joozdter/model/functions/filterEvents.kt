package nl.joozd.joozdter.model

import nl.joozd.joozdter.data.JoozdterPrefs
import nl.joozd.klcrosterparser.Activities
import nl.joozd.klcrosterparser.KlcRosterEvent

/**
 * Filter [events] by preferences set in JoozdterPrefs
 */
fun filterEvents(events: List<Event>): List<Event> = Array(10) { index ->
    when (index) {
        0 -> if (JoozdterPrefs.leave) events.filter { it.eventType == Activities.LEAVE } else emptyList()
        1 -> if (JoozdterPrefs.taxi) events.filter { it.eventType == Activities.TAXI } else emptyList()
        2 -> if (JoozdterPrefs.standby) events.filter { it.eventType == Activities.STANDBY } else emptyList()
        3 -> if (JoozdterPrefs.simulator) events.filter { it.eventType == Activities.SIM } else emptyList()
        4 -> if (JoozdterPrefs.actualSimulator) events.filter { it.eventType == Activities.ACTUALSIM } else emptyList()
        5 -> if (JoozdterPrefs.other) events.filter { it.eventType == Activities.OTHER_DUTY } else emptyList()
        6 -> if (JoozdterPrefs.checkIn) events.filter { it.eventType == Activities.CHECKIN } else emptyList()
        7 -> if (JoozdterPrefs.checkOut) events.filter { it.eventType == Activities.CHECKOUT } else emptyList()
        8 -> if (JoozdterPrefs.flight) events.filter { it.eventType == Activities.FLIGHT } else emptyList()
        9 -> if (JoozdterPrefs.hotel) events.filter { it.eventType == Activities.HOTEL } else emptyList()
        else -> error ("Index $index out of bounds (not in 0-9)") // should not happen but won't compile otherwise
    }
}.toList().flatten().sortedBy{it.startTime}
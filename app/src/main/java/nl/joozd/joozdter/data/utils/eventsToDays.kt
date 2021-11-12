package nl.joozd.joozdter.data.utils

import nl.joozd.joozdter.data.Day
import nl.joozd.joozdter.data.events.Event
import nl.joozd.joozdter.data.helpers.EventsMap

/**
 * Puts a list of [Event] into Days
 */
fun eventsToDays(events: Collection<Event>): List<Day>{
    val eventsMap = EventsMap(events)
    return eventsMap.days()
}
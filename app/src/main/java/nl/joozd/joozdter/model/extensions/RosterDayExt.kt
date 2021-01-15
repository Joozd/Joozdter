package nl.joozd.joozdter.model.extensions

import nl.joozd.joozdter.utils.fdpChecker
import nl.joozd.klcrosterparser.Activities
import nl.joozd.klcrosterparser.RosterDay

fun RosterDay.addNotesToCorrectEvent(): RosterDay {
    val newEvents = checkinEvent?.let { ci ->
        events.filter { it != checkinEvent } + listOf(ci.copy(extraMessage = extraInfo))
    } ?: events.firstOrNull()?.let {
            listOf(it.copy(extraMessage = extraInfo)) + events.drop(1)
        } ?: emptyList()
    return this.copy(events = newEvents)
}
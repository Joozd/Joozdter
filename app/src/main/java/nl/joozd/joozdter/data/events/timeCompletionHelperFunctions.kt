package nl.joozd.joozdter.data.events

import nl.joozd.joozdter.data.EventTypes

/**
 * This will return all events that implement [Event.CompleteableEvent] interface
 */
internal fun Collection<Event>.getEventsThatNeedCompleting(): Collection<CompleteableEvent> =
    this.filter{ it is CompleteableEvent }.map{
        it as CompleteableEvent
    }
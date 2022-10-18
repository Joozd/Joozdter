package nl.joozd.joozdter.data.events

import nl.joozd.joozdter.data.Day
import nl.joozd.joozdter.data.EventTypes
import java.time.Instant

/**
 * helper functions for Events handling, parsing, etc.
 */

/**
 * Get first event of the day
 */
internal fun Day.getFirstEvent(): Event? =
    this.events.getFirstEvent()

/**
 * Get first event of the day after a specific Instant
 * Null if none found.
 */
internal fun Day.getFirstEventAfter(after: Instant) =
    this.events.getFirstEventAfter(after)

/**
 * Get last event of the day before a specific Instant
 * Null if none found.
 */
internal fun Day.getLastEventBefore(before: Instant) =
    this.events.getLastEventBefore(before)

/**
 * Get first event of a specific type.
 * If [after] is given, it will give the first after that.
 */
internal fun Day.getFirstTypedEvent(allowedTypes: Collection<EventTypes>,
                                                  after: Instant? = null): Event? =
    this.events.getFirstTypedEvent(allowedTypes, after)

/**
 * Get first event of a specific type.
 * If [before] is given, it will give the last before that.
 */
internal fun Day.getLastTypedEvent(allowedTypes: Collection<EventTypes>,
                                    before: Instant? = null): Event? =
    this.events.getLastTypedEvent(allowedTypes, before)

/**
 * Get first event (only startTime considered) from a Collection of Events
 * Null if no event found.
 */
internal fun Collection<Event>.getFirstEvent(): Event? =
    this.getFirstEventFromEventsWithTimes()

/**
 * Get last event (only endTime considered) from a Collection of Events
 * Null if no event found.
 */
internal fun Collection<Event>.getLastEvent(): Event? =
    this.getLastEventFromEventsWithTimes()

/**
 * Get first event after a specific Instant
 * Null if none found.
 */
internal fun Collection<Event>.getFirstEventAfter(after: Instant): Event? =
    this.filter{ it.startTime > after  }
        .getFirstEventFromEventsWithTimes()

/**
 * Get last event before a specific Instant
 * Null if none found.
 */
internal fun Collection<Event>.getLastEventBefore(before: Instant): Event? =
    this.filter{ it.endTime < before  }
        .getLastEventFromEventsWithTimes()

/**
 * Get first event of a specific type.
 * If [after] is given, it will give the first after that.
 */
internal fun Collection<Event>.getFirstTypedEvent(allowedTypes: Collection<EventTypes>,
                                                  after: Instant? = null): Event? {
    val relevantEvents = this.filter {it.type in allowedTypes }
    return if (after == null) relevantEvents.getFirstEvent()
           else relevantEvents.getFirstEventAfter(after)
}

/**
 * Get last event of a specific type.
 * If [before] is given, it will give the first after that.
 */
internal fun Collection<Event>.getLastTypedEvent(allowedTypes: Collection<EventTypes>,
                                                  before: Instant? = null): Event? {
    val relevantEvents = this.filter {it.type in allowedTypes }
    return if (before == null) relevantEvents.getLastEvent()
    else relevantEvents.getLastEventBefore(before)
}



private fun Collection<Event>.getFirstEventFromEventsWithTimes(): Event? =
    this.minByOrNull { it.startTime }

private fun Collection<Event>.getLastEventFromEventsWithTimes(): Event? =
    this.maxByOrNull { it.endTime }
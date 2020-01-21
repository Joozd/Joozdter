package nl.joozd.klcrosterparser

import org.threeten.bp.Instant

/**
 * Holds an Event as published on the roster, eg. a flight, sim session, Check-in, Hotel, etc.
 * @param type: Type of event, will probably make an object with fixed events to prevent typos
 * @param start: Start time as Instant
 * @param end: End time as Instand
 * @param description: Description of Event eg. KL1425 AMS-BLQ or Hotel FRA
 * @param extraMessage: Extra message on roster, eg. "To c/m: Three-yearly recurrent"
 */

data class KlcRosterEvent(val type: String, val start: Instant, val end: Instant, val description: String, val extraMessage: String? = null)

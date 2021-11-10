package nl.joozd.joozdter.utils.extensions

import java.time.*


/**
 * The end of this day (midnight the following day)
 */
fun LocalDate.atEndOfDay(zone: ZoneId): ZonedDateTime = this.plusDays(1).atStartOfDay(zone)

fun LocalDate.atEndOfDay(): LocalDateTime = this.plusDays(1).atStartOfDay()

/**
 * Get the Instant at which this day starts
 */
fun LocalDate.startInstant(zoneOffset: ZoneOffset = ZoneOffset.UTC): Instant =
    this.atStartOfDay(zoneOffset).toInstant()

/**
 * Get the epochSecond at which this day starts
 */
fun LocalDate.startEpochSecond(zoneOffset: ZoneOffset = ZoneOffset.UTC): Long =
    this.startInstant(zoneOffset).epochSecond

/**
 * Get the Instant at which this day starts
 */
fun LocalDate.endInstant(zoneOffset: ZoneOffset = ZoneOffset.UTC): Instant =
    this.atEndOfDay(zoneOffset).toInstant()

/**
 * Get the epochSecond at which this day ends
 */
fun LocalDate.endEpochSecond(zoneOffset: ZoneOffset = ZoneOffset.UTC) =
    this.endInstant(zoneOffset).epochSecond
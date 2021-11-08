package nl.joozd.joozdter.utils.extensions

import java.time.*


/**
 * The end of this day (midnight the following day)
 */
fun LocalDate.atEndOfDay(zone: ZoneId): ZonedDateTime = this.plusDays(1).atStartOfDay(zone)

fun LocalDate.atEndOfDay(): LocalDateTime = this.plusDays(1).atStartOfDay()

fun Instant.toLocalDate(zoneOffset: ZoneOffset = ZoneOffset.UTC) = LocalDateTime.ofInstant(this, zoneOffset).toLocalDate()
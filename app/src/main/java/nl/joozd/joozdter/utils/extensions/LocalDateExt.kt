package nl.joozd.joozdter.utils.extensions

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime


/**
 * The end of this day (midnight the following day)
 */
fun LocalDate.atEndOfDay(zone: ZoneId): ZonedDateTime = this.plusDays(1).atStartOfDay(zone)

fun LocalDate.atEndOfDay(): LocalDateTime = this.plusDays(1).atStartOfDay()
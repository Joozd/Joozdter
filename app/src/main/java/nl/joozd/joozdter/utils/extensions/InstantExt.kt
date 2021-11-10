package nl.joozd.joozdter.utils.extensions

import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

operator fun Instant.minus(other: Instant): Duration = Duration.between(other, this)

fun Instant.toLocalDate(zoneOffset: ZoneOffset = ZoneOffset.UTC) =
    LocalDateTime.ofInstant(this, zoneOffset).toLocalDate()
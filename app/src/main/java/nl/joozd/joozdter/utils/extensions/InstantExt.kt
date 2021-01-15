package nl.joozd.joozdter.utils.extensions

import java.time.Duration
import java.time.Instant

operator fun Instant.minus(other: Instant): Duration = Duration.between(other, this)
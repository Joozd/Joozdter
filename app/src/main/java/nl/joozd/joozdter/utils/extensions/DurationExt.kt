package nl.joozd.joozdter.utils.extensions

import java.time.Duration

operator fun Duration.times(other: Int): Duration = Duration.ofMinutes(other * toMinutes())
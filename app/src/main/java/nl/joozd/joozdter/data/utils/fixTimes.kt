package nl.joozd.joozdter.data.utils

import nl.joozd.joozdter.data.Day

/**
 * Updates times from a complete set of days
 * eg. hotel times, etc
 */
fun Collection<Day>.fixTimes(): List<Day> =
    this.map{
        it.completeTimes(this)
    }

package nl.joozd.joozdter.data

import nl.joozd.joozdter.data.extensions.words
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * An event is one thing that happens, eg. pickup, 1 flight, a hotel stay or a checkin.
 * @param name: Name of the event (eg. Flight AMS-BKK, Simulator Duty or Hotel GRU)
 * @param type: Type of event (see [EventTypes])
 * @param startTime: Time at which the event starts. Can be null if this event doesn't have a start time (eg. checkOut)
 * @param endTime: Time at which the event ends Can be null if this event doesn't have aen end time (eg. checkIn)
 * @param info: extra info, such as aircraft type, rest margin, hotel name, etc
 * @param notes: Notes, such as crew names, FDP info, etc
 */
open class Event(val name: String, val type: EventTypes, val startTime: Instant?, val endTime: Instant?, val info: String = "", val notes: List<String> = emptyList()) {
    override fun toString(): String = "name: $name\ntype: $type\nstart: $startTime\nend: $endTime\ninfo: $info\nnotes:$notes"

    /**
     * Works like copy in a data class
     */
    open fun copy(name: String = this.name,
                  type: EventTypes = this.type,
                  startTime: Instant? = this.startTime,
                  endTime: Instant? = this.endTime,
                  info: String = this.info,
                  notes: List<String> = this.notes
    ) = Event(name, type, startTime, endTime, info, notes)

    companion object{
        private const val TIME = """\d{4}"""
        private const val ONE_DAY = 86400L

        fun parse(input: String, date: LocalDate): Event?{
            require ('\n' !in input.trim()) { "Only parse single lines into a single event!" }

            val name = input.words().first()
            return when {
                input matches leaveRegex -> {
                    val results = leaveRegex.find(input)!!.groupValues
                    val timeStart = results[1].timeStringToInstant(date)
                    val timeEnd = results[2].timeStringToInstant(date).let{
                        if (it > timeStart) it else it.plusSeconds(ONE_DAY)
                    }
                    Event(input.words().first(), EventTypes.LEAVE, timeStart, timeEnd)
                }

                input matches checkInRegex -> {
                    val results = checkInRegex.find(input)!!.groupValues
                    val timeStart = results[1].timeStringToInstant(date)
                    Event(input.words().first(), EventTypes.CHECK_IN, timeStart, null)
                }

                input matches checkOutRegex -> {
                    val results = checkOutRegex.find(input)!!.groupValues
                    val timeEnd = results[1].timeStringToInstant(date)
                    Event(input.words().first(), EventTypes.CHECK_OUT, null, timeEnd)
                }

                input matches trainingRegex -> {
                    val words = input.words()
                    val timeStart: Instant? = words.getOrNull(2)?.timeStringToInstant(date)
                    val timeEnd: Instant? = words.getOrNull(3)?.timeStringToInstant(date)
                    Event(words.first(), EventTypes.TRAINING, timeStart, timeEnd) // training events are always main events, times are filled in by [Day]
                }

                input matches standbyRegex -> {
                    val results = standbyRegex.find(input)!!.groupValues
                    val timeStart = results[1].timeStringToInstant(date)
                    val timeEnd = results[2].timeStringToInstant(date).let{
                        if (it > timeStart) it else it.plusSeconds(ONE_DAY)
                    }
                    Event(input.words().first(), EventTypes.STANDBY, timeStart, timeEnd)
                }
                else -> null.also { println("No event found in $input")}
            }


        }

        private fun String.timeStringToInstant(date: LocalDate): Instant = LocalTime.parse(this, DateTimeFormatter.ofPattern("HHmm")).atDate(date).toInstant(ZoneOffset.UTC)

        private val leaveRegex = """(?:LV[A-Z]+|SL[A-Z]+)\s[A-Z]{3}\s($TIME)\s($TIME)""".toRegex()
        private val checkInRegex = """(?:C/I|S/U)\s(?:[A-Z]{3})\s(\d{4})""".toRegex()
        private val checkOutRegex = """C/O\s(\d{4})\s(?:[A-Z]{3}).*""".toRegex()
        private val trainingRegex = """T[A-Z]+.*""".toRegex()
        private val standbyRegex = """(?:RE[A-Z0-9]+|WTV)\s[A-Z]{3}\s($TIME)\s($TIME).*""".toRegex()


    }
}
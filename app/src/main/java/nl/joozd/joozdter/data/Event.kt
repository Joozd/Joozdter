package nl.joozd.joozdter.data

import nl.joozd.joozdter.data.extensions.words
import nl.joozd.joozdter.data.room.RoomEvent
import nl.joozd.joozdter.utils.extensions.atEndOfDay
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
 * @param notes: Notes, such as crew names, FDP info, etc.
 */
open class Event(val name: String, val type: EventTypes, val startTime: Instant?, val endTime: Instant?, val info: String = "", val notes: String = "") {
    override fun toString(): String = "name: $name\ntype: $type\nstart: $startTime\nend: $endTime\ninfo: $info\nnotes:$notes"

    /**
     * Works like copy in a data class
     */
    open fun copy(name: String = this.name,
                  type: EventTypes = this.type,
                  startTime: Instant? = this.startTime,
                  endTime: Instant? = this.endTime,
                  info: String = this.info,
                  notes: String = this.notes
    ) = Event(name, type, startTime, endTime, info, notes)

    /**
     * [startTime] and [endTime] may not be null, will return null otherwise
     */
    fun toRoomEvent(): RoomEvent? = if (startTime == null || endTime == null) null
    else RoomEvent(-1, name, type.value, startTime.epochSecond, endTime.epochSecond, info, notes)

    override fun equals(other: Any?): Boolean {
        if (other !is Event) return false
        return name == other.name
                && type == other.type
                && startTime == other.startTime
                && endTime == other.endTime
                && info == other.info
                && notes == other.notes
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + (startTime?.hashCode() ?: 0)
        result = 31 * result + (endTime?.hashCode() ?: 0)
        result = 31 * result + info.hashCode()
        result = 31 * result + notes.hashCode()
        return result
    }

    companion object{
        private const val TIME = """\d{4}"""
        private const val ONE_DAY = 86400L

        fun parse(input: String, date: LocalDate, legend: Map<String, String> = emptyMap()): Event?{
            require ('\n' !in input.trim()) { "Only parse single lines into a single event!" }

            val name = input.words().first().let{ n ->
                legend[n]?.let { ln -> "$n ($ln)"} ?: n
            }
            println("$date - INPUT IS $input")
            return when {
                input matches leaveRegex -> {
                    val timeStart = date.atStartOfDay(ZoneOffset.UTC).toInstant() // leave is an aLL DAY event, and must start and end at midnight UTC
                    val timeEnd = date.atEndOfDay(ZoneOffset.UTC).toInstant() // leave is an aLL DAY event, and must start and end at midnight UTC
                    Event(name, EventTypes.LEAVE, timeStart, timeEnd)
                }

                input matches checkInRegex -> {
                    val results = checkInRegex.find(input)!!.groupValues
                    val timeStart = results[1].timeStringToInstant(date)
                    Event(name, EventTypes.CHECK_IN, timeStart, null)
                }

                input matches checkOutRegex -> {
                    val results = checkOutRegex.find(input)!!.groupValues
                    val timeEnd = results[1].timeStringToInstant(date)
                    Event(name, EventTypes.CHECK_OUT, null, timeEnd)
                }

                input matches trainingRegex -> {
                    val words = input.words()
                    val timeStart: Instant? = words.getOrNull(2)?.timeStringToInstant(date)
                    val timeEnd: Instant? = words.getOrNull(3)?.timeStringToInstant(date)
                    Event(name, EventTypes.TRAINING, timeStart, timeEnd) // training events are always main events, times are filled in by [Day]
                }

                input matches standbyRegex -> {
                    val results = standbyRegex.find(input)!!.groupValues
                    val timeStart = results[1].timeStringToInstant(date)
                    val timeEnd = results[2].timeStringToInstant(date).let{
                        if (it > timeStart) it else it.plusSeconds(ONE_DAY)
                    }
                    Event(name, EventTypes.STANDBY, timeStart, timeEnd)
                }

                input matches flightRegex -> {
                    // KL 1929 AMS 1010 1135 GVA E75 89010 RI
                    // will become KL1929 AMS - GVA with times and info "E75 89010 RI"
                    val results = flightRegex.find(input)!!.groupValues
                    val flightName = results[1].filter{it != ' '} + " ${results[2]} - ${results[5]}"
                    val timeStart = results[3].timeStringToInstant(date)
                    val timeEnd = results[4].timeStringToInstant(date)
                    Event(flightName, EventTypes.FLIGHT, timeStart, timeEnd, info = results[6].trim())
                }

                input matches clickRegex -> {
                    val results = clickRegex.find(input)!!.groupValues
                    val timeStart = results[1].timeStringToInstant(date)
                    val timeEnd = results[2].timeStringToInstant(date)
                    Event(name, EventTypes.CLICK, timeStart, timeEnd)
                }

                // a hotel event doesn't have a start- or endtime when first parsed. This should be taken care of by calling Day parser.
                // A hotel event WILL have a start and end when saved to database
                input matches hotelRegex -> {
                    val results = hotelRegex.find(input)!!.groupValues
                    val hotelKey = results[1]
                    Event(results[0], EventTypes.HOTEL, null, null, info = legend[hotelKey] ?: "---")
                }

                input matches pickupRegex -> {
                    val timeStart = pickupRegex.find(input)!!.groupValues[1].timeStringToInstant(date)
                    Event(name, EventTypes.PICK_UP, timeStart, null)
                }

                input matches dayOverRegex -> {
                    Event(name, EventTypes.ROUTE_DAY, date.atTime(12,0).toInstant(ZoneOffset.UTC), null)
                }

                else -> null.also { println("No event found in $input")}
            }


        }

        private fun String.timeStringToInstant(date: LocalDate): Instant = LocalTime.parse(this, DateTimeFormatter.ofPattern("HHmm")).atDate(date).toInstant(ZoneOffset.UTC)

        private val leaveRegex = """(?:L[A-Z]+|SL[A-Z]+)\s(?:R\s)?[A-Z]{3}\s($TIME)\s($TIME)""".toRegex()
        private val checkInRegex = """(?:C/I|S/U)\s(?:[A-Z]{3})\s(\d{4})""".toRegex()
        private val checkOutRegex = """C/O\s(\d{4})\s(?:[A-Z]{3}).*""".toRegex()
        private val trainingRegex = """T[A-Z]+.*""".toRegex()
        private val standbyRegex = """(?:RE[A-Z0-9]+|WTV)\s[A-Z]{3}\s($TIME)\s($TIME).*""".toRegex()
        private val dayOverRegex = """X""".toRegex()

        //results: [0] = whole line, [1] = flightnumber, [2] = orig, [3] = tOut, [4] = tIn, [5] = dest, [6] = extra info
        private val flightRegex = """((?:KL|WA)\s?\d{2,5})\s([A-Z]{3})\s($TIME)\s($TIME)\s([A-Z]{3})(.*)""".toRegex()

        private val hotelRegex = """(H\d+)\s([A-Z]{3})""".toRegex()
        private val clickRegex = """CLICK\s[A-Z]{3}\s($TIME)\s($TIME)""".toRegex()
        private val pickupRegex = """Pick Up ($TIME)""".toRegex()

    }
}
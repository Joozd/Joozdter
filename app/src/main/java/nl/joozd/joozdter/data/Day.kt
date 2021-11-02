package nl.joozd.joozdter.data

import nl.joozd.joozdter.data.extensions.words
import nl.joozd.joozdter.utils.InstantRange
import nl.joozd.joozdter.utils.extensions.atEndOfDay
import java.time.*
import java.time.format.DateTimeFormatter

/**
 * A Day holds all events for that day
 * It also has a static function [Day.of] that does a lot of work generating all those events from a string.
 * @param date: Date of this [Day]
 * @param events: All events of this day
 * @param legend: Legend for the used abbreviations in this day (not all have to be used, its the legend for the entire roster)
 */
class Day(val date: LocalDate, val events: List<Event>, val legend: Map<String, String>?, val fdp: Duration? = null){
    override fun toString(): String = "Day: $date\nEvents:\n${events.joinToString("\n")}"
    companion object{
        /**
         * Parse a Day from a string.
         * @param dayString: String with day, type of day, time start and time end.
         *  eg. Mon25
         *      Sby
         *      0700
         *      1900
         * @param dayContentsString: String that has all the data for this day, as found in roster.
         * This will always start with the "day" (eg. Mon03) on its own line.
         * @param legend: A legend for abbreviations that might be found in this day
         */
        fun of(dayString: String, dayContentsString: String, legend: Map<String, String>? = null, period: InstantRange): Day?{
            var foundEvents = ArrayList<Event>()

            val dayLines = dayString.lines()

            require(dayContentsString.startsWith(dayLines.first())) { "Day in $dayLines does not match day in $dayContentsString" }
            when (dayLines.size) {
                1 -> return null // this is a date without any roster info (can happen at start of employment)
                2 -> { /*this is a route day, will lead to a day with only a "day over" event*/
                    val date = getDate(dayLines.first(), period) ?: return null
                    val event = Event(dayLines[1], EventTypes.ROUTE_DAY, date.atStartOfDay(ZoneOffset.UTC).toInstant(), date.atEndOfDay(ZoneOffset.UTC).toInstant())
                    return Day(date, emptyList(), emptyMap())
                }
                4 -> { /*this is a normal day*/ }
                else -> error ("Day.of(): Malformed [dayLines] does not have 1, 2 or 4 lines: $dayLines")


            }
            //require(dayLines.size == 4) { "Malformed dayLines: $dayLines, must be 4 lines (day, type, start, end)"}

            // extraMessage should be added as [Event.info] to the day's event that matches the times in [dayString]
            val extraMessage = extraMessageRegex.find(dayContentsString)?.groupValues?.get(1)

            // split [dayContentsString] into lines, and remove extra info and dayString line
            var lines = (extraMessage?.let{
                dayContentsString.replace(it, "").lines()
            } ?: dayContentsString.lines())
                .drop(1)
                .filter {it.isNotBlank()}

            val date = dayLines.firstOrNull()?.let { getDate(it, period) } ?: return null
            val dayStart = LocalTime.parse(dayLines[2], timeFormatter).atDate(date).toInstant(ZoneOffset.UTC)
            val dayEnd = LocalTime.parse(dayLines[3], timeFormatter).atDate(date).toInstant(ZoneOffset.UTC)

            //pop mainEventLine out of [lines], parse it and add it to [foundEvents]
            lines.firstOrNull{ it matches mainEventRegex}?.also{
                lines = lines.filter { l -> l != it }
            }?.let{ l ->
                Event.parse(l, date)?.let{
                    it.copy(startTime = it.startTime ?: dayStart, endTime = it.endTime ?: dayEnd, info = extraMessage ?: "")?.let { e -> foundEvents.add(e) }
                }
            }
            //println(lines.joinToString("\n", postfix = "\n***") { ".$it"})

            foundEvents.addAll(lines.mapNotNull { Event.parse(it, date) })

            // Add texts like TLC or TSOD to the next event if it is not the main activity (these have null times)
            foundEvents.filter { it.startTime == null && it.endTime == null}.forEach{
                eventThatIsANote ->
                val index = foundEvents.indexOf(eventThatIsANote)
                foundEvents.remove(eventThatIsANote)
                foundEvents.getOrNull(index)?.let{ e ->
                    foundEvents[index] = e.copy(name = eventThatIsANote.name + " " + e.name)
                }
            }

            //add a DUTY event if a Checkin and Checkout event are present
            foundEvents.firstOrNull { it.type == EventTypes.CHECK_IN }?.let{ ci ->
                foundEvents.firstOrNull { it.type == EventTypes.CHECK_OUT }?.let{ co ->
                    Event(name = FLIGHT_DAY_NAME, type = EventTypes.DUTY, startTime = ci.startTime, endTime = co.endTime, info = ci.info, notes = ci.notes)
                }
            }?.let{
                foundEvents.add(it)
            }

            val fdp = fdpRegex.find(dayContentsString)?.groupValues?.let{ r ->
                Duration.ofMinutes(r[1].toLong() * 60 + r[2].toLong())
            }


            return Day(date, foundEvents, legend, fdp) // TODO should not be emptylist obviously
        }

        private fun getDate(dayString: String, period: InstantRange): LocalDate?{
            val dayOfMonth = dayString.filter { it.isDigit() }.toInt()
            return period.dates.firstOrNull{ it.dayOfMonth == dayOfMonth}
        }

        private val timeFormatter = DateTimeFormatter.ofPattern("HHmm")

        private val fdpRegex = """\[FDP (\d\d):(\d\d)]""".toRegex()
        private val extraMessageRegex = """(To c/m:.*)""".toRegex(RegexOption.DOT_MATCHES_ALL)
        private val mainEventRegex = """(?:^T[A-Z].*|^S/U\s.*|^C/I\s.*|^LV[A-Z]+\s.*|^SL[A-Z]+\s.*)""".toRegex()

        private const val FLIGHT_DAY_NAME = "Flight Day"

    }
}

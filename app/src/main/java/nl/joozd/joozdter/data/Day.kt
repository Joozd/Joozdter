package nl.joozd.joozdter.data

import nl.joozd.joozdter.utils.InstantRange
import nl.joozd.joozdter.data.EventTypes.*
import nl.joozd.joozdter.data.events.*
import nl.joozd.joozdter.data.events.actualEvents.CheckOutEvent
import nl.joozd.joozdter.data.events.actualEvents.CheckinEvent
import nl.joozd.joozdter.data.events.getEventsThatNeedCompleting
import nl.joozd.joozdter.utils.extensions.replaceValue
import java.time.*

/**
 * A Day holds all events for that day
 * It also has a static function [Day.of] that does a lot of work generating all those events from a string.
 * @param date: Date of this [Day]
 * @param events: All events of this day
 */
class Day(val date: LocalDate, val events: List<Event>){
    /**
     * Completes times for this day.
     * @return a [Day] where all [events] have their times completed
     */
    fun completeTimes(allDays: Collection<Day>): Day{
        val nextDay = getNextWorkingDay(allDays)
        val currentEvents = events.toMutableList()
        events.getEventsThatNeedCompleting().forEach{
            currentEvents.updateTimesForEvent(it, nextDay)
        }
        return Day(date, currentEvents)
    }

    /**
     * Get the start time of the first event of this day
     */
    fun startOfDay(): Instant = getFirstEvent()?.startTime ?: standardStartTime()

    override fun toString(): String = "Day: $date\nEvents:\n${events.joinToString("\n")}"

    /**
     * replace a CompleteableEvent by its version with completed times
     */
    private fun MutableList<Event>.updateTimesForEvent(event: CompleteableEvent, nextDay: Day?){
        require (event is Event){ "event must be an Event"}
        this.replaceValue(event, event.completeTimes(this@Day, nextDay))
    }

    /**
     * Generate a standard start
     */
    private fun standardStartTime(): Instant = ZonedDateTime.of(date, LocalTime.of(5,30), ZoneId.of("Europe/Amsterdam")).toInstant()

    /**
     * Get the next working day, or null if none found
     * (when it ends with a route day it will also return null)
     */
    private fun getNextWorkingDay(days: Collection<Day>): Day?{
        var nextDay = days.firstOrNull { it.date == date.plusDays(1) } ?: return null
        while (nextDay.events.any {it.type == ROUTE_DAY}) nextDay = days.firstOrNull { it.date == nextDay.date.plusDays(1) } ?: return null
        return nextDay
    }


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
         *
         * At this point, we have all the events in the roster,
         * but not all have start and/or end times.
         * These have to be added and/or calculated.
         * Since that needs info from the next day as well (for HOTEL end times) this is not done here.
         * When all days have been created, map the list with all days to it.completeTimes(allDays)
         */
        fun of(dayString: String, dayContentsString: String, legend: Map<String, String>? = null, period: InstantRange): Day?{
            //dayLines is the part from top of roster where all the dates are in one wide table
            val dayLines = dayString.lines()

            //data sanity check
            require(dayContentsString.startsWith(dayLines.first())) { "Day in $dayLines does not match day in $dayContentsString" }

            // If day is empty, parse and return it.
            parseEmptyDayIfAble(dayLines, period)?.let{
                return it
            }

            // If [foundEvents] returns null, bad data was received and no Day will be parsed.
            val foundEvents = parseEvents(dayContentsString, period, legend) ?: return null

            val date = getDateFromLines(dayLines, period) ?: return null
            return Day(date, foundEvents)
        }

        private fun parseEvents(dayContentsString: String,
                                period: InstantRange,
                                legend: Map<String, String>?
        ) : List<Event>? {
            //If no date found, no Day will be parsed
            val date = getDateFromLines(dayContentsString.lines(), period) ?: return null
            val extraMessage = dayContentsString.getExtraMessage()
            val lines = dayContentsString.linesWithEvents(extraMessage)
            return parseLines(lines, date, legend)
                .addDutyEvent()                 //add a DUTY event if a Checkin and Checkout event are present
                .addExtraMessage(extraMessage)  // add extraMessage to relevant items
                .addFdpInfo(dayContentsString)  // add fdp info as a note to relevant items
        }

        private fun getDateFromLines(dayLines: List<String>, period: InstantRange): LocalDate? =
            dayLines.firstOrNull()?.let { getDate(it, period) }


        /**
         * Get extraMessage from a dayContentsString
         */
        private fun String.getExtraMessage(): String? {
            return extraMessageRegex.find(this)?.groupValues?.get(1)
        }

        private fun List<Event>.addDutyEvent(): List<Event>{
            firstOrNull { it is CheckinEvent }?.let{ ci ->
                firstOrNull { it is CheckOutEvent }?.let{ co ->
                    Event.dutyEvent(ci as CheckinEvent, co as CheckOutEvent)
                }
            }?.let{
                return this + it
            }
                ?: return this
        }

        /**
         * Add extraMessage to relevant events:
         */
        private fun List<Event>.addExtraMessage(extraMessage: String?): List<Event>{
            if (extraMessage == null) return this
            val mainEvents = getMainEvents(this)
            return this.map { if (it in mainEvents) it.copy(info = extraMessage) else it}
        }

        /**
         * Add fdp info to relevant Events
         */
        private fun List<Event>.addFdpInfo(dayContentsString: String): List<Event>{
            val mainEvents = getMainEvents(this)

            return fdpRegex.find(dayContentsString)?.value?.let{ fdpString ->
                this.map{ if (it in mainEvents) it.copy(notes = "$fdpString\n" + it.notes) else it}
            } ?: this
        }

        /**
         * Parse lines into a [Day]
         */
        private fun parseLines(
            lines: List<String>,
            date: LocalDate,
            legend: Map<String, String>?
        ) = lines.mapNotNull { Event.parse(it, date, legend ?: emptyMap()) }

        /**
         * Remove [extraMessage] and first line (which is the day part) from a String.
         */
        private fun String.linesWithEvents(extraMessage: String?) =
            this.replace(extraMessage ?: "", "").lines()
                .drop(1)
                .filter { it.isNotBlank() }

        private fun getDate(dayString: String, period: InstantRange): LocalDate?{
            val dayOfMonth = dayString.filter { it.isDigit() }.toInt()
            return period.dates.firstOrNull{ it.dayOfMonth == dayOfMonth}
        }

        /**
         * Return all events that implement [MainEvent]
         */
        private fun getMainEvents(events: List<Event>): List<Event>{
            return events.filter { it is MainEvent }
        }

        /**
         * returns a day if it is an empty day or a day over. Throws an error on malformed data
         */
        private fun parseEmptyDayIfAble(dayLines: List<String>, period: InstantRange): Day? {
            require(dayLines.isNotEmpty()) { "Cannot parse empty text into a Day"}
            if (dayLines.size == 4) return null // this is a normal day
            val date = getDate(dayLines.first(), period)
                ?: error ("Cannot get day from ${dayLines.first()}")
            return when (dayLines.size) {
                1 -> Day(date, emptyList()) // empty day, can happen at start of employment
                2 -> { /*this is a route day, will lead to a day with only a "day over" event*/
                    val event = Event.dayOver(dayLines[1], date)
                    Day(date, listOf(event))
                }
                else -> error("Day.of(): Malformed [dayLines] does not have 1, 2 or 4 lines: $dayLines")
            }
        }
    }
}

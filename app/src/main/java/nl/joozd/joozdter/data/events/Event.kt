package nl.joozd.joozdter.data.events

import nl.joozd.joozdcalendarapi.CalendarEvent
import nl.joozd.joozdter.data.EventTypes
import nl.joozd.joozdter.data.events.actualEvents.*
import nl.joozd.joozdter.data.room.RoomEvent
import nl.joozd.joozdter.utils.extensions.endInstant
import nl.joozd.joozdter.utils.extensions.startInstant
import nl.joozd.joozdter.utils.extensions.toLocalDate
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * An event is one thing that happens, eg. pickup, 1 flight, a hotel stay or a checkin.
 * @param name: Name of the event (eg. Flight AMS-BKK, Simulator Duty or Hotel GRU)
 * @param type: Type of event (see [EventTypes])
 * @param startTime: Time at which the event starts. Can be null if this event doesn't have a start time (eg. checkOut)
 * @param endTime: Time at which the event ends Can be null if this event doesn't have aen end time (eg. checkIn)
 * @param info: extra info, such as aircraft type, rest margin, hotel name, etc
 * @param notes: Notes, such as crew names, FDP info, etc.
 * @param id: this Events ID in calendar, null if not inserted in calendar.
 */
open class Event(val name: String, val type: EventTypes, val startTime: Instant, val endTime: Instant, val info: String = "", val notes: String = "", val id: Long? = null) {
    val startEpochMilli: Long get() = startTime.toEpochMilli()
    val endEpochMilli: Long get() = endTime.toEpochMilli()


    override fun toString(): String = "name: $name\ntype: $type\nstart: $startTime\nend: $endTime\ninfo: $info\nnotes:$notes\nid: $id\n"

    /**
     * Works like copy in a data class
     */
    open fun copy(name: String = this.name,
                  type: EventTypes = this.type,
                  startTime: Instant = this.startTime,
                  endTime: Instant = this.endTime,
                  info: String = this.info,
                  notes: String = this.notes,
                  id: Long? = this.id
    ) = Event(name, type, startTime, endTime, info, notes, id)

    /**
     * Convert an Event to a RoomEvent.
     * startTime and endTime should not be null as only items in calendar will be saved in RoomDB
     *  and those must have start and end time
     */
    fun toRoomEvent(): RoomEvent =
        RoomEvent(
            id = 0,
            name,
            startTime.epochSecond,
            endTime.epochSecond,
        )


    fun toCalendarEvent(): CalendarEvent =
        CalendarEvent(
            calendarID = CalendarEvent.ID_NOT_SET,
            title = name,
            location = info,
            description = notes,
            startEpochMillis = startEpochMilli,
            endEpochMillis = endEpochMilli,
            timeZone = "UTC",
            allDay = type == EventTypes.ROUTE_DAY || type == EventTypes.LEAVE
        )

    /**
     * Get the date of this event.
     */
    fun date(zoneOffset: ZoneOffset = ZoneOffset.UTC): LocalDate? =
        startTime.toLocalDate(zoneOffset) ?: endTime.toLocalDate(zoneOffset)

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
        result = 31 * result + (startTime.hashCode())
        result = 31 * result + (endTime.hashCode())
        result = 31 * result + info.hashCode()
        result = 31 * result + notes.hashCode()
        return result
    }

    companion object {
        private const val FLIGHT_DAY_NAME = "Flight Day"

        /**
         * Make a "Day Over" event
         */
        fun dayOver(name: String, date: LocalDate) = RouteDayEvent(
            name,
            date.startInstant(),
            date.endInstant()
        )

        /**
         * Make a duty event from a checkin and checkout event
         */
        fun dutyEvent(checkInEvent: CheckinEvent, checkOutEvent: CheckOutEvent): Event =
            Event(
                name = FLIGHT_DAY_NAME,
                type = EventTypes.DUTY,
                startTime = checkInEvent.startTime,
                endTime = checkOutEvent.endTime,
                info = checkInEvent.info,
                notes = checkInEvent.notes
            )

        /**
         * Parse an event from a string, date and legend
         */
        fun parse(
            input: String,
            date: LocalDate,
            legend: Map<String, String> = emptyMap()
        ): Event? {
            require('\n' !in input.trim()) { "Only parse single lines into a single event!" }

            val constructorData = EventConstructorData(input, date, legend)

            return when {
                input matches checkInRegex      -> CheckinEvent(constructorData)
                input matches checkOutRegex     -> CheckOutEvent(constructorData)
                input matches clickRegex        -> ClickEvent(constructorData)
                input matches flightRegex       -> FlightEvent(constructorData)
                input matches hotelRegex        -> HotelEvent(constructorData)
                input matches leaveRegex        -> LeaveEvent(constructorData)
                input matches dayOverRegex      -> RouteDayEvent(constructorData)
                input matches simRegex          -> SimEvent(constructorData)
                input matches trainingRegex     -> TrainingEvent(constructorData)
                input matches standbyRegex      -> StandbyEvent(constructorData)

                input matches pickupRegex       -> PickupEvent(constructorData)

                else -> null.also { println("No event found in $input") }
            }
        }
    }
}
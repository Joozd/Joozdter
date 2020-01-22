package nl.joozd.joozdter.utils

import android.util.Log
import nl.joozd.joozdter.data.Day
import nl.joozd.joozdter.data.EventOld
import nl.joozd.joozdter.data.EventType
import org.threeten.bp.*
import org.threeten.bp.format.DateTimeFormatter

val dayStartTime = LocalTime.of(3,30)


/**
 * This does a number of things:
 * Fix start and end times for hotels, with days over and such
 * Add CAO margins
 */
fun fixHotels(days: List<Day>): List<Day>{
    val fixedDays: MutableList<Day> = mutableListOf()
    days.forEach{day ->
        val todaysEvents: MutableList<EventOld> = mutableListOf()
        day.events.forEach {event ->
            when (event.event_type) {
                EventType.hotel -> {
                    var daysOver: Long = 0
                    while ((findDayOver(days.firstOrNull {d -> d.dateAsLocalDate == day.dateAsLocalDate.plusDays(1+daysOver)}) != null)) daysOver++

                    if (daysOver > 0) {
                        val nextDay = (days.firstOrNull { d -> d.dateAsLocalDate == day.dateAsLocalDate.plusDays(1+daysOver)})
                        todaysEvents.add(event.copy(start_time = findDayEnd(day)?.end_time ?: (LocalDateTime.of(day.dateAsLocalDate, dayStartTime)).format(DateTimeFormatter.ISO_DATE_TIME),
                            end_time = findDayStart(nextDay)?.start_time ?: (LocalDateTime.of(day.dateAsLocalDate.plusDays(1+daysOver), dayStartTime)).format(DateTimeFormatter.ISO_DATE_TIME)))
                    } else{
                        val tomorrow = days.firstOrNull {d -> d.dateAsLocalDate == day.dateAsLocalDate.plusDays(1)}
                        val eventWithFixedTimes = event.copy(
                            start_time = findDayEnd(day)?.end_time ?: (LocalDateTime.of(day.dateAsLocalDate, dayStartTime)).format(DateTimeFormatter.ISO_DATE_TIME), // default is 03:30 today
                            end_time = findDayStart(tomorrow)?.start_time ?: (LocalDateTime.of(day.dateAsLocalDate.plusDays(1), dayStartTime)).format(DateTimeFormatter.ISO_DATE_TIME))

                        todaysEvents.add(
                            eventWithFixedTimes.copy(extra_data = makeCaoMarginString(day, tomorrow ?: day)) // default is 03:30 tomorrow
                        )
                    }
                }
                EventType.flight_day -> todaysEvents.add(event.copy(notes = if ("Toc/m:" in event.notes) event.notes.drop(event.notes.indexOf("Toc/m:") + 6) else event.notes))
                EventType.leave -> {
                    todaysEvents.add(event.copy(start_time = LocalDateTime.of(day.dateAsLocalDate, LocalTime.of(0,0,0)).toString() + ":00", end_time = LocalDateTime.of(day.dateAsLocalDate, LocalTime.of(0,0,0)).plusDays(1).toString() + ":00"))
                }
                EventType.other -> todaysEvents.add(event.copy(notes = if ("Toc/m:" in event.notes) event.notes.drop(event.notes.indexOf("Toc/m:") + 6) else event.notes))
                else -> todaysEvents.add(event)
            }
            if (event.start_time.isEmpty()) Log.d("event", event.description)
            if (event.end_time.isEmpty()) Log.d("event", event.description)
        }
        fixedDays.add(Day(day.date, todaysEvents))
    }
    return fixedDays
}

fun findDayStart(day: Day?): EventOld? = day?.events?.firstOrNull { it.event_type == EventType.taxi }
    ?: day?.events?.firstOrNull { it.event_type == EventType.flight_day }
    ?: day?.events?.firstOrNull { it.event_type == EventType.day_over }

fun findDayEnd(day: Day?): EventOld? = day?.events?.firstOrNull { it.event_type == EventType.flight_day }

fun Day.getFlightDay(): EventOld? = this.events.firstOrNull{ it.event_type == EventType.flight_day}


/**
 * calculates the time between two flight_day events on two days (end of day1 untill beginning of day2)
 * Will return 0 if there isn't a flight_day Event in both days of if day2 starts before day1 end
 * or if for some reason startInstant or endInstant are null
 * If things are OK it will return the duration between the two flight days
 */
fun calculateRest(day1: Day, day2: Day): Duration {
    val flightDay1 = day1.getFlightDay()
    val flightDay2 = day2.getFlightDay()
    if (flightDay1 == null || flightDay2 == null) return Duration.ZERO
    if (flightDay1.endInstant == null || flightDay2.startInstant == null) return Duration.ZERO
    if (flightDay1.endInstant!! > flightDay2.startInstant!!) return Duration.ZERO
    return Duration.ofMillis(flightDay2.startInstant!! - flightDay1.endInstant!!)
}


fun findDayOver(day: Day?): EventOld? =  day?.events?.firstOrNull { it.event_type == EventType.day_over }


fun findTaxiOrNull(day:Day?): EventOld? = day?.events?.firstOrNull { it.event_type == EventType.taxi }

fun makeCaoMarginString(day1: Day, day2: Day): String{
    val rest = calculateRest(day1, day2)
    if (rest.seconds == 0L) return ""
    val taxiTime = findTaxiOrNull(day2)?.duration ?: Duration.ofMinutes(30)
    val correctedTaxiTime = if (taxiTime.toMinutes() >= 30) taxiTime else Duration.ofMinutes(30)
    val hotelTime = rest - correctedTaxiTime - correctedTaxiTime
    if (hotelTime.toHours() <  10.0) return "Below Normrest by ${(600-hotelTime.toMinutes())} minutes!!"
    return "Rest margin (CAO): ${(hotelTime.toMinutes().toInt() - 600)/60}:${((hotelTime.toMinutes().toInt() - 600)%60).toString().padStart(2,'0')}"
}


/*
fun makeCaoMarginString(event: Event, taxiTime: Duration?): String {
    if (event.duration == null) return ""
    val taxi = if (taxiTime == null || taxiTime.toMinutes() <= 30) 30 else taxiTime.toMinutes()
    val rest = Duration.of( event.duration!!.toMinutes() - (2 * taxi), ChronoUnit.MINUTES)
    if (rest.toHours() <  10.0) return "Below Normrest by ${(600-rest.toMinutes().toInt())} minutes!!"
    return "Rest margin (CAO): ${(rest.toMinutes().toInt() - 600)/60}:${((rest.toMinutes().toInt() - 600)%60).toString().padStart(2,'0')}" // fixed, not yet in live version
}
*/
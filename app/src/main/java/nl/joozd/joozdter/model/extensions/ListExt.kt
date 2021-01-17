package nl.joozd.joozdter.model.extensions

import android.util.Log
import nl.joozd.joozdter.data.JoozdterLayoutOptions
import nl.joozd.joozdter.data.JoozdterPrefs
import nl.joozd.joozdter.model.Event
import nl.joozd.joozdter.utils.fdpChecker
import nl.joozd.klcrosterparser.Activities
import java.time.ZoneId
import nl.joozd.joozdter.utils.extensions.*
import java.time.Duration

/**
 * Add FDP info to Checkin events
 */
fun List<Event>.addMaxFDP(): List<Event>{
    val checkInEvents = filter { it.eventType == Activities.CHECKIN }.sortedBy { it.startInstant }
    val checkOutEvents = filter { it.eventType == Activities.CHECKOUT }.sortedBy { it.startInstant }
    if (checkInEvents.size != checkOutEvents.size){
        Log.e("addMaxFDP", "Not the same amount of checkins as checkouts, not touching data")
        return this
    }
    val newCheckinEvents = mutableListOf<Event>()
    checkInEvents.forEach { ci ->
        val co = checkOutEvents.firstOrNull {it.startInstant > ci.startInstant} ?: return this.also{
            Log.e("addMaxFDP", "No check-out found after check-in $ci")
        }
        val stretches = filter{it.eventType == Activities.FLIGHT && it.startInstant in (ci.startInstant..co.startInstant)}.size
        val maxFDP = fdpChecker(ci.startTime, co.startTime, stretches, ZoneId.of("Europe/Amsterdam")) // Only works for Amsterdam as that is always homebase for KLC
        val actualFdp = co.startTime - ci.startTime
        val marginMinutes = (maxFDP - actualFdp).toMinutes()
        val marginString = if (marginMinutes > 0) "FDP Margin: ${marginMinutes.minutesToString()} "
            else "FDP TOO HIGH by $marginMinutes minutes"
        val fdpInfoString = "FDP Max: ${maxFDP.toMinutes().minutesToString()}\n" +
                            "FDP Act: ${actualFdp.toMinutes().minutesToString()}"

        newCheckinEvents.add(ci.copy(extraData = marginString, notes = fdpInfoString))
    }
    return newCheckinEvents + filter{it !in checkInEvents}
}

/**
 * Add CAO rest margin info to Hotel activities
 * TODO check FDP of previous CheckOut event against actual rest for FDP Rest margin
 */
fun List<Event>.addCAORest(): List<Event>{
    val hotelEvents = filter { it.eventType == Activities.HOTEL }.sortedBy { it.startInstant }
    val checkInEvents = filter { it.eventType == Activities.CHECKIN }.sortedBy { it.startInstant }

    val newHotelEvents = mutableListOf<Event>()
    hotelEvents.forEach { hotelEvent ->
        val ci = checkInEvents.firstOrNull { it.startTime > hotelEvent.endTime }
        //check-out time is starttime of Hotel event
        if (ci == null) newHotelEvents.add(hotelEvent) // if no check- out time after end of hotel or check-out before begin, don't change Hotel event
        else { // We have a hotel, and a checkin after the hotel
            val grossRest = ci.startTime - hotelEvent.startTime
            val taxiTime = maxOf((ci.startTime - hotelEvent.endTime) * 2, Duration.ofMinutes(60))
            val netRest = grossRest - taxiTime
            val notes = if (netRest < REQUIRED_REST)
                "Rest too short by ${(REQUIRED_REST - netRest).toMinutes()} minutes\n" +
                "Required rest: ${(REQUIRED_REST + taxiTime).toMinutes().minutesToString()}\n" +
                "Actual rest: ${grossRest.toMinutes().minutesToString()}"
            else "Required rest: ${(REQUIRED_REST + taxiTime).toMinutes().minutesToString()}\n" +
                 "Actual rest: ${grossRest.toMinutes().minutesToString()}"
            val extraData = if (netRest < REQUIRED_REST)
                "Rest below CAO by ${(REQUIRED_REST - netRest).toMinutes()} minutes\n"
            else "Rest margin (CAO): ${(netRest - REQUIRED_REST).toMinutes().minutesToString()}"
            newHotelEvents.add(hotelEvent.copy(extraData = extraData, notes = notes))
        }
    }
    return newHotelEvents + filter {it !in hotelEvents}
}

fun List<Event>.setPreferredDescription(): List<Event> = map{ event ->
    when (event.eventType){
        Activities.LEAVE, Activities.OTHER_DUTY, Activities.SIM, Activities.STANDBY -> {
            when (JoozdterPrefs.preferedLayout) {
                JoozdterLayoutOptions.CODE -> event.copy(
                    description = event.description.split(
                        " "
                    ).firstOrNull() ?: event.description
                )
                JoozdterLayoutOptions.DECODED -> {
                    val description = if (event.description.split(" ").size > 1)
                        if (event.description.split(" ").drop(1).joinToString(" ")
                                .trim().length > 1
                        ) event.description.split(
                            " "
                        ).drop(1).joinToString(" ").trim().drop(1).dropLast(1)
                        else event.description
                    else event.description
                    event.copy(description = description)
                }
                else -> event // JoozdterLayoutOptions.FULL is only other possibility
            }
        }
        else -> event
    }
}

private fun Long.minutesToString() = "${this/60}:${(this%60).toString().padStart(2, '0')}"
private val REQUIRED_REST =  Duration.ofHours(10)
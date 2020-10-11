package nl.joozd.joozdter.utils

import android.util.Log
import nl.joozd.joozdter.data.Event
import nl.joozd.klcrosterparser.Activities
import nl.joozd.klcrosterparser.RosterDay
import java.time.Duration
import java.time.ZoneId


/**
 * parseEvents will add useful data to events, such as roster notes and FTL limits
 * @param rosterDays: List of Rosterdays as found in KlcRosterParser.days
 *
 * Notes for flight day will go to C/I activity as extraMessage
 * FDP info etc will also go there.
 * //TODO perhaps make possible to change standard ZoneId
 * //TODO check hotel times
 */


fun parseEvents(rosterDays: List<RosterDay>): List<Event>{
    val standardZone = ZoneId.of("Europe/Amsterdam")


    val events = mutableListOf<Event>()
    rosterDays.forEach { day ->
        when{
            //when there is a checkin today, add info to checkin
            day.events.filter{it.type == Activities.CHECKIN}.isNotEmpty() -> {
                val dayMessage = if (day.extraInfo.isNotEmpty()) day.extraInfo + '\n' else ""
                val checkIn = day.events.first{it.type == Activities.CHECKIN}
                val checkOut = day.events.firstOrNull{it.type == Activities.CHECKOUT}
                val amountOfSectors = day.events.filter{it.type == Activities.FLIGHT}.size
                val maxFDP = (if (checkOut == null) Duration.ofHours(13) else fdpChecker(checkIn.start, checkOut.start, amountOfSectors, standardZone))
                val actualFdp = Duration.between(checkIn.start, checkOut?.start ?: checkIn.start.plusSeconds(60*60*13))
                val fdpMargin = maxFDP - actualFdp
                val fdpMessage = "Fdp margin: ${fdpMargin.toMinutes()/60}:${(fdpMargin.toMinutes()%60).toString().padStart(2,'0')}\n"
                val fdpLongMessage = "Max fdp: ${maxFDP.toMinutes()/60}:${(maxFDP.toMinutes()%60).toString().padStart(2,'0')}\nActual FDP: ${actualFdp.toMinutes()/60}:${(actualFdp.toMinutes()%60).toString().padStart(2,'0')}\n"

                day.events.forEach {e ->
                    events.add(when (e.type) {
                        Activities.CHECKIN ->   Event(e.type, e.description, e.start, e.end, fdpMessage, fdpLongMessage + dayMessage + (e.extraMessage ?: "") )
                        else -> Event(
                                e.type,
                                e.description,
                                e.start,
                                e.end,
                                e.extraMessage ?: "",
                                "")
                    }.also{ Log.d("Added event", "$it from $e") }
                    )
                }
            }
            else -> {
                val dayMessage = if (day.extraInfo.isNotEmpty()) day.extraInfo + '\n' else ""
                day.events.forEach {e ->
                    if (e === day.events.minByOrNull { it.start }!!) events.add(Event(e.type, e.description, e.start, e.end,  dayMessage, e.extraMessage?: ""))
                    else events.add(Event(e.type, e.description, e.start, e.end,  e.extraMessage?: "", "" ))
                }
            }
        }
    }

    // check CAO rest:
    val taxiEvents = events.filter{it.eventType == Activities.TAXI}
    taxiEvents.forEach { taxi ->
        val hotelEvent =
            events.firstOrNull { it.eventType == Activities.HOTEL && it.endTime == taxi.startTime }
        hotelEvent?.let {
            checkHotelRest(it, taxi).let { checkedRest ->
                events[events.indexOf(it)] = it.copy(extraData = checkedRest.first, notes = checkedRest.second)
            }
        }
    }

    return events.toList()
}


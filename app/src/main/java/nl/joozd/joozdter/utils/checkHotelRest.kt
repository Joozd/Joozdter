package nl.joozd.joozdter.utils

import nl.joozd.joozdter.data.Event
import java.time.Duration

/**
 * Returns a string for checking CAO rest duration in hotels
 * (11 ours or 10 hours + 2 times taxi time, Whichever is more)
 * @param hotel: Event containing Hotel time
 * @param taxi: Event containing taxi time from hotel to airport
 * @return a pair of [extraData] to [notes] to be added to event
 */
fun checkHotelRest(hotel: Event, taxi: Event): Pair<String, String>{
    val actualRest = hotel.duration
    val neededRest = maxOf((Duration.ofHours(10) + taxi.duration + taxi.duration), Duration.ofHours(11))
    val margin = actualRest - neededRest
    return "Rest margin (CAO): ${margin.toMinutes()/60}: ${(margin.toMinutes()%60).toString().padStart(2,'0')}" to
            "Actual: ${actualRest.toMinutes()/60}: ${(actualRest.toMinutes()%60).toString().padStart(2,'0')}\nNormrest: ${neededRest.toMinutes()/60}: ${(neededRest.toMinutes()%60).toString().padStart(2,'0')}"
}
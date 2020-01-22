package nl.joozd.joozdter.utils

import nl.joozd.joozdter.data.Event
import org.threeten.bp.Duration

fun checkHotelRest(hotel: Event, taxi: Event): Pair<String, String>{
    val actualRest = hotel.duration
    val neededRest = Duration.ofHours(10) + taxi.duration + taxi.duration
    val margin = actualRest - neededRest
    return "Rest margin (CAO): ${margin.toMinutes()/60}: ${(margin.toMinutes()%60).toString().padStart(2,'0')}" to
            "Actual: ${actualRest.toMinutes()/60}: ${(actualRest.toMinutes()%60).toString().padStart(2,'0')}\nNormrest: ${neededRest.toMinutes()/60}: ${(neededRest.toMinutes()%60).toString().padStart(2,'0')}"
}
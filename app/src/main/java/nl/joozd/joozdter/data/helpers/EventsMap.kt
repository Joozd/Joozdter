package nl.joozd.joozdter.data.helpers

import nl.joozd.joozdter.data.Day
import nl.joozd.joozdter.data.events.Event
import java.time.LocalDate

/**
 * A map of LocalDate to a list of all the Events on that date.
 */
class EventsMap private constructor(): HashMap<LocalDate, MutableList<Event>>() {
    /**
     * Constructs this map and fills it with all the dates and Events in [events]
     */
    constructor(events: Collection<Event>): this(){
        buildEventsMap(events)
    }

    /**
     * Make a List<Day> from this EventsMap
     */
    fun days(): List<Day> = this.map{
        Day(it.key, it.value)
    }



    /**
     * Build up the map
     */
    private fun buildEventsMap(events: Collection<Event>){
        fillMapWithEmptyArrayLists(getLocalDatesFromEventsNotNull(events))
        addEventsToMap(events)
    }


    /**
     * Get localDates from events
     */
    private fun getLocalDatesFromEventsNotNull(events: Collection<Event>): List<LocalDate> =
        events.mapNotNull { it.date() }


    /**
     * Set all values in a map to an empty ArrayList for all keys in [keys]
     */
    private fun fillMapWithEmptyArrayLists(keys: Collection<LocalDate>){
        keys.toSet().forEach{ this[it] = ArrayList() }
    }

    /**
     * Adds Events to the Map filled with ArrayLists.
     * Throws error when date of event not yet filled in Map
     */
    private fun addEventsToMap(events: Collection<Event>){
        events.forEach { this[it.date()]!!.add(it) }
    }


}
package nl.joozd.joozdter.data.events

import nl.joozd.joozdter.data.extensions.words
import nl.joozd.joozdter.utils.extensions.endInstant
import nl.joozd.joozdter.utils.extensions.startInstant
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Class for passing constructor data to Event's constructor
 * Holds all the functions for parsing the input to the data required for construction.
 */
internal class EventConstructorData(private val input: String, val date: LocalDate, val legend: Map<String, String>, ){
    // If checking all regexes happens to be too slow, I could make a function to only do the work
    // for the required type but I think we're good.
    private val hotelResults = hotelRegex.find(input)?.groupValues
    private val checkInResults = checkInRegex.find(input)?.groupValues
    private val checkOutResults = checkOutRegex.find(input)?.groupValues
    private val standbyRegexesults = standbyRegex.find(input)?.groupValues
    private val flightResults = flightRegex.find(input)?.groupValues
    private val clickResults = clickRegex.find(input)?.groupValues
    private val pickupResults = pickupRegex.find(input)?.groupValues


    /**
     * Make name with decoded version from legend
     */
    fun name() = makeNameFromLegend(input, legend)

    /**
     * Info for hotel from it's key (eg. H2 means Hotel No-tell, Alabama City, +1-555-364432)
     */
    fun hotelInfo(): String{
        val hotelKey = hotelResults!![1]
        return legend[hotelKey] ?: "---"
    }

    /**
     * Start of Event CheckInEvent
     */
    fun checkInTimeStart(): Instant =
        checkInResults!![1].timeStringToInstant(date)

    /**
     * End of Event for CheckOutEvent
     */
    fun checkOutTimeEnd(): Instant =
        checkOutResults!![1].timeStringToInstant(date)

    /**
     * Start time for TrainingEvent
     */
    fun trainingTimeStart(): Instant? =
        input.words().getOrNull(2)?.timeStringToInstant(date)

    /**
     * End time for TrainingEvent.
     */
    fun trainingTimeEnd(): Instant? =
        input.words().getOrNull(3)?.timeStringToInstant(date)

    /**
     * Start time for StandbyEvent
     */
    fun standbyTimeStart(): Instant =
        standbyRegexesults!![1].timeStringToInstant(date)

    /**
     * End time for StandbyEvent
     */
    fun standbyTimeEnd(): Instant =
        standbyRegexesults!![2].timeStringToInstant(date)

    /**
     * Name for FlightEvent
     */
    fun flightName(): String =
        flightResults!![1].filter { it != ' ' } + " ${flightResults[2]} - ${flightResults[5]}"
    /**
     * Start time for FlightEvent
     */
    fun flightTimeStart(): Instant =
        flightResults!![3].timeStringToInstant(date)

    /**
     * End time for FlightEvent
     */
    fun flightTimeEnd(): Instant =
        flightResults!![4].timeStringToInstant(date)

    /**
     * Any extra info for FlightEvent
     */
    fun flightInfo(): String =
        flightResults!![6].trim()

    /**
     * Start time for ClickEvent
     */
    fun clickTimeStart(): Instant =
        clickResults!![1].timeStringToInstant(date)

    /**
     * End time for ClickEvent
     */
    fun clickTimeEnd(): Instant =
        clickResults!![2].timeStringToInstant(date)

    /**
     * Start time for  PickupEvent
     */
    fun pickupTimeStart() =
        pickupResults!![1].timeStringToInstant(date)


    /**
     * Start of day for this Event. Used for all-day events.
     */
    fun dayStart(): Instant = date.startInstant()

    /**
     * End of day for this Event. Used for all-day events.
     */
    fun dayend(): Instant = date.endInstant()

    /**
     * Make a name from a word and a legend.
     * eg LVEC becomes LVEC (Leave Flightcrew 5C)
     */
    private fun makeNameFromLegend(input: String, legend: Map<String, String>): String =
        input.words().first().let { n ->
            legend[n]?.let { ln -> "$n ($ln)" } ?: n
        }

    private fun String.timeStringToInstant(date: LocalDate): Instant =
        LocalTime.parse(this, DateTimeFormatter.ofPattern("HHmm"))
            .atDate(date).toInstant(ZoneOffset.UTC)
}
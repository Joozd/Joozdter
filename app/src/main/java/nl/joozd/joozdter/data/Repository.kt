package nl.joozd.joozdter.data

import nl.joozd.joozdter.App
import nl.joozd.joozdter.data.room.EventDao
import nl.joozd.joozdter.data.room.EventsDatabase
import nl.joozd.joozdter.utils.extensions.atEndOfDay
import nl.joozd.joozdter.utils.extensions.toLocalDate
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * The repository is our one-stop-shop for all things saving and loading from external resources
 * such as Room DB or Calendar API.
 */
class Repository private constructor(private val eventDao: EventDao) {
    suspend fun allEvents(): List<Event> = eventDao.getAll().map { it.toEvent() }

    suspend fun getDay(date: LocalDate): Day{
        val startTime = date.atStartOfDay(ZoneOffset.UTC).toInstant().epochSecond
        val endTime = date.atEndOfDay(ZoneOffset.UTC).toInstant().epochSecond
        val events = eventDao.getEvents(startTime, endTime).map {it.toEvent()}
        return Day(date, events)
    }

    /**
     * Will get all Events from DB and puts them into Days.
     */
    suspend fun allDays(): List<Day> = eventsToDays(allEvents())

    /**
     * Gets all events in [dateRange] and puts them into [Day] objects.
     * Days without events are left out.
     */
    suspend fun getDays(dateRange: ClosedRange<LocalDate>): List<Day>{
        val startTime = dateRange.start.atStartOfDay(ZoneOffset.UTC).toInstant().epochSecond
        val endTime = dateRange.endInclusive.atEndOfDay(ZoneOffset.UTC).toInstant().epochSecond
        val events = eventDao.getEvents(startTime, endTime).map {it.toEvent()}
        return eventsToDays(events)
    }

    /**
     * Puts a list of [Event] into Days
     */
    private fun eventsToDays(events: List<Event>): List<Day>{
        val eventsMap = HashMap<LocalDate, MutableList<Event>>()
        events.map { it.startTime!!.toLocalDate()}.toSet().forEach {
            eventsMap[it] = ArrayList()
        }
        events.forEach {
            eventsMap[it.startTime!!.toLocalDate()]!!.add(it)
        }
        return eventsMap.map{
            Day(it.key, it.value)
        }
    }



    companion object{
        private var INSTANCE: Repository? = null

        @Synchronized
        fun getInstance() = INSTANCE
            ?: Repository(EventsDatabase.getDatabase(App.instance).eventDao()).also{
                INSTANCE = it
            }

    }
}
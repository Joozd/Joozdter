package nl.joozd.joozdter.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.joozd.joozdter.App
import nl.joozd.joozdter.data.events.Event
import nl.joozd.joozdter.data.room.EventDao
import nl.joozd.joozdter.data.room.EventsDatabase
import nl.joozd.joozdter.data.room.RoomEvent
import nl.joozd.joozdter.data.utils.eventsToDays
import nl.joozd.joozdter.utils.extensions.endEpochSecond
import nl.joozd.joozdter.utils.extensions.startEpochSecond
import java.time.LocalDate

/**
 * The repository is our one-stop-shop for all things saving and loading from external resources
 * such as Room DB or Calendar API.
 * Get an instance with [getInstance].
 */
class Repository private constructor(private val eventDao: EventDao) {
    /**
     * Get a single day from DB, or null if that day is not found in DB
     */
    suspend fun getDay(date: LocalDate): Day? =
        getDays(date..date).firstOrNull()

    /**
     * Will get all Events from DB and puts them into Days.
     */
    suspend fun allDays(): List<Day> = eventsToDays(allEvents())

    /**
     * Gets all events in [dateRange] and puts them into [Day] objects.
     * Days without events are left out.
     */
    suspend fun getDays(dateRange: ClosedRange<LocalDate>): List<Day> = withContext(Dispatchers.IO){
        val startTime = dateRange.start.startEpochSecond()
        val endTime = dateRange.endInclusive.endEpochSecond()
        val events = eventDao.getEvents(startTime, endTime).map {it.toEvent()}

        eventsToDays(events)
    }

    /**
     * Save events to DB
     */
    suspend fun saveEvents(events: List<Event>){
        val roomEventsArray = events.map{it.toRoomEvent()}.toTypedArray()
        eventDao.insertEvents(*roomEventsArray)
    }

    /**
     * get events from EventDao
     */
    private suspend fun getEvents(startEpochSecond: Long, endEpochSecond: Long): List<Event> {
        val roomEvents = eventDao.getEvents(startEpochSecond, endEpochSecond)
        return roomEventsToEvents(roomEvents)
    }

    /**
     * Get all events from Dao
     */
    private suspend fun allEvents(): List<Event> = roomEventsToEvents(eventDao.getAll())


    /**
     * Converts a Collection of [RoomEvent] to a List of [Event]
     */
    private fun roomEventsToEvents(roomEvents: Collection<RoomEvent>) = roomEvents.map { it.toEvent() }

    companion object{
        private var INSTANCE: Repository? = null

        @Synchronized
        fun getInstance() = INSTANCE
            ?: Repository(EventsDatabase.getDatabase(App.instance).eventDao()).also{
                INSTANCE = it
            }
    }
}
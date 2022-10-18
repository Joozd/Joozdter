package nl.joozd.joozdter.workers

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.joozd.joozdter.calendar.CalendarHandler
import nl.joozd.joozdter.data.Day
import nl.joozd.joozdter.data.JoozdterPrefs
import nl.joozd.joozdter.data.Repository
import nl.joozd.joozdter.data.events.Event
import nl.joozd.joozdter.data.utils.eventsToDays
import nl.joozd.joozdter.parser.RosterParser
import nl.joozd.joozdter.utils.InstantRange
import nl.joozd.joozdter.utils.extensions.endInstant
import nl.joozd.joozdter.utils.extensions.startInstant
import nl.joozd.joozdter.workers.JoozdterWorkersHub.URI_TAG

/**
 * This updateWorker assumes uri has been checked before sending it here.
 * It will do all the work parsing, checking, inserting into calendar and saving events.
 */
class UpdateWorker(appContext: Context, workerParams: WorkerParameters):
    CoroutineWorker(appContext, workerParams) {
    private val calendarHandler = CalendarHandler()
    private val repository get() = Repository.getInstance()


    override suspend fun doWork(): Result {
        val parser = RosterParser.ofUri(getUri(), applicationContext) ?: return Result.failure()
        val days = parser.parse() ?: return Result.failure()
        if (!checkCalendarWritePermission())
            return Result.failure()

        //if (wipeAllDataNeeded())
            wipeAllData()

        removeLegacyEventsFromDays(days) // this has a nukeCalendar() call that should be commented out
        //processDays(days)
        return Result.success()
    }



    @RequiresPermission(android.Manifest.permission.WRITE_CALENDAR)
    private suspend fun removeLegacyEventsFromDays(days: Collection<Day>){
        val dateRange = makeDateRange(days) ?: error("cannot nuke, dateRange is null")

        // Nuking is only for when things went wrong during testing
        calendarHandler.nukeCalendar(dateRange)
        //calendarHandler.showCalendar(dateRange)
        calendarHandler.removeLegacyEvents(dateRange)
    }

    private fun wipeAllDataNeeded() = JoozdterPrefs.wipeNeeded

    @RequiresPermission(android.Manifest.permission.WRITE_CALENDAR)
    private suspend fun wipeAllData(){
        val allEvents = repository.allEvents()
        removeEventsFromCalendar(allEvents)
        repository.wipe()

    }

    private fun makeDateRange(days: Collection<Day>): InstantRange?{
        val start = days.minByOrNull { it.date }?.date?.startInstant() ?: return null
        val end = days.maxByOrNull { it.date }?.date?.endInstant() ?: return null
        return (InstantRange(start..end))
    }


    @RequiresPermission(android.Manifest.permission.WRITE_CALENDAR)
    private suspend fun processDays(days: List<Day>): Result{
        var currentlyInCalendar = getDaysFromDisk().getEvents()
        //Log.d(this::class.simpleName,"Currently in calendar: ${currentlyInCalendar.size} items")
        val eventsToRemove = days.getEventsToRemove(currentlyInCalendar)
        removeEventsFromCalendar(eventsToRemove)
        removeEventsFromRepository(eventsToRemove)

        currentlyInCalendar = currentlyInCalendar.filter {it !in eventsToRemove}
        val daysToSave = saveDaysToCalendar(days, currentlyInCalendar)
        saveEventsToDatabase(daysToSave)
        //Log.d(this::class.simpleName,"saved ${daysToSave.size} days:")
        return Result.success()
    }


    private fun checkCalendarWritePermission(): Boolean {
        val result = ActivityCompat.checkSelfPermission(
            applicationContext,
            android.Manifest.permission.WRITE_CALENDAR
        )
        return result == PackageManager.PERMISSION_GRANTED
    }

    private suspend fun removeEventsFromRepository(eventsToRemove: Collection<Event>){
        repository.removeEvents(eventsToRemove)
    }



    @RequiresPermission(android.Manifest.permission.WRITE_CALENDAR)
    private suspend fun saveDaysToCalendar(days: Collection<Day>, currentlyInCalendar: Collection<Event>): List<Event>{
        val dirtyEventsToSave = days.getDaysToSave(currentlyInCalendar)
        val cleanedEventsToSave = updateEventsTimes(dirtyEventsToSave, currentlyInCalendar)
        return saveEventsToCalendar(cleanedEventsToSave)
    }

    private suspend fun saveEventsToDatabase(eventsToSave: List<Event>){
        Repository.getInstance().saveEvents(eventsToSave)
    }

    private fun Collection<Day>.getEvents() = this.map{it.events}.flatten()

    private fun Collection<Day>.getDaysToSave(knownEvents: Collection<Event>): List<Event> =
        this.map{ it.relevantEvents(knownEvents) }.flatten()

    @RequiresPermission(android.Manifest.permission.WRITE_CALENDAR)
    private suspend fun removeEventsFromCalendar(eventsToRemove: Collection<Event>) = withContext(Dispatchers.Default){
        launch{
            calendarHandler.deleteEvents(eventsToRemove)
        }
    }

    @RequiresPermission(android.Manifest.permission.WRITE_CALENDAR)
    private suspend fun saveEventsToCalendar(eventsToSave: Collection<Event>): List<Event> =
        calendarHandler.insertEvents(eventsToSave).filterNotNull()


    private fun updateEventsTimes(
        eventsToSave: Collection<Event>,
        otherEvents: Collection<Event>
    ): List<Event> {
        val allEvents = eventsToSave + otherEvents
        val allDays = eventsToDays(allEvents)
        val daysToSave = eventsToDays(eventsToSave)
        return daysToSave
            .map { it.completeTimes(allDays) }
            .getEvents()
    }



    private fun Collection<Day>.getEventsToRemove(knownEvents: Collection<Event>): List<Event> =
        this.map{ it.getObsoleteEvents(knownEvents)}.flatten()

    private suspend fun getDaysFromDisk() =
        Repository.getInstance().allDays()

    private fun getUri() =
        Uri.parse(inputData.getString(URI_TAG)) ?: error ("bad uri")
}
package nl.joozd.joozdter.workers

import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import nl.joozd.joozdter.App
import nl.joozd.joozdter.calendar.CalendarHandler
import nl.joozd.joozdter.data.Repository
import nl.joozd.joozdter.data.events.Event
import nl.joozd.joozdter.utils.InstantRange
import nl.joozd.joozdter.utils.extensions.endInstant
import nl.joozd.joozdter.utils.extensions.startInstant
import nl.joozd.joozdter.workers.JoozdterWorkersHub.CALENDAR_ID_TAG

/**
 * Moves all Events from one calendar to the next
 */
class CalendarMoverWorker(appContext: Context, workerParams: WorkerParameters):
    CoroutineWorker(appContext, workerParams) {

    private val calendarHandler = CalendarHandler()
    private val repository get() = Repository.getInstance()


    override suspend fun doWork(): Result {
        if (!App.instance.checkCalendarWritePermission()) return Result.failure()
        val newCalendarID = getCalendarID()
        moveAllEventsToNewCalendar(newCalendarID)
        return Result.success()
    }


    private fun getCalendarID() = inputData.getLong(CALENDAR_ID_TAG, Long.MIN_VALUE).takeIf{
        it != Long.MIN_VALUE
    } ?: error ("No calendar passed to calendarMoverWorker")

    @RequiresPermission(android.Manifest.permission.WRITE_CALENDAR)
    suspend fun moveAllEventsToNewCalendar(calID: Long){
        val allEvents = repository.allEvents()

        println("DEBUG:")
        makeDateRange(allEvents)?.let {
            println("started")
            calendarHandler.showCalendar(it, allEvents)
        } ?: println("No dateRange made")

        println("DEBUG: Moving ${allEvents.size} to calendar $calID")
        calendarHandler.moveEventsToNewCalendar(allEvents, calID)
    }

    private fun makeDateRange(events: Collection<Event>): InstantRange? {
        val start = events.minByOrNull { it.date()!! }?.date()?.startInstant() ?: return null
        val end = events.maxByOrNull { it.date()!! }?.date()?.endInstant() ?: return null
        return (InstantRange(start..end))
    }
}
package nl.joozd.joozdter.workers

import android.content.Context
import android.net.Uri
import android.os.Parcel
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.joozd.joozdter.calendar.CalendarHandler
import nl.joozd.joozdter.data.extensions.expandCollapsedList
import nl.joozd.joozdter.data.extensions.klcRosterEventFromData
import nl.joozd.joozdter.data.extensions.toModel
import nl.joozd.joozdter.model.extensions.addCAORest
import nl.joozd.joozdter.model.extensions.addMaxFDP
import nl.joozd.joozdter.model.extensions.addNotesToCorrectEvent
import nl.joozd.joozdter.model.filterEvents
import nl.joozd.joozdter.ui.utils.FeedbackEvents
import nl.joozd.joozdter.utils.InstantRange
import nl.joozd.klcrosterparser.KlcRosterParser
import java.io.FileNotFoundException
import java.time.Instant

/**
 * This will do the whole calendar-related part of the "adding roster to calendar" thing.
 *
 */
class CalendarUpdaterWorker(appContext: Context, workerParams: WorkerParameters):
    CoroutineWorker(appContext, workerParams) {

    /**
     * A suspending method to do your work.  This function runs on the coroutine context specified
     * by [coroutineContext].
     * <p>
     * A CoroutineWorker is given a maximum of ten minutes to finish its execution and return a
     * [ListenableWorker.Result].  After this time has expired, the worker will be signalled to
     * stop.
     *
     * @return The [ListenableWorker.Result] of the result of the background work; note that
     * dependent work will not execute if you return [ListenableWorker.Result.failure]
     */
    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val start = Instant.ofEpochSecond(inputData.getLong(START_TAG, 0))
        val end = Instant.ofEpochSecond(inputData.getLong(END_TAG, 0))
        val uri = Uri.parse(inputData.getString(URI_TAG))
        val inputStream = try {
            applicationContext.contentResolver.openInputStream(uri)
        } catch (e: FileNotFoundException) {
            Log.e(THIS, "File not found")
            return@withContext Result.failure()
        }
        if (inputStream == null) {
            Log.e(THIS, "inputStream is null")
            return@withContext Result.failure()
        }

        //parse inputstream as roster:

        val parsedRoster = KlcRosterParser(inputStream)
        if (!parsedRoster.seemsValid || parsedRoster.startOfRoster == null || parsedRoster.endOfRoster == null) {
            Log.e(THIS, "bad file received")
            return@withContext Result.failure()
        }

        //add notes to checkin or to first event on that day
        val days = parsedRoster.days.map { it.addNotesToCorrectEvent() }

        //TODO add FDP and CAO checks to relevant fields

        // get events from days and filter
        val allEvents = days.map{ day -> day.events.map{ it.toModel() }  }
            .flatten()
            // add FDP info
            .addMaxFDP()
            .addCAORest()

        //filter after processing because some info might be retreived from not-selected events
        val events = filterEvents(allEvents)

        val calendarHandler = CalendarHandler(applicationContext).apply { initialize() }

        (start..end).datesAsInstants.forEach { i ->
            val todaysEvents = calendarHandler.getEventsStartingOn(i)
            calendarHandler.deleteEvents(todaysEvents)
        }
        calendarHandler.insertEvents(events)

        Result.success()
    }



    private operator fun Instant.rangeTo(other: Instant) = InstantRange(this, other)

    companion object{
        const val THIS = "CalendarUpdaterWorker"

        const val START_TAG = "a"
        const val END_TAG = "b"
        const val URI_TAG = "c"
    }
}
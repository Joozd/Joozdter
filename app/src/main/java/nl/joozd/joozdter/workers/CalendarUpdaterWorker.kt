package nl.joozd.joozdter.workers

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.joozd.joozdter.calendar.CalendarHandler
import nl.joozd.joozdter.data.extensions.toModel
import nl.joozd.joozdter.model.extensions.addCAORest
import nl.joozd.joozdter.model.extensions.addMaxFDP
import nl.joozd.joozdter.model.extensions.addNotesToCorrectEvent
import nl.joozd.joozdter.model.extensions.setPreferredDescription
import nl.joozd.joozdter.model.filterEvents
import nl.joozd.joozdter.utils.InstantRange
import nl.joozd.klcrosterparser.KlcRosterParser
import java.io.FileNotFoundException
import java.time.Instant

/**
 * This will do the whole calendar-related part of the "adding roster to calendar" thing.
 * expects [inputData] with a startInstant (epochseconds), endInstant and an uri to a KLC roster
 * TODO: get start and end from roster
 */
class CalendarUpdaterWorker(appContext: Context, workerParams: WorkerParameters):
    CoroutineWorker(appContext, workerParams) {

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val start = Instant.ofEpochSecond(inputData.getLong(START_TAG, 0))
        val end = Instant.ofEpochSecond(inputData.getLong(END_TAG, 0))
        val uri = Uri.parse(inputData.getString(URI_TAG))

        // get inputstream from URI
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

        //parse inputstream as roster, fail on error
        val parsedRoster = KlcRosterParser(inputStream)
        if (!parsedRoster.seemsValid || parsedRoster.startOfRoster == null || parsedRoster.endOfRoster == null) {
            Log.e(THIS, "bad file received")
            return@withContext Result.failure()
        }

        //add notes to checkin or to first event on that day
        val days = parsedRoster.days.map { it.addNotesToCorrectEvent() }

        // get events from days and cast to model class `Event`
        val allEvents = days.map{ day -> day.events.map{ it.toModel() } }.flatten()
            .addMaxFDP()                    // add FDP info
            .addCAORest()                   // add CAO rest margin
            .setPreferredDescription()      // set preferred name (eg. "LVEC" or "LVEC (Leave 5C)")

        /**
         * Filter flights from [JoozdterPrefs]
         * This should be done after processing (eg. max FDP and CAO rest) because those functions
         * might need data in an event that gets filtered out.
         */
        val events = filterEvents(allEvents)

        //Calendarhandler takes care of inserting into and deleting from device's calendar
        val calendarHandler = CalendarHandler(applicationContext).apply { initialize() }

        /**
         * Delete joozdter events overlapping with roster from calendar
         * uses custom InstantRange
         * @see InstantRange if you want to know how that works
         */
        (start..end).datesAsInstants.forEach { i ->
            val todaysEvents = calendarHandler.getEventsStartingOn(i)
            calendarHandler.deleteEvents(todaysEvents)
        }

        /**
         * Insert (processed and filtered) events into device calendar
         */
        calendarHandler.insertEvents(events)

        // return success
        Result.success()
    }

    private operator fun Instant.rangeTo(other: Instant) = InstantRange(this, other)

    companion object{
        // for error logging
        const val THIS = "CalendarUpdaterWorker"

        const val START_TAG = "a"
        const val END_TAG = "b"
        const val URI_TAG = "c"
    }
}
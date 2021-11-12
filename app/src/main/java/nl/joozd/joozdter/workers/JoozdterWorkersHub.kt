package nl.joozd.joozdter.workers

import android.net.Uri
import android.os.Parcel
import androidx.work.*
import nl.joozd.joozdter.App
import nl.joozd.joozdter.calendar.CalendarDescriptor
import java.time.Instant

/**
 * Repository for starting workers.
 */
object JoozdterWorkersHub {
    /**
     * Updates calendar: clears Joozdter events from [startInstant] until [endInstant] and
     * inserts all events in [uri]
     * @param uri: uri pointing to a KLC Roster PDF
     */
    @Deprecated("Deprecated")
    fun updateCalendar(uri: Uri){
        val data = putUriInData(uri)
        val task = buildCalendarUpdaterWorkerTask(data)
        enqueTask(task)
    }

    /**
     * Invalidates a calendar.
     * This means all it's known Events will be removed from that calendar
     * and then the known-events DB will be cleared.
     */
    fun invalidateCalendar(calendarToInvalidate: CalendarDescriptor){
        val data = putCalendarInData(calendarToInvalidate)
        val task = buildCalendarInvalidatorWorkerTask(data)
        enqueTask(task)
    }

    /**
     * Processes a URI.
     * If Uri is bad, worker will return Result.failure()
     * @param uri: uri pointing to a KLC Roster PDF
     */
    fun processUri(uri: Uri){
        val data = putUriInData(uri)
        val task = buildUpdateWorkerTask(data)
        enqueTask(task)
    }

    private fun buildCalendarInvalidatorWorkerTask(data: Data) =
        OneTimeWorkRequestBuilder<CalendarInvalidatorWorker>()
            .addTag(UPDATE_CALENDAR_TAG)
            .setInputData(data)
            .build()


    @Deprecated("Deprecated")
    private fun buildCalendarUpdaterWorkerTask(data: Data) =
        OneTimeWorkRequestBuilder<CalendarUpdaterWorker>()
            .addTag(UPDATE_CALENDAR_TAG)
            .setInputData(data)
            .build()

    private fun buildUpdateWorkerTask(data: Data) =
        OneTimeWorkRequestBuilder<UpdateWorker>()
            .addTag(UPDATE_CALENDAR_TAG)
            .setInputData(data)
            .build()

    private fun enqueTask(task: OneTimeWorkRequest) {
        WorkManager.getInstance(App.instance)
            .enqueueUniqueWork(UPDATE_CALENDAR_TAG, ExistingWorkPolicy.APPEND_OR_REPLACE, task)
    }

    private fun putUriInData(uri: Uri) = Data.Builder().apply {
        putString(URI_TAG, uri.toString())
    }.build()

    private fun putCalendarInData(calendar: CalendarDescriptor) = Data.Builder().apply{
        putLong(CALENDAR_ID_TAG, calendar.calID)
    }.build()

    private const val UPDATE_CALENDAR_TAG = "UPDATE_CALENDAR_TAG"

    const val URI_TAG = "URI"
    const val CALENDAR_ID_TAG = "calID"
}
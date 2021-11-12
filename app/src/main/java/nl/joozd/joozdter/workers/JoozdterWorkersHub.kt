package nl.joozd.joozdter.workers

import android.net.Uri
import android.os.Parcel
import androidx.work.*
import nl.joozd.joozdter.App
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
    fun updateCalendar(uri: Uri){
        val data = putUriInData(uri)
        val task = buildCalendarUpdaterWorkerTask(data)
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
            .enqueueUniqueWork(UPDATE_CALENDAR_TAG, ExistingWorkPolicy.REPLACE, task)
    }

    private fun putUriInData(uri: Uri) = Data.Builder().apply {
        putString(CalendarUpdaterWorker.URI_TAG, uri.toString())
    }.build()

    private const val UPDATE_CALENDAR_TAG = "UPDATE_CALENDAR_TAG"
}
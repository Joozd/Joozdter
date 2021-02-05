package nl.joozd.joozdter.workers

import android.net.Uri
import android.os.Parcel
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
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

        val data = Data.Builder().apply{
            putString(CalendarUpdaterWorker.URI_TAG, uri.toString())
        }.build()
        val task = OneTimeWorkRequestBuilder<CalendarUpdaterWorker>()
            .addTag(UPDATE_CALENDAR_TAG)
            .setInputData(data)
            .build()

        with (WorkManager.getInstance(App.instance)){
            enqueueUniqueWork(UPDATE_CALENDAR_TAG, ExistingWorkPolicy.REPLACE, task)
        }
    }

    private const val UPDATE_CALENDAR_TAG = "UPDATE_CALENDAR_TAG"
}
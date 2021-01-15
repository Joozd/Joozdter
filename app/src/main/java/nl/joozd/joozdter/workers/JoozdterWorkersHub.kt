package nl.joozd.joozdter.workers

import android.net.Uri
import android.os.Parcel
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import nl.joozd.joozdter.App
import java.time.Instant

object JoozdterWorkersHub {
    /**
     * @param startInstant: Start instant of this roster (ie. when to start removing old data)
     * @param endInstant: End instant of this roster (ie. when to stop removing old data)
     * @param events: collapsed list of all events to be inserted (@see collapseDataList)
     */
    fun updateCalendar(startInstant: Instant, endInstant: Instant, uri: Uri){

        val data = Data.Builder().apply{
            putLong(CalendarUpdaterWorker.START_TAG, startInstant.epochSecond)
            putLong(CalendarUpdaterWorker.END_TAG, endInstant.epochSecond)
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
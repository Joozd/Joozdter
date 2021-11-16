package nl.joozd.joozdter

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import nl.joozd.joozdter.calendar.CalendarHandlerIndices.EVENT_PROJECTION
import nl.joozd.joozdter.calendar.CalendarHandlerOld
import nl.joozd.joozdter.mockcalendar.MockContentProvider
import nl.joozd.joozdter.mockcalendar.MockDatabase
import nl.joozd.joozdter.mockdata.MockCalendarData
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.time.Instant

//@RunWith(RobolectricTestRunner::class)
class ContentProviderTests {
    private val database = MockDatabase()
    private val contentResolver= MockContentProvider(database)

    @Before
    fun setup(){
        // fill database
    }

    @Test
    fun testCalendarCursor(){
        val uri: Uri = CalendarContract.Events.CONTENT_URI
        val selection: String = "((${CalendarContract.Events.CALENDAR_ID} = ?) AND (" +
                "${CalendarContract.Events.DTSTART} >= ?) AND (" +
                "${CalendarContract.Events.DTSTART} < ?))"
        val selectionArgs: Array<String> = arrayOf(
            2.toString(),
            Instant.EPOCH.toEpochMilli().toString(),
            Instant.MAX.toEpochMilli().toString()
        )
        contentResolver.query(
            uri,
            EVENT_PROJECTION,
            selection,
            selectionArgs,
            null
        ).use { cur ->
            //do something with cursor
        }
    }
}
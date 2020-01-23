package nl.joozd.joozdter.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.android.synthetic.main.activity_pdf_parser.*
import nl.joozd.joozdter.R
import nl.joozd.joozdter.calendar.CalendarHandler
import nl.joozd.joozdter.data.JoozdterPrefs

import nl.joozd.joozdter.data.SharedPrefKeys
import nl.joozd.joozdter.utils.parseEvents
import nl.joozd.klcrosterparser.KlcRosterParser
import org.jetbrains.anko.*
import org.threeten.bp.ZoneId
import java.io.FileNotFoundException
import java.util.concurrent.CountDownLatch

class PdfParserActivity : AppCompatActivity() {
    companion object {
        const val TAG = "PdfParserActivity"
    }

    private val initialized = CountDownLatch(1)
    private val calendarReady = CountDownLatch(1)
    private val calendarUpdateComplete = CountDownLatch(2)
    private val prefs = JoozdterPrefs()
    //private lateinit var sharedPref: SharedPreferences
    private var working = true

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        recreate()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidThreeTen.init(this)
        prefs.init(this)


        //get selected calendar name from sharedPrefs:
        val calendarName = prefs.pickedCalendar ?: "NOT FOUND!!!!!!1"

        // handle if no calendar selected yet:
        if (calendarName == "NOT FOUND!!!!!!1") {
            alert("No calendar picked, run app normally to pick one") {
                okButton {
                    finish()
                }
            }
        }

        //check if permissions are OK, ask if they aren't:
        while (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR), 0
            )
        }

        //initialize calendar, countDown clendarReady when done:
        val calendarHandler = CalendarHandler(this)
        doAsync {
            calendarHandler.initialize()
            calendarHandler.activeCalendar = calendarHandler.findCalendarByName(calendarName)
            if (calendarHandler.activeCalendar == null) {
                runOnUiThread {
                    longToast("problem with picked calendar, run app normally to pick one")
                    finish()
                }
            }
            calendarReady.countDown()
        }

        Log.d(TAG, "started")
        setContentView(R.layout.activity_pdf_parser)

        // get inputstream from intent:
        intent?.let {
            Log.d("PdfParserActivity", intent.action ?: "intent.action = null")
            Log.d("PdfParserActivity", intent.type ?: "intent.type = null")
            (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let {
                val inputStream = try {
                    /*
                 * Get the content resolver instance for this context, and use it
                 * to get a ParcelFileDescriptor for the file.
                 */
                    contentResolver.openInputStream(it)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                    Log.e("PdfParserActivity", "File not found.")
                    return
                }
                if (inputStream == null) {
                    Log.e(TAG, "Inputstream is null")
                    alert("Inputstream is null!")
                    return
                }
                receivedFileText.visibility = View.VISIBLE

                //parse inputstream as roster:
                doAsync {
                    val roster = KlcRosterParser(inputStream)
                    if (!roster.seemsValid) {
                        Log.e(TAG, "roster.seemsValid == false")
                        alert("This doesn't seem to be a KLC Roster")
                    } else {
                        runOnUiThread {
                            itSeemsToBeARosterText.visibility = View.VISIBLE
                        }
                        //TODO add FDP and CAO checks to relevant fields

                        //remove all events on days that are on current roster
                        val hotelEvent =
                            calendarHandler.getHotelEvent(roster.days.map { it.date }.min()!!)

                        runOnUiThread {
                            removingOldEvents.visibility = View.VISIBLE
                        }
                        roster.days.map { it.date }.forEach { date ->
                            val i = date.atStartOfDay().atZone(ZoneId.of("UTC")).toInstant()
                            val todaysEvents = calendarHandler.getEventsStartingOn(i)
                            calendarHandler.deleteEvents(todaysEvents)
                        }

                        //add new events to calendar:
                        runOnUiThread {
                            addingNewText.visibility = View.VISIBLE
                        }

                        val allEvents = (parseEvents(roster.days) + hotelEvent).filterNotNull()
                        calendarHandler.insertEvents(allEvents, prefs)
                        Log.d(
                            TAG,
                            "Inserted ${allEvents.size} items into calendar ${calendarHandler.activeCalendar}"
                        )

                    }
                    runOnUiThread {
                        doneText.visibility = View.VISIBLE
                    }
                }
            }
        }

    }

    override fun onBackPressed() {
        if (working) this.moveTaskToBack(true)
        else super.onBackPressed()
    }
} // class


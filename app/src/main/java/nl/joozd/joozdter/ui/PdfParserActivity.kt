package nl.joozd.joozdter.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.LocationTextExtractionStrategy
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.android.synthetic.main.activity_pdf_parser.*
import nl.joozd.joozdter.R
import nl.joozd.joozdter.calendar.CalendarHandler
import nl.joozd.joozdter.comm.Comms
import nl.joozd.joozdter.data.Day
import nl.joozd.joozdter.data.MetaData
import nl.joozd.joozdter.data.SharedPrefKeys
import nl.joozd.joozdter.extensions.toByteArray
import nl.joozd.joozdter.utils.fixHotels
import org.jetbrains.anko.*
import java.io.FileNotFoundException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketException
import java.net.UnknownHostException
import java.util.concurrent.CountDownLatch

class PdfParserActivity : AppCompatActivity() {
    companion object {
        const val TAG = "PDFParserActivity"
    }

    private val initialized = CountDownLatch(1)
    private val calendarReady = CountDownLatch(1)
    private val calendarUpdateComplete = CountDownLatch(2)
    private lateinit var sharedPref: SharedPreferences
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

        sharedPref = this.getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )

        val calendarName = sharedPref.getString(SharedPrefKeys.PICKED_CALENDAR, "NOT FOUND!!!!!!1")
        val shareName = sharedPref.getBoolean(SharedPrefKeys.SHARE_NAME, false)
        if (calendarName == "NOT FOUND!!!!!!1") {
            alert("No calendar picked, run app normally to pick one") {
                okButton {
                    finish()
                }
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR), 0
            )
        } else {
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


            Log.d("PdfParserActivity", "started")
            setContentView(R.layout.activity_pdf_parser)


            lateinit var server: Comms

            doAsync {
                try {
                    server = Comms()
                    initialized.countDown()
                } catch (he: UnknownHostException) {
                    val exceptionString = "An exception 11 occurred:\n ${he.printStackTrace()}"
                    Log.e(javaClass.simpleName, exceptionString, he)
                    working = false
                    runOnUiThread {
                        alert(getString(R.string.hostNotFound)) {
                            okButton {}
                        }.show()
                    }
                } catch (ioe: IOException) {
                    val exceptionString = "An exception 12 occurred:\n ${ioe.printStackTrace()}"
                    Log.e(javaClass.simpleName, exceptionString, ioe)
                    working = false
                } catch (ce: ConnectException) {
                    val exceptionString = "An exception 13 occurred:\n ${ce.printStackTrace()}"
                    Log.e(javaClass.simpleName, exceptionString, ce)
                    working = false
                    runOnUiThread {
                        alert(getString(R.string.serverRefused)) {
                            okButton {}
                        }.show()

                    }
                } catch (se: SocketException) {
                    val exceptionString = "An exception 14 occurred:\n ${se.printStackTrace()}"
                    Log.e(javaClass.simpleName, exceptionString, se)
                    working = false
                }
            }
            connecttingToServerTextView.setTypeface(null, Typeface.BOLD)
            doAsync {
                initialized.await()
                runOnUiThread {
                    connectedCheck.visibility = View.VISIBLE
                }
            }
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
                    }


                    /**
                     * FROM HERE PDFREADER TEST
                     */


                    val reader = PdfReader(inputStream)
                    Log.d(TAG, "#######################################################")
                    Log.d(TAG, "reader has ${reader.numberOfPages} pages")
                    Log.d(TAG, "extracted text w/ strategy LocationTextExtractionStrategy:\n")
                    Log.d(TAG, PdfTextExtractor.getTextFromPage(reader, 1, LocationTextExtractionStrategy()))
                    Log.d(TAG, "\n\nextracted text w/ strategy SimpleTextExtractionStrategy:\n")
                    Log.d(TAG, PdfTextExtractor.getTextFromPage(reader, 1, SimpleTextExtractionStrategy()))
                    Log.d(TAG, "#######################################################")


                    /**
                     * TO HERE PDFREADER TEST
                     */
/*
                    doAsync {

                        initialized.await()
                        runOnUiThread {
                            receivingDataText.setTypeface(null, Typeface.BOLD)
                        }
                        // TODO make some loading twirly thing
                        val reply = try {
                            server.sendPdfRoster(
                                inputStream!!.toByteArray(),
                                MetaData(
                                    sharedPref.getBoolean(
                                        SharedPrefKeys.SHARE_NAME,
                                        false
                                    )
                                )
                            )
                        } catch (he: UnknownHostException) {
                            val exceptionString =
                                "An exception 11 occurred:\n ${he.printStackTrace()}"
                            Log.e(javaClass.simpleName, exceptionString, he)
                            runOnUiThread {
                                alert(getString(R.string.hostNotFound)) {
                                    okButton {}
                                }.show()
                            }
                            emptyList<Day>()
                        } catch (exc: Exception) {   // catch any other exception
                            runOnUiThread {
                                alert(getString(R.string.errorParsingRoster)) {
                                    okButton {}
                                }.show()
                            }
                            emptyList<Day>()
                        }


                        server.close()
                        inputStream?.close()
                        if (reply == null) {
                            working = false
                            runOnUiThread {
                                alert(getString(R.string.errorWrongFile)) {
                                    okButton {}
                                }.show()
                            }
                        } else {
                            if (reply.isNotEmpty()) {
                                Log.d("pdfParserActivity", "werkt")
                                // TODO put days into calendar
                                runOnUiThread {
                                    receivingDataCheck.visibility = View.VISIBLE
                                }

                            } else {
                                working = false
                                Log.d("pdfParserActivity", "werkt niet")
                                // TODO depending on reason either save the thing to be sent when internet is available or show an error for bad file
                            }


                            Log.d("One Date", "${reply[0].dateAsLocalDate}")
                            Log.d(
                                "events found:",
                                reply.map { r -> r.events }.flatten().size.toString()
                            )
                            val receivedDates = reply.map { d -> d.dateStartAsInstant }
                            val fixedReply = fixHotels(reply)


                            // get all events on those dates made by me
                            val hotelEvent =
                                calendarHandler.getHotelEvent(receivedDates.min()!!)
                            runOnUiThread {
                                updatingCalendarText.setTypeface(null, Typeface.BOLD)
                            }
                            receivedDates.forEach { i ->
                                // don't do this async or you ill delete all fresh entries as well
                                // get events already in calendar on a received day
                                val todaysEvents = calendarHandler.getEventsStartingOn(i)
                                // delete all those events
                                calendarHandler.deleteEvents(todaysEvents)
                            }
                            calendarUpdateComplete.countDown()


                            // enter new events from reply in calendar
                            val allEvents =
                                (fixedReply.map { day -> day.events }.flatten() + hotelEvent).filterNotNull()
                            doAsync {
                                calendarHandler.insertEvents(allEvents, sharedPref)
                                calendarUpdateComplete.countDown()
                                working = false
                                calendarUpdateComplete.await()
                                runOnUiThread {
                                    doneCheck.visibility = View.VISIBLE
                                    longToast("Inserted ${allEvents.size} events into calendar!")
                                }
                            }
                        }
                    } // doAsync

 */
                }
            }
        } // oncreate
    }

    override fun onBackPressed() {
        if (working) this.moveTaskToBack(true)
        else super.onBackPressed()
    }
} // class


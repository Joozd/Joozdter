package nl.joozd.joozdter.ui.pdfParserActivity

import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import nl.joozd.joozdter.calendar.CalendarHandler
import nl.joozd.joozdter.ui.utils.FeedbackEvents.PdfParserActivityEvents
import nl.joozd.joozdter.ui.utils.JoozdterViewModel
import nl.joozd.joozdter.workers.JoozdterWorkersHub
import nl.joozd.klcrosterparser.KlcRosterParser
import java.io.FileNotFoundException

class PdfParserActivityViewModel: JoozdterViewModel() {
    private val calendarHandler = CalendarHandler(context)
    private val initializationMutex = Mutex()
    private var mIntent: Intent? = null

    /**
     * Parsing of intent should be triggered by [calendarWorkerReady]
     */
    private val _calendarWorkerReady = MutableLiveData(false)
    val calendarWorkerReady: LiveData<Boolean>; get() = _calendarWorkerReady

    private val _fileReceived = MutableLiveData(false)
    val fileReceived: LiveData<Boolean>; get() = _fileReceived

    private val _seemsToBeARoster = MutableLiveData(false)
    val seemsToBeARoster: LiveData<Boolean>; get() = _seemsToBeARoster

    init {
        viewModelScope.launch(Dispatchers.IO) {
            initializationMutex.withLock {

                calendarHandler.initialize()
                if (calendarHandler.activeCalendar == null) {
                    feedback(PdfParserActivityEvents.NO_VALID_CALENDAR_PICKED)
                }
                else _calendarWorkerReady.postValue(true)
            }
        }
    }

    /**
     * Process the received intent (once. Second time it will not do anything
     */
    private fun putIntent(intent: Intent){
        if (mIntent == null ) {
            mIntent = intent
        }
    }

    fun parseIntent(intent: Intent){
        putIntent(intent)
        mIntent?.let {
            viewModelScope.launch(Dispatchers.IO) {
                (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let { uri ->
                    val inputStream = try {
                        context.contentResolver.openInputStream(uri)
                    } catch (e: FileNotFoundException) {
                        feedback(PdfParserActivityEvents.FILE_NOT_FOUND)
                        return@launch
                    }
                    if (inputStream == null) {
                        feedback(PdfParserActivityEvents.FILE_ERROR).putString("InputStream is null")
                        return@launch
                    }
                    _fileReceived.postValue(true)

                    //parse inputstream as roster:

                    val parsedRoster = KlcRosterParser(inputStream)
                    if (!parsedRoster.seemsValid || parsedRoster.startOfRoster == null || parsedRoster.endOfRoster == null) {
                        feedback(PdfParserActivityEvents.NOT_A_KLC_ROSTER)
                        return@launch
                    }
                    _seemsToBeARoster.postValue(true)


                    /**
                     * Now, we have a parsed roster. This should be handed over to
                     * a CalendarUpdateWorker for entering into users' calendar.
                     * Unfortunately, I cannot fit all them bytes into a Data object.
                     * So we'll just send the URI and parse it again in worker.
                     */
                    println("Got roster:\n" +
                            "start: ${parsedRoster.startOfRoster}\n" +
                            "end: ${parsedRoster.endOfRoster}\n" +
                            "Contents: ${parsedRoster.events}"
                    )
                    JoozdterWorkersHub.updateCalendar(uri)
                    feedback(PdfParserActivityEvents.DONE)
                }
            }
        }
    }
}
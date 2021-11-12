package nl.joozd.joozdter.ui.pdfParserActivity

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nl.joozd.joozdter.exceptions.BadFileTypeException
import nl.joozd.joozdter.exceptions.NotARosterException
import nl.joozd.joozdter.model.IntentHandler
import nl.joozd.joozdter.ui.utils.FeedbackEvents.PdfParserActivityEvents
import nl.joozd.joozdter.ui.utils.JoozdterViewModel
import java.io.FileNotFoundException

class PdfParserActivityViewModelNew: JoozdterViewModel() {
    private val _progressCountDown = MutableLiveData(4)
    val progressCountDown: LiveData<Int>
        get() = _progressCountDown

    private var progress: Int
        get() = _progressCountDown.value!!
        set(it) { viewModelScope.launch(Dispatchers.Main) { _progressCountDown.value = it } }

    private val _fileReceived = MutableLiveData(false)
    val fileReceived: LiveData<Boolean>; get() = _fileReceived

    private val _seemsToBeARoster = MutableLiveData(false)
    val seemsToBeARoster: LiveData<Boolean>; get() = _seemsToBeARoster


    /**
     * Parse intent
     */
    fun parseIntent(intent: Intent) = viewModelScope.launch {
        //If no intentHandler received, return
        val intentHandler: IntentHandler = getIntentHandler(intent) ?: return@launch
        _fileReceived.postValue(true)

        if (!parseFile(intentHandler)) return@launch
        _seemsToBeARoster.postValue(true)

    }

    /**
     * Gets an IntentHandler and sets its progressHandler
     */
    private fun getIntentHandler(intent: Intent): IntentHandler? =
        try {
            IntentHandler(intent) {
                handleProgress(it)
            }
        } catch (e: FileNotFoundException) {
            feedback(PdfParserActivityEvents.FILE_NOT_FOUND)
            null
        }

    private suspend fun parseFile(intentHandler: IntentHandler): Boolean = try{
            intentHandler.sendFileToWorker()
            true
        } catch (e: BadFileTypeException){
            feedback(PdfParserActivityEvents.FILE_ERROR)
            false
        } catch (e: NotARosterException){
            feedback(PdfParserActivityEvents.NOT_A_KLC_ROSTER)
            false
        }



    private fun handleProgress(p: Int){
        progress = p
    }
}
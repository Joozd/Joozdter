package nl.joozd.joozdter.ui.pdfParserActivity

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import nl.joozd.joozdter.calendar.CalendarRepository
import nl.joozd.joozdter.exceptions.NoCalendarSelectedException
import nl.joozd.joozdter.parser.UriHandler
import nl.joozd.joozdter.utils.enums.Progress
import nl.joozd.joozdter.utils.extensions.getParcelableExtraUri

class PdfParserActivityViewModel2: ViewModel() {
    //MessageFlow messages are shown on screen in a dialog (errors, etc)
    private val _messageFlow = MutableStateFlow<String?>(null)
    val messageFlow: StateFlow<String?> get() = _messageFlow

    //ProgressFlow keeps track of progress
    private val _progressFlow = MutableStateFlow(Progress.STARTED)
    val progressFlow: StateFlow<Progress> get() = _progressFlow

    private var previouslyReceivedUri: Uri? = null

    /*
     * Parse an intent, if not already doing that
     */
    @RequiresPermission(allOf = [Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR])
    fun parseIntent(intent: Intent) {
        //This makes sure uri
        intent.getParcelableExtraUri()?.let{ uri ->
            if (previouslyReceivedUri != null && previouslyReceivedUri?.path == uri.path) return // Don't do the same work twice
            previouslyReceivedUri = uri
            viewModelScope.launch {
                try {
                    handleUri(uri)
                } catch(e: NoCalendarSelectedException){
                    _messageFlow.value = NO_CALENDAR_SELECTED_ERROR
                }
            }
        } // TODO: handle if intent extra is not an Uri

    }

    fun messageShown(){
        _messageFlow.value = null
    }

    @RequiresPermission(allOf = [Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR])
    private suspend fun handleUri(uri: Uri){
        val roster = UriHandler(uri, _progressFlow).getRoster()
        _progressFlow.value = Progress.SAVING_ROSTER
        CalendarRepository.instance.mergeRosterDaysIntoCalendar(roster.days)
        _progressFlow.value = Progress.DONE
    }

    companion object{
        const val NO_CALENDAR_SELECTED_ERROR = "ERROR: NO_CALENDAR_SELECTED"
    }


}
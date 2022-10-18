package nl.joozd.joozdter.ui.pdfParserActivity

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
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
    fun parseIntent(intent: Intent) {
        //This makes sure uri
        intent.getParcelableExtraUri()?.let{ uri ->
            if (previouslyReceivedUri != null && previouslyReceivedUri?.path == uri.path) return // Don't do the same work twice
            previouslyReceivedUri = uri
            viewModelScope.launch {
                handleUri(uri)
            }
        } // TODO: handle if intent extra is not an Uri

    }

    fun messageShown(){
        _messageFlow.value = null
    }

    private suspend fun handleUri(uri: Uri){
        val roster = UriHandler(uri, _progressFlow).getRoster()

    }


}
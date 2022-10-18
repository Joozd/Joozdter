package nl.joozd.joozdter.parser

import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import nl.joozd.joozdter.App
import nl.joozd.joozdter.exceptions.NotARosterException
import nl.joozd.joozdter.utils.enums.Progress

class UriHandler(
    private val uri: Uri,
    private val progressFlow: MutableStateFlow<Progress>
    ) {
    suspend fun getRoster(): Roster{
        progressFlow.value = Progress.GOT_FILE
        val pdfGrabber = PdfGrabber(App.instance, uri)
        progressFlow.value = Progress.READING_FILE
        val rosterLines = pdfGrabber.getText() ?: throw(NotARosterException("Not a PDF file."))
        val parser = RosterParser(rosterLines)
        if (!parser.checkIfValid()){
            progressFlow.value = Progress.ERROR
            throw(NotARosterException("Parser error: Not a valid roster."))
        }
        return Roster(parser.parse() ?: emptyList())
    }
}
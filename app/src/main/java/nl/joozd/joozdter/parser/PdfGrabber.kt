package nl.joozd.joozdter.parser

import android.content.Context
import android.net.Uri
import android.util.Log
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException

class PdfGrabber(val context: Context, val uri: Uri){
    @Suppress("BlockingMethodInNonBlockingContext") // running on Dispatchers.IO so is OK
    private suspend fun inputStream() = withContext(Dispatchers.IO){
        try {
            context.contentResolver.openInputStream(uri)
        } catch (e: FileNotFoundException){
            Log.w(this::class.simpleName, "No inputstream found in Uri (${uri.path})")
            null
        }
    }

    /**
     * Checks if this is actually an URI that holds a PDF
     */
    private fun isValid() = context.contentResolver.getType(uri) == PDF_MIME_TYPE

    /**
     * read the text from this URI
     * @return text from the URI's pdf if it is valid, or null if it isn't
     */
    @Suppress("BlockingMethodInNonBlockingContext") // running on Dispatchers.IO so is OK
    suspend fun getText(): List<String>? = withContext(Dispatchers.IO){
        if (!isValid()) null
        else inputStream()?.use{ stream ->
            val reader = PdfReader(stream)
            (1..reader.numberOfPages).map { PdfTextExtractor.getTextFromPage(reader, it, SimpleTextExtractionStrategy()) }
        }
    }

    companion object{
        const val PDF_MIME_TYPE = "application/pdf"
    }

}
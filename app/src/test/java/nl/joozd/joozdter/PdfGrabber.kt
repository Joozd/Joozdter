package nl.joozd.joozdter

import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy
import java.io.File

class PdfGrabber(val fileName: String) {
    private val reader = PdfReader(File(PATH + fileName + if (fileName.endsWith(SUFFIX, ignoreCase = true)) "" else SUFFIX)
        .inputStream())

    /**
     * Will read all pages to their own string.
     */
    fun read(): List<String> = (1..reader.numberOfPages).map { PdfTextExtractor.getTextFromPage(reader, it, SimpleTextExtractionStrategy())}

    companion object{
        const val PATH = "c:\\temp\\joozdter\\"
        const val SUFFIX = ".pdf"
    }
}


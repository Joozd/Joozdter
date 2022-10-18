package nl.joozd.joozdter

import nl.joozd.joozdter.parser.RosterParser
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class RosterParserTest {
    @Test
    fun canParseFile(){
        val pages = PdfGrabber("j4").read()
        assert(pages.isNotEmpty())
    }

}

package nl.joozd.joozdter

import nl.joozd.joozdter.data.extensions.replaceWithValue
import nl.joozd.joozdter.data.extensions.splitByRegex
import nl.joozd.joozdter.parser.RosterParser
import nl.joozd.klcrosterparser.KlcRosterParser
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class RosterParserTest {
    @Test
    fun canParseFile(){
        println("canParseFile()")
        val pages = PdfGrabber("j4").read()
        assert(pages.isNotEmpty())

        val days = RosterParser(pages).parse()

        println(days?.joinToString("\n***\n"))

        println("success")
    }

}

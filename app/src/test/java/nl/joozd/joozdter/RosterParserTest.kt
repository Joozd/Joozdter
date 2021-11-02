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
    private val periodRegex = """Period: (\d\d${month}\d\d) - (\d\d${month}\d\d)""".toRegex()
    private val dayRegExWithSpace = "(${weekDay}\\d\\d) ".toRegex()
    private val dayRegEx = "(${weekDay}\\d\\d)".toRegex()
    private val legendRegex = """Absence/Ground Activity Legend${NEWLINE}code description$NEWLINE(.*)""".toRegex(RegexOption.DOT_MATCHES_ALL)


    private val rosterTextRegex = """${ROSTER_START}(.*)""".toRegex(RegexOption.DOT_MATCHES_ALL)
    private val lastPageRosterTextRegex = """${ROSTER_START}(.*)$ROSTER_END""".toRegex(RegexOption.DOT_MATCHES_ALL)
    @Test
    fun canParseFile(){
        println("canParseFile()")
        val pages = PdfGrabber("r1").read()
        assert(pages.isNotEmpty())

        val days = RosterParser(pages).parse()

        println(days?.joinToString("\n***\n"))

        println("success")
    }




    private fun getPeriodFrom(periodString: String?): ClosedRange<Instant>?{
        val dateFormatter = DateTimeFormatter.ofPattern("ddMMMyy", Locale.US) // intentionally misusing yy instead of uu since if this is incorrect on the roster I want it to fail.
        periodRegex.find(periodString ?: return null)?.groupValues?.let{ result ->
            val startString = result[1]
            val endString = result[2]
            val startDate = LocalDate.parse(startString, dateFormatter)
            val endDate = LocalDate.parse(endString, dateFormatter)
            val startInstant = startDate.atStartOfDay(ZoneOffset.UTC).toInstant()
            val endInstant = endDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant() // ends at midnight at the end of endDate
            return (startInstant..endInstant)
        } ?: return null
    }

    private fun getRosterString(pages: List<String>): String? {
        //If there are any pages that have a roster but are not the end of the roster, they start here
        val initialRosterPages = pages.filter{ ROSTER_START in it && !lastPageRosterTextRegex.containsMatchIn(it) }
        val lastPage = pages.firstOrNull { lastPageRosterTextRegex.containsMatchIn(it) }
        return (initialRosterPages.joinToString("\n", postfix = "\n") { rosterTextRegex.find(it)!!.groupValues[1] } + //leading pages
                (lastPage?.let { p -> lastPageRosterTextRegex.find(p)?.groupValues?.get(1) } ?: ""))                          //last page
            .trim()                                                         // remove trailing or leading "\n"
            .takeIf { it.isNotBlank()}                                      // empty roster becomes null
            ?.replaceWithValue("""(${weekDay}\d\d) """.toRegex()){ "$it\n"} // place day markers on their own line
    }


    /*
    @Test
    fun escapeQuotes(){
        println("escapeQuotes")
        val sentence = """hallo! "Joozd" is "gaaf"!"""
        println(sentence)
        val escaped = sentence.escapeQuotes()
        println(escaped)
        assertEquals(sentence, escaped.restoreEscapedQuotes())
        println("ok!")
    }
    */
    companion object{
        // This line is the beginning of roster info. It is on all pages with a roster on it, and not on pages without.
        private const val ROSTER_START = "date H duty R dep arr AC info date H duty R dep arr AC info date H duty R dep arr AC info"
        //This line marks the end of the roster info part. It only appears after the last entry.
        private const val ROSTER_END = """Flight time \d+:\d+ Off days \d"""
        private const val NEWLINE = """(?:\r?\n|\r)""" // match any one of \r\n, \n or \r

        private const val weekDay = "(?:Mon|Tue|Wed|Thu|Fri|Sat|Sun)"
        private const val month = "(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)"
        private const val carrier = "DH/[A-Z]{2}|WA|KL"
    }
}

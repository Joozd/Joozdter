package nl.joozd.joozdter.parser

import android.content.Context
import android.net.Uri
import nl.joozd.joozdter.data.Day
import nl.joozd.joozdter.data.extensions.replaceWithValue
import nl.joozd.joozdter.data.extensions.splitByRegex
import nl.joozd.joozdter.utils.InstantRange
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Parse a KLC Roster.
 * All dates and times in UTC!
 * NOTE: [context] and [uri] can not be null, unless a parsed roster is injected through [parsedPdf]
 */
class RosterParser(private val parsedPdf: List<String>) {
    /**
     * Regexes we will use:
     */
    // get period from roster
    private val periodRegex = """Period: (\d\d${month}\d\d) - (\d\d${month}\d\d)""".toRegex()

    //grab the roster text from a roster
    private val rosterTextRegex = """${ROSTER_START}(.*)""".toRegex(RegexOption.DOT_MATCHES_ALL)
    private val lastPageRosterTextRegex = """${ROSTER_START}(.*)Flight time \d+:\d+ Off days \d""".toRegex(RegexOption.DOT_MATCHES_ALL)

    private val dayStringsRegex = """Period: \d\d${month}\d\d - \d\d${month}\d\d contract: .*?$NEWLINE(.*)$ROSTER_START""".toRegex(RegexOption.DOT_MATCHES_ALL)

    //get legend from a page (legend will be last item on its page so it grabs everything after legend's title)
    private val legendRegex = """Absence/Ground Activity Legend${NEWLINE}code description$NEWLINE(.*)""".toRegex(RegexOption.DOT_MATCHES_ALL)

    // Hotels can be last item on a page, so a backup regex is provided for that case.
    private val hotelsRegex = """Hotels$NEWLINE(.*)${NEWLINE}Recurrent Training / Checks""".toRegex(RegexOption.DOT_MATCHES_ALL)
    private val hotelsBackupRegex = """Hotels$NEWLINE(.*)""".toRegex(RegexOption.DOT_MATCHES_ALL) // this one only to be used if [hotelsRegex] not found on any page



    private val dayRegEx = "(${weekDay}\\d\\d)".toRegex()

    /**
     * Parse this roster into Days
     */
    fun parse(): List<Day>?{
        // If no pages found, no roster will be parsed
        val pages = parsedPdf.takeIf { it.isNotEmpty() } ?: return null

        // If no period found in this pdf, no roster will be parsed.
        val rosterPeriod: InstantRange = getPeriodFrom(periodRegex.find(pages.first())?.value) ?: return null

        val hotelsString = pages.firstNotNullOfOrNull { hotelsRegex.find(it)?.groupValues?.get(1) }
            ?: pages.firstNotNullOfOrNull { hotelsBackupRegex.find(it)?.groupValues?.get(1) }

        val legendString = pages.firstNotNullOfOrNull { legendRegex.find(it)?.groupValues?.get(1) }
        /**
         * If a legend is found, this will be it's map.
         * A line "RES2 Reserve at Home E2" will become "RES2" to "Reserve at Home E2"
         */
        val legend = legendString
            ?.lines()
            ?.filter{ it.isNotBlank() }
            ?.map { line ->
                line.split(" ")
                    .let {
                        it.first() to it.drop(1).joinToString(" ")
                    }}
            ?.toMap() ?: emptyMap<String, String>() + (buildHotelsLegend(hotelsString))

        println("Found Hotels string:\n$hotelsString\n*****")

        // If no roster text found, no roster will be parsed
        //there is a chance a roster is longer than one page. In this case rosterTextRegex will only return the last page
        val rosterString = getRosterString(pages)?: return null

        val dayContentStrings = rosterString.splitByRegex(dayRegEx, true).filter{ it.isNotBlank() }


        //If no dayStrings box on top of any page, no roster will be parsed
        val dayStrings = (pages.firstOrNull { dayStringsRegex.containsMatchIn(it) }?: return null).let{ page ->
            dayStringsRegex.find(page)!!.groupValues[1]
        }.splitByRegex(dayRegEx, true).filter{ it.isNotBlank() }.map {it.trim()}

        // println(dayStrings.joinToString("\n.....\n"))
        // println(dayContentStrings.joinToString("\n.....\n"))


        return dayContentStrings.mapNotNull {
            dayStrings.firstOrNull { ds -> it.startsWith(ds.lines().first()) }
                ?.let { ds -> Day.of(ds, it, legend, rosterPeriod) }
        }

    }

    /**
     * This gets its own function because some logic needs to be applied in case of multi-page rosters.
     *  - A roster can be 1 page
     *  - A roster can be multiple pages
     *  - A roster can be 1 page but [ROSTER_END] can be on the next page
     *  @return found roster data, or null if a blank string found.
     *  This Puts date strings (Mon03) on its own line.
     */
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

    private fun buildHotelsLegend(hotelsString: String?): Map<String, String>{
        val hotelRegex = """(H\d+ .*?)H\d""".toRegex() // will get all hotels but the last one

        return hotelRegex.findAll(hotelsString ?: return emptyMap()).map{
            it.groupValues[1]
        }.let{ hss ->
            var workingString: String = hotelsString
            //remove all found hotel strings, what remains is the last one
            hss.forEach{
                workingString = workingString.replace(it, "")
            }
            hss + workingString
        }.map{ it.replace (NEWLINE, " ")
                // part below generates "H1" to "Hotel No-Tell +001 555 23566" from "H1 Hotel No-Tell +001 555 23566"
            .split(" ")
            .let { words ->
                words.first() to words.drop(1).joinToString(" ")
            }
        }.toMap()




    }



    /**
     * Get a period from a "period string" (eg. Period: 25Oct21 - 21Nov21)
     */
    private fun getPeriodFrom(periodString: String?): InstantRange?{
        val dateFormatter = DateTimeFormatter.ofPattern("ddMMMyy", Locale.US) // intentionally misusing yy instead of uu since if this is incorrect on the roster I want it to fail.
        periodRegex.find(periodString ?: return null)?.groupValues?.let{ result ->
            val startString = result[1]
            val endString = result[2]
            val startDate = LocalDate.parse(startString, dateFormatter)
            val endDate = LocalDate.parse(endString, dateFormatter)
            val startInstant = startDate.atStartOfDay(ZoneOffset.UTC).toInstant()
            val endInstant = endDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant() // ends at midnight at the end of endDate
            return InstantRange(startInstant, endInstant)
        } ?: return null
    }

    companion object{
        /**
         * Create a RosterParser object from a Uri
         */
        suspend fun ofUri(context: Context, uri: Uri): RosterParser? = PdfGrabber(context, uri).getText()?.let { RosterParser(it) }


        // This line is the beginning of roster info. It is on all pages with a roster on it, and not on pages without.
        private const val ROSTER_START = "date H duty R dep arr AC info date H duty R dep arr AC info date H duty R dep arr AC info"
        //This line marks the end of the roster info part. It only appears after the last entry.
        private const val ROSTER_END = "date H duty R dep arr AC info date H duty R dep arr AC info date H duty R dep arr AC info"

        private const val NEWLINE = """(?:\r?\n|\r)""" // match any one of \r\n, \n or \r

        private const val weekDay = "(?:Mon|Tue|Wed|Thu|Fri|Sat|Sun)"
        private const val month = "(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)"
        private const val carrier = "DH/[A-Z]{2}|WA|KL"
    }
}
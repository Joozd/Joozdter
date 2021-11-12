package nl.joozd.joozdter.parser

import android.content.Context
import android.net.Uri
import nl.joozd.joozdter.data.Day
import nl.joozd.joozdter.data.extensions.replaceWithValue
import nl.joozd.joozdter.data.extensions.splitByRegex
import nl.joozd.joozdter.data.extensions.words
import nl.joozd.joozdter.data.utils.fixTimes
import nl.joozd.joozdter.utils.InstantRange
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Parse a KLC Roster.
 * All dates and times in UTC!
 */
class RosterParser(private val parsedPdf: List<String>) {
    /**
     * Regexes we will use in more than once place:
     */
    // get period from roster
    private val periodRegex = """Period: (\d\d${month}\d\d) - (\d\d${month}\d\d)""".toRegex()
    private val dayStringsRegex =
        """Period: \d\d${month}\d\d - \d\d${month}\d\d contract: .*?$NEWLINE(.*)$ROSTER_START"""
            .toRegex(RegexOption.DOT_MATCHES_ALL)

    //grab the roster text from a roster


    private val dayRegEx = "(${weekDay}\\d\\d)".toRegex()


    /**
     * Check if this seems to be a valid roster
     * It checks this by looking for key data in text, if that is here, it marks this as a correct
     * roster which can probably be parsed.
     * Can be fooled, but that will probably not happen unintentionally.
     */
    fun checkIfValid(): Boolean{
        val pages = parsedPdf.takeIf { it.isNotEmpty() } ?: return false
        pages.getPeriod() ?: return false
        getRosterString(pages)?: return false
        pages.getDayStringsString()?: return false
        return true
    }

    /**
     * Parse this roster into Days
     */
    fun parse(): List<Day>?{
        // If no pages found, no roster will be parsed
        val pages = parsedPdf.takeIf { it.isNotEmpty() } ?: return null
        //println("ROSTERPARSER: Got ${pages.size} pages")

        // If no period found in this pdf, no roster will be parsed.
        val rosterPeriod: InstantRange = pages.getPeriod() ?: return null
        //println("ROSTERPARSER: period = ${rosterPeriod.startDate} - ${rosterPeriod.endDate}")

        val rosterString = getRosterString(pages)?: return null
        //println("ROSTERPARSER: Got rosterString:\n\n$rosterString\n-o0o-\n\n")

        val dayContentStrings = getDayContentStrings(rosterString)
        //println("ROSTERPARSER: Got ${dayContentStrings.size} dayContentStrings")
        //println("ROSTERPARSER: they are:\n${dayContentStrings.joinToString("\n")}")

        //If no dayStrings box on top of any page, no roster will be parsed
        val dayStrings = pages.extractDayStrings()?: return null
        //println("ROSTERPARSER: Got ${dayStrings.size} dayStrings")

        //println(dayStrings.joinToString("\n.....\n"))
        //println(dayContentStrings.joinToString("\n.....\n"))

        val legend = pages.buildLegend()

        val unfinishedDays = buildDays(dayContentStrings, dayStrings, legend, rosterPeriod)
        //println("ROSTERPARSER: Got ${unfinishedDays.size} unfinishedDays")

        return unfinishedDays.fixTimes()

    }

    private fun buildDays(
        dayContentStrings: List<String>,
        dayStrings: List<String>,
        legend: Map<String, String>,
        rosterPeriod: InstantRange
    ) = dayContentStrings.mapNotNull {
        dayStrings.firstOrNull { ds -> it.startsWith(ds.lines().first()) }
            ?.let { ds -> Day.of(ds, it, legend, rosterPeriod) }
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
        val rosterTextRegex = """${ROSTER_START}(.*)""".toRegex(RegexOption.DOT_MATCHES_ALL)
        val lastPageRosterTextRegex = """${ROSTER_START}(.*)Flight time \d+:\d+ Off days \d"""
            .toRegex(RegexOption.DOT_MATCHES_ALL)

        //If there are any pages that have a roster but are not the end of the roster, they start here
        val initialRosterPages = pages.filter{ ROSTER_START in it && !lastPageRosterTextRegex.containsMatchIn(it) }

        val lastPage = pages.firstOrNull { lastPageRosterTextRegex.containsMatchIn(it) }
        return (initialRosterPages.joinToString("\n", postfix = "\n") { rosterTextRegex.find(it)!!.groupValues[1] } + //leading pages
                (lastPage?.let { p -> lastPageRosterTextRegex.find(p)?.groupValues?.get(1) } ?: ""))                          //last page
            .trim()                                                         // remove trailing or leading "\n"
            .takeIf { it.isNotBlank()}                                      // empty roster becomes null
            ?.replaceWithValue("""(${weekDay}\d\d) """.toRegex()){ "$it\n"} // place day markers on their own line
    }

    /**
     * Get string with hotel info from a roster
     */
    private fun getHotelsString(pages: List<String>): String?{
        // Hotels can be last item on a page, so a backup regex is provided for that case.
        val hotelsRegex = """Hotels$NEWLINE(.*)${NEWLINE}Recurrent Training / Checks"""
            .toRegex(RegexOption.DOT_MATCHES_ALL)

        // this one only to be used if [hotelsRegex] not found on any page
        val hotelsBackupRegex = """Hotels$NEWLINE(.*)"""
            .toRegex(RegexOption.DOT_MATCHES_ALL)

        return pages.firstNotNullOfOrNull { hotelsRegex.find(it)?.groupValues?.get(1) }
            ?: pages.firstNotNullOfOrNull { hotelsBackupRegex.find(it)?.groupValues?.get(1) }
    }

    /**
     * Gets legend String from a page
     * (legend will be last item on its page so it grabs everything after legend's title)
     */
    private fun getLegendString(pages: List<String>): String?{
        val legendRegex = """Absence/Ground Activity Legend${NEWLINE}code description$NEWLINE(.*)"""
            .toRegex(RegexOption.DOT_MATCHES_ALL)
        return pages.firstNotNullOfOrNull { legendRegex.find(it)?.groupValues?.get(1) }
    }

    /**
     * Parse legendString to a Legend Map
     */
    private fun buildGroundLegend(legendString: String?): Map<String, String> =
        legendString
            ?.lines()
            ?.filter{ it.isNotBlank() }
            ?.map { line ->
                line.split(" ")
                    .let {
                        it.first() to it.drop(1).joinToString(" ")
                    }
            }
            ?.toMap()
            ?: emptyMap()


    /**
     * If a legend is found, this will build it's map.
     */
    private fun List<String>.buildLegend(): Map<String, String>{
        val hotelsString = getHotelsString(this)
        val legendString = getLegendString(this)
        return buildGroundLegend(legendString) + (buildHotelsLegend(hotelsString))
    }

    private fun List<String>.getPeriod(): InstantRange?{
        return getPeriodFrom(periodRegex.find(this.first())?.value)
    }
    /**
     * Get Day Strings from pages
     * Day strings are the small 1-4 lines boxes at the top of a roster page
     */
    private fun List<String>.extractDayStrings(): List<String>? =
        this.getDayStringsString()
            ?.let { page ->
                dayStringsRegex.find(page)!!.groupValues[1]
            }?.splitByRegex(dayRegEx, true)
            ?.filter{ it.isNotBlank() }
            ?.map { it.trim() }

    /**
     * get string with all dayStrings
     */
    private fun List<String>.getDayStringsString(): String? =
        this.firstOrNull { dayStringsRegex.containsMatchIn(it) }

    /**
     * Build hotels legend (eg. H1=AC Hotel Valencia by Marriott, Valencia +34963317000, H2=NH Firenze, Florence 0039 055 2770)
     */
    private fun buildHotelsLegend(hotelsString: String?): Map<String, String>{
        hotelsString?.takeIf { it.isNotEmpty() } ?: return emptyMap() // return empty map on empty or null string
        val hotelRegex = """H\d+\s.*""".toRegex()
        val lines = LinkedList(hotelsString.lines())
        var currentLine = lines.pop()
        val hotelLines = ArrayList<String>()

        while (lines.isNotEmpty()){
            val l = lines.pop()
            if (l matches hotelRegex){
                hotelLines.add(currentLine)
                currentLine = l
            }
            else currentLine += " $l"
        }
        hotelLines.add(currentLine)

        return hotelLines.map { line ->
            line.words().let { words ->
                words.first() to words.drop(1).joinToString(" ")
            }
        }.toMap()
    }


    private fun getDayContentStrings(rosterString: String): List<String> =
        rosterString.splitByRegex(dayRegEx, true).filter{ it.isNotBlank() }



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
        suspend fun ofUri(uri: Uri, context: Context): RosterParser? = PdfGrabber(context, uri).getText()?.let { RosterParser(it) }


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
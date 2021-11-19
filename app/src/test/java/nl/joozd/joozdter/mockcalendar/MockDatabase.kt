package nl.joozd.joozdter.mockcalendar

import android.net.Uri
import android.provider.CalendarContract

class MockDatabase {
    private val calendarRows = ArrayList<MockCalendar>()
    private val eventRows = ArrayList<MockCalendarEvent>()

    fun addCalendar(calendar: MockCalendar){
        calendarRows.add(calendar)
    }

    fun addEvent(event: MockCalendarEvent){
        eventRows.add(event)
    }

    fun get(uri: Uri, projection: Array<String>?): List<MockCalendarValues> = when (uri){
        CalendarContract.Calendars.CONTENT_URI -> calendarRows.map{ it.getValues(projection)}
        CalendarContract.Events.CONTENT_URI -> eventRows.map { it.getValues(projection) }
        else -> throw (NoSuchElementException("Only content here is Calendar and Events"))

    }

    inner class Table(private val uri: Uri, private val projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?){
        init{
            require(sortOrder == null){ "SortOrder not supported yet" }
        }
        private val rules = selection?.let { makeSelectionRules(it, selectionArgs ?: emptyArray()) }
        private val target: List<MockCalendarEntity> = when(uri){
            CalendarContract.Calendars.CONTENT_URI -> calendarRows
            CalendarContract.Events.CONTENT_URI -> eventRows
            else -> throw (NoSuchElementException("Only content here is Calendar and Events"))
        }.let{
            if (rules == null) it else it.filter{ row ->
                rules allow row
            }
        }
        val count = target.size
        val names = projection ?: target.firstOrNull()?.columnNames?.toTypedArray()

        val selectionString = selection?.let { buildSelectionString( it, selectionArgs ?: emptyArray()) }

        operator fun get(index: Int) = target[index].getValues(projection)

        override fun equals(other: Any?): Boolean =
            if (other !is MockDatabase.Table) false
            else other.uri == uri && other.projection.contentEquals(projection) && other.selectionString == selectionString

        override fun hashCode(): Int {
            var result = uri.hashCode()
            result = 31 * result + (projection?.contentHashCode() ?: 0)
            result = 31 * result + (rules?.hashCode() ?: 0)
            result = 31 * result + target.hashCode()
            result = 31 * result + count
            result = 31 * result + (names?.contentHashCode() ?: 0)
            result = 31 * result + (selectionString?.hashCode() ?: 0)
            return result
        }
    }




        /**
     * Returns a bunch of rules. Rules that have to be AND are in sublists together.
     * Does not support parentheses  - eg. not ((A OR B) AND (C OR D))
     */
    private fun makeSelectionRules(selection: String, selectionArgs: Array<String>): RuleSet = RuleSet().apply{
        require (selection.count { it == '?'} <= selectionArgs.size) { "Not enough args for selection" }
        val selectionString = buildSelectionString(selection, selectionArgs).replace("\\(|\\)".toRegex(), " ")

        selectionString.split("OR").forEach{
            addRulesCombinationFromText(it)
        }
    }

    private fun buildSelectionString(selection: String, selectionArgs: Array<String>): String{
        var selectionWIP = selection
        val argsWIP = selectionArgs.toMutableList()
        while ('?' in selectionWIP){
            selectionWIP = selectionWIP.replaceFirst("?", argsWIP.removeFirst())
        }
        return selectionWIP
    }

    private class RuleSet {
        private var rules = ArrayList<List<Rule>>()

        fun addRulesCombinationFromText(text: String) {
            require("OR" !in text) { "Do not parse rules with \"OR\" ( got $text)" }
            addRulesCombination(text.split(" ").map {
                parseSingleRule(it)
            })
        }

        infix fun allow(row: MockCalendarEntity): Boolean =
            rules.any{ ruleSet ->
                ruleSet.all { it.check(row)}
            }


        private fun addRulesCombination(newRules: List<Rule>) {
            rules.add(newRules)
        }


        fun interface Rule {
            fun check(row: MockCalendarEntity): Boolean
        }

        companion object {
            private fun parseSingleRule(ruleText: String): RuleSet.Rule {
                val words = ruleText.removeDoubleSpaces().trim().split(" ")
                require(words.size == 2) { "Cannot parse " }
                val key = words[0]
                val value = words[2]
                return when (words[1]) {
                    "=" -> Rule { row -> row[key] == value }
                    ">=" -> Rule { row -> row[key].toLong() >= value.toLong() }
                    "<=" -> Rule { row -> row[key].toLong() <= value.toLong() }
                    else -> error("Could not parse $ruleText")
                }
            }

            private fun String.removeDoubleSpaces(): String =
                if ("  " in this)
                    this.replace("  ", " ").removeDoubleSpaces()
                else this
        }
    }


}
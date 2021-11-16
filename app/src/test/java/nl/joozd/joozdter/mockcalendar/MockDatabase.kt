package nl.joozd.joozdter.mockcalendar

import android.net.Uri
import android.provider.CalendarContract

class MockDatabase() {
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



    inner class Table(uri: Uri, private val projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?){
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

        operator fun get(index: Int) = target[index].getValues(projection)
    }

    /**
     * Returns a bunch of rules. Rules that have to be AND are in sublists together.
     */
    private fun makeSelectionRules(selection: String, selectionArgs: Array<String>): RuleSet{
        require (selection.count { it == '?'} <= selectionArgs.size) { "Not enough args for selection" }
        var selectionWIP = selection.replace("(|)".toRegex(), " ")
        val argsWIP = selectionArgs.toMutableList()
        val ruleSet = RuleSet()
        while ('?' in selectionWIP){
            selectionWIP = selectionWIP.replaceFirst("?", argsWIP.removeFirst())
        }
        selectionWIP.split("OR").forEach{
            ruleSet.addRulesCombinationFromText(it)
        }
        return ruleSet
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
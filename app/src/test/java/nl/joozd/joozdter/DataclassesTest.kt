package nl.joozd.joozdter

import nl.joozd.joozdter.parser.RosterParser
import org.junit.Test

class DataclassesTest {
    @Test
    fun testDataClasses(){
        val pages = PdfGrabber("j1").read()
        assert(pages.isNotEmpty())

        val days = RosterParser(pages).parse()
        require(days != null) { "days is null. Test failed."}
        val events = days.map{ it.events}.flatten()
        val roomEvents = events.map{it.toRoomEvent()}
        val reparsedEvents = roomEvents.map{ it.toEvent() }
        println(events.joinToString("\n"))
        println("*-*-*-*-*-*-*-*-*-*-*-")
        println(reparsedEvents.joinToString("\n"))
        println("*-*-*-*-*-*-*-*-*-*-*-")
        println("*-*-*-*-*-*-*-*-*-*-*-")
        println(events.filter { e -> reparsedEvents.none { it == e }})
    }
}
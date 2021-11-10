package nl.joozd.joozdter

import nl.joozd.joozdter.data.EventTypes
import nl.joozd.joozdter.data.events.*
import nl.joozd.joozdter.parser.RosterParser
import org.junit.Test
import java.time.Instant

class DataclassesTest {
    @Test
    fun testDataClasses(){
        val pages = PdfGrabber("j1").read()
        assert(pages.isNotEmpty())

        val days = RosterParser(pages).parse()
        require(days != null) { "days is null. Test failed."}
        val events = days.map{ it.events}.flatten()
    }

    @Test
    fun testEventTypes(){
        val he = HotelEvent("naam", Instant.now(), Instant.now().plusSeconds(600), "test") as Event
        val fe = Event("iets", EventTypes.FLIGHT, Instant.now(), Instant.now().plusSeconds(600), "andere test") as Event
        val cie = CheckinEvent("iets", Instant.now(), Instant.now().plusSeconds(600), "andere test") as Event
        val coe = CheckOutEvent("iets", Instant.now(), Instant.now().plusSeconds(600), "andere test") as Event
        val pue = PickupEvent("iets", Instant.now(), Instant.now().plusSeconds(600), "andere test") as Event

        assert (he is CompleteableEvent)
        assert (cie is CompleteableEvent)
        assert (coe is CompleteableEvent)
        assert (pue is CompleteableEvent)
        assert (fe !is CompleteableEvent)

        fun buildList(vararg ee: Event): List<Event>{
            return ee.toList()
        }
        val list = buildList(he, fe, cie, coe, pue)
        assert(list.filterIsInstance<CompleteableEvent>().size == 4)
        println("done")
    }
}
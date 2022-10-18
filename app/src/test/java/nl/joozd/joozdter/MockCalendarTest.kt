package nl.joozd.joozdter

import nl.joozd.joozdter.calendar.CalendarDescriptorOld
import nl.joozd.joozdter.mockcalendar.MockCalendar
import nl.joozd.joozdter.mockcalendar.MockDatabase
import nl.joozd.joozdter.mockdata.MockCalendarData
import org.junit.Before

class MockCalendarTest {
    val database = MockDatabase()

    @Before
    fun setUp(){
        database.addCalendar(MockCalendar.of(CalendarDescriptorOld(2, "Mock Calendar", "Joozd zijn account", "joozd", "Test kalender")))
        MockCalendarData.allEvents.forEach {
            database.addEvent(it)
        }
    }
}
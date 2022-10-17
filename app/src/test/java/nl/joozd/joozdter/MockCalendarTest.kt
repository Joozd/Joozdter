package nl.joozd.joozdter

import nl.joozd.joozdter.calendar.CalendarDescriptor
import nl.joozd.joozdter.mockcalendar.MockCalendar
import nl.joozd.joozdter.mockcalendar.MockDatabase
import nl.joozd.joozdter.mockdata.MockCalendarData
import org.junit.Before

class MockCalendarTest {
    val database = MockDatabase()

    @Before
    fun setUp(){
        database.addCalendar(MockCalendar.of(CalendarDescriptor(2, "Mock Calendar", "Joozd zijn account", "joozd", "Test kalender")))
        MockCalendarData.allEvents.forEach {
            database.addEvent(it)
        }
    }
}
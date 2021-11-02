package nl.joozd.joozdter

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class FunctionTest {
    @Test
    fun timeParser(){
        val timeFormatter = DateTimeFormatter.ofPattern("HHmm")
        val t = "0330"
        val lt = LocalTime.parse(t, timeFormatter)
        assertEquals(lt, LocalTime.of(3,30))
        println("ok")
    }
}

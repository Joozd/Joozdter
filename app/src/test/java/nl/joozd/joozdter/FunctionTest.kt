package nl.joozd.joozdter

import nl.joozd.joozdter.utils.extensions.words
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

class FunctionTest {
    @Test
    fun timeParser(){
        val timeFormatter = DateTimeFormatter.ofPattern("HHmm")
        val t = "0330"
        val lt = LocalTime.parse(t, timeFormatter)
        assertEquals(lt, LocalTime.of(3,30))
    }

    @Test
    fun hotelsTest(){
        val hotelsString = "H1 AC Hotel Valencia by Marriott,\n" +
                "Valencia\n" +
                "+34963317000\n" +
                "H2 NH Firenze, Florence 0039 055 2770\n" +
                "H3 Novotel Hannover, Hannover 0049 51139040\n" +
                "H4 AEMILIA HOTEL BOLOGNA, Bologna 0039 0513940311\n" +
                "H5 Movenpick Hotel, Geneva 0041227171111"
        val hl = buildHotelsLegend(hotelsString)
        assert(hl.keys.size == 5)
    }

    }

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

        return hotelLines.map { it.words().let{
            it.first() to it.drop(1).joinToString(" ")
        }}.toMap()


}

package nl.joozd.joozdter.data

import androidx.annotation.Keep
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId

@Keep
data class Day(val date: String, val events: List<Event>){
    val dateAsLocalDate: LocalDate by lazy{
        LocalDate.parse(date)
    }
    val dateStartAsInstant: Instant by lazy {
        dateAsLocalDate.atStartOfDay().atZone(ZoneId.of("UTC")).toInstant()
    }
    val dateEndAsInstant: Instant by lazy {
        dateAsLocalDate.atStartOfDay().plusDays(1).atZone(ZoneId.of("UTC"))
            .toInstant()
    }

}

// date: "2019-07-05"
package nl.joozd.joozdter.utils

import java.time.*

class InstantRange(override val start: Instant, override val endInclusive: Instant): ClosedRange<Instant> {
    constructor(range: ClosedRange<Instant>): this(range.start, range.endInclusive)
    val startDate: LocalDate = LocalDateTime.ofInstant(start, ZoneOffset.UTC).toLocalDate()
    val endDate: LocalDate = LocalDateTime.ofInstant(endInclusive, ZoneOffset.UTC).toLocalDate()

    val dates: Iterable<LocalDate>
        get() = DateProgression(startDate, endDate)

    val datesAsInstants: List<Instant>
        get() = dates.map{it.atStartOfDay().toInstant(ZoneOffset.UTC)}

    override fun toString() = "InstantRange ($start .. $endInclusive"



    private class DateIterator(val startDate: LocalDate,
                               val endDateInclusive: LocalDate,
                               val stepDays: Long): Iterator<LocalDate> {
        private var currentDate = startDate
        override fun hasNext() = currentDate <= endDateInclusive
        override fun next(): LocalDate {
            val next = currentDate
            currentDate = currentDate.plusDays(stepDays)
            return next
        }
    }

    private class DateProgression(override val start: LocalDate,
                                  override val endInclusive: LocalDate,
                                  val stepDays: Long = 1) :
        Iterable<LocalDate>, ClosedRange<LocalDate> {

        override fun iterator(): Iterator<LocalDate> =
            DateIterator(start, endInclusive, stepDays)

        infix fun step(days: Long) = DateProgression(start, endInclusive, days)

    }
}



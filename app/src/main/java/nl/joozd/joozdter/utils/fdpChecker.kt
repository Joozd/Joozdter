package nl.joozd.joozdter.utils

import java.time.*

/**
 * fdpChecker will check FDP margin for two times.
 * @param start: check-in time
 * @param end: check-out time
 * @param sectors: Amount of sectors on this duty
 * @param zoneId: timezone for local time
 */

fun fdpChecker(start: Instant, end: Instant, sectors: Int, timeZone: ZoneId ): Duration{
    val startTime = start.atZone(timeZone).toLocalDateTime()
    val duration = Duration.between(start, end)
    val uncorrectedMax: Duration = when(startTime.toLocalTime()){
        in (LocalTime.of(6,0)..LocalTime.of(13,29)) ->      Duration.ofHours(13)

        in (LocalTime.of(13,30)..LocalTime.of(13,59)) ->    Duration.ofHours(12) + Duration.ofMinutes(45)

        in (LocalTime.of(14,0)..LocalTime.of(14,29)) ->     Duration.ofHours(12) + Duration.ofMinutes(30)

        in (LocalTime.of(14,30)..LocalTime.of(14,59)) ->    Duration.ofHours(12) + Duration.ofMinutes(15)

        in (LocalTime.of(15,0)..LocalTime.of(15,29)) ->     Duration.ofHours(12) + Duration.ofMinutes(0)

        in (LocalTime.of(15,30)..LocalTime.of(15,59)) ->    Duration.ofHours(11) + Duration.ofMinutes(45)

        in (LocalTime.of(16,0)..LocalTime.of(16,29)) ->     Duration.ofHours(11) + Duration.ofMinutes(30)

        in (LocalTime.of(16,30)..LocalTime.of(16,59)) ->    Duration.ofHours(11) + Duration.ofMinutes(15)

        in (LocalTime.of(5,0)..LocalTime.of(5,14)) ->       Duration.ofHours(12) + Duration.ofMinutes(0)

        in (LocalTime.of(5,15)..LocalTime.of(5,29)) ->      Duration.ofHours(12) + Duration.ofMinutes(15)

        in (LocalTime.of(5,30)..LocalTime.of(5,44)) ->      Duration.ofHours(12) + Duration.ofMinutes(30)

        in (LocalTime.of(5,45)..LocalTime.of(5,59)) ->      Duration.ofHours(12) + Duration.ofMinutes(45)

        else -> Duration.ofHours(11)
    }
    val correctionForSectors = when (sectors) {
        0, 1, 2 -> Duration.ofMinutes(0)
        else -> Duration.ofMinutes(30L * (sectors - 2))
    }
    return maxOf(Duration.ofHours(9), uncorrectedMax - correctionForSectors)












}
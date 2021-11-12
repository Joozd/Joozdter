package nl.joozd.joozdter.exceptions

import java.lang.RuntimeException

class NoCalendarSelectedException(reason: String): RuntimeException(reason)
package nl.joozd.joozdter.exceptions

import java.lang.RuntimeException

class NotARosterException(reason: String): RuntimeException(reason)
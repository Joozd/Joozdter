package nl.joozd.joozdter.exceptions

import java.lang.RuntimeException

class BadFileTypeException(reason: String): RuntimeException(reason) {
}
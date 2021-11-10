package nl.joozd.joozdter.data.events

/**
 * Regular expressions used in this package
 */
private const val TIME = """\d{4}"""


internal val leaveRegex =
    """(?:L[A-Z]+|SL[A-Z]+|ALC|IGC)\s(?:R\s)?[A-Z]{3}\s(${TIME})\s(${TIME})""".toRegex()
internal val checkInRegex = """(?:C/I|S/U)\s(?:[A-Z]{3})\s(\d{4})""".toRegex()
internal val checkOutRegex = """C/O\s(\d{4})\s(?:[A-Z]{3}).*""".toRegex()
internal val trainingRegex = """T[A-Z]+.*""".toRegex()
internal val standbyRegex = """(?:RE[A-Z0-9]+|WTV)\s[A-Z]{3}\s(${TIME})\s(${TIME}).*""".toRegex()
internal val dayOverRegex = """X""".toRegex()

//results: [0] = whole line, [1] = flightnumber, [2] = orig, [3] = tOut, [4] = tIn, [5] = dest, [6] = extra info
internal val flightRegex =
    """((?:KL|WA)\s?\d{2,5})\s([A-Z]{3})\s(${TIME})\s(${TIME})\s([A-Z]{3})(.*)""".toRegex()

internal val hotelRegex = """(H\d+)\s([A-Z]{3})""".toRegex()
internal val clickRegex = """CLICK\s[A-Z]{3}\s(${TIME})\s(${TIME})""".toRegex()
internal val pickupRegex = """Pick Up (${TIME})""".toRegex()
internal val extraMessageRegex = """(To c/m:.*)""".toRegex(RegexOption.DOT_MATCHES_ALL)
internal val fdpRegex = """\[FDP (\d\d):(\d\d)]""".toRegex()


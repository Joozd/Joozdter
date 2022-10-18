package nl.joozd.joozdter.data.sharedPrefs

/**
 * sharedprefs wrapper
 * before first use, call init(context) to bind context to the class
 */

object JoozdterPrefs: JoozdPreferences() {
    override val preferencesFileKey: String = "JOOZDTER_PREFS"

    // Preference keys
    private const val FIRST_TIME = "FIRST_TIME"
    private const val VERSION = "VERSION"
    private const val PICKED_CALENDAR = "PICKED_CALENDAR"
    private const val LEAVE = "LEAVE"
    private const val TAXI = "TAXI"
    private const val CHECK_IN = "CHECK_IN"
    private const val CHECK_OUT = "CHECK_OUT"
    private const val FLIGHT = "FLIGHT"
    private const val HOTEL = "HOTEL"
    private const val STANDBY = "STANDBY"
    private const val TRAINING = "TRAINING"
    private const val SIMULATOR = "SIMULATOR"
    private const val OTHER = "OTHER"
    private const val SHOW_DECODED_CODES = "SHOW_DECODED_CODES"

    // Constants
    private const val NO_CALENDAR_PICKED = "\n\nNO_CALENDAR_PICKED\n\n"

    // Preferences
    val firstTime by SharedPreferenceDelegate(FIRST_TIME, true)
    val version by SharedPreferenceDelegate(VERSION, 0)
    val pickedCalendar by SharedPreferenceDelegate(PICKED_CALENDAR, NO_CALENDAR_PICKED)
    val leave by SharedPreferenceDelegate(LEAVE, true)
    val taxi by SharedPreferenceDelegate(TAXI, true)
    val checkIn by SharedPreferenceDelegate(CHECK_IN, true)
    val checkOut by SharedPreferenceDelegate(CHECK_OUT, true)
    val flight by SharedPreferenceDelegate(FLIGHT, true)
    val hotel by SharedPreferenceDelegate(HOTEL, true)
    val standby by SharedPreferenceDelegate(STANDBY, true)
    val training by SharedPreferenceDelegate(TRAINING, true)
    val simulator by SharedPreferenceDelegate(SIMULATOR, true)
    val otherDuty by SharedPreferenceDelegate(OTHER, true)
    val decodeCodes by SharedPreferenceDelegate(SHOW_DECODED_CODES, 0)
}
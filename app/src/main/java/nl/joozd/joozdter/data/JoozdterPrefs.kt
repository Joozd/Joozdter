package nl.joozd.joozdter.data

import android.content.Context
import android.content.SharedPreferences
import nl.joozd.joozdter.App
import nl.joozd.joozdter.R

/**
 * sharedprefs wrapper
 * before first use, call init(context) to bind context to the class
 */

object JoozdterPrefs {
    const val CURRENTVERSION = 3 // version of JoozdterPrefs, only update if changing something in this companion object
    const val PICKED_CALENDAR = "pickedCalendar"


    private val context: Context = App.instance

    val sharedPrefs: SharedPreferences = context.getSharedPreferences(
        context.getString(R.string.preference_file_key), Context.MODE_PRIVATE)

    var firstTime: Boolean by SharedPrefsHelper(sharedPrefs,true)

    var version: Int by SharedPrefsHelper(sharedPrefs,0)


    var pickedCalendar: String?
        get() = sharedPrefs.getString(PICKED_CALENDAR,null)
        set(v) = with(sharedPrefs.edit()) {
            putString(PICKED_CALENDAR, v)
            apply()
        }

    var leave: Boolean by SharedPrefsHelper(sharedPrefs,true)


    var taxi: Boolean by SharedPrefsHelper(sharedPrefs,true)

    var checkIn: Boolean by SharedPrefsHelper(sharedPrefs,true)

    var checkOut: Boolean by SharedPrefsHelper(sharedPrefs,true)

    var flight: Boolean by SharedPrefsHelper(sharedPrefs,true)

    var hotel: Boolean by SharedPrefsHelper(sharedPrefs,true)


    var standby: Boolean by SharedPrefsHelper(sharedPrefs,true)

    var simulator: Boolean by SharedPrefsHelper(sharedPrefs,true)

    var actualSimulator: Boolean by SharedPrefsHelper(sharedPrefs,true)

    var other: Boolean by SharedPrefsHelper(sharedPrefs,true)

    var preferedLayout: Int by SharedPrefsHelper(sharedPrefs,0)

    private fun checkVersion() = version == CURRENTVERSION

    fun updateVersion() {
        version = CURRENTVERSION
    }
}
package nl.joozd.joozdter.data

import android.content.Context
import nl.joozd.joozdter.R

/**
 * sharedprefs wrapper
 * before first use, call init(context) to bind context to the class
 */

class JoozdterPrefs {
    companion object{
        const val CURRENTVERSION = 3
        const val FILLED = "filled"
        const val VERSIONCHECK = "version"
        const val FIRST_TIME = "firstTime"
        // const val SHARE_NAME = "shareName"
        const val PICKED_CALENDAR = "pickedCalendar"
        const val PREFERED_LAYOUT = "preferedLayout"
        const val SHOW_LEAVE = "leave"
        const val SHOW_TAXI = "taxi"
        const val SHOW_STANDBY = "standby"
        const val SHOW_SIM = "simulator"
        const val SHOW_ACTUALSIM = "actualSimulator"
        const val SHOW_OTHER_DUTY = "other"
        const val SHOW_CHECKIN = "checkIn"
        const val SHOW_CHECKOUT = "checkOut"
        const val SHOW_FLIGHT = "flight"
        // const val SHOW_CLICK = "click"
        const val SHOW_HOTEL = "hotel"
    }
    var ctx: Context? = null

    val sharedPref by lazy{
        requireNotNull(ctx) { "JoozdterPrefs not initialized properly, context == null" }
        ctx!!.getSharedPreferences(
        ctx!!.getString(R.string.preference_file_key), Context.MODE_PRIVATE)
    }

    val filled: Boolean
        get() = sharedPref.getBoolean(FILLED, false)

    var firstTime: Boolean
        get() = sharedPref.getBoolean(FIRST_TIME, true)
        set(v) = with(sharedPref.edit()) {
            putBoolean(FIRST_TIME, v)
            apply()
        }

    var version: Int
        get() = sharedPref.getInt(VERSIONCHECK, 0)
        set(v) = with(sharedPref.edit()) {
            putInt(VERSIONCHECK, v)
            apply()
        }


    var pickedCalendar: String?
        get() = sharedPref.getString(PICKED_CALENDAR,null)
        set(v) = with(sharedPref.edit()) {
            putString(PICKED_CALENDAR, v)
            apply()
        }

    var showLeave: Boolean
        get() = sharedPref.getBoolean(SHOW_LEAVE, true)
        set(v) = with(sharedPref.edit()) {
            putBoolean(SHOW_LEAVE, v)
            apply()
        }

    var showTaxi: Boolean
        get() = sharedPref.getBoolean(SHOW_TAXI, true)
        set(v) = with(sharedPref.edit()) {
            putBoolean(SHOW_TAXI, v)
            apply()
        }

    var showCheckIn: Boolean
        get() = sharedPref.getBoolean(SHOW_CHECKIN, true)
        set(v) = with(sharedPref.edit()) {
            putBoolean(SHOW_CHECKIN, v)
            apply()
        }

    var showCheckOut: Boolean
        get() = sharedPref.getBoolean(SHOW_CHECKOUT, true)
        set(v) = with(sharedPref.edit()) {
            putBoolean(SHOW_CHECKOUT, v)
            apply()
        }

    var showFlight: Boolean
        get() = sharedPref.getBoolean(SHOW_FLIGHT, true)
        set(v) = with(sharedPref.edit()) {
            putBoolean(SHOW_FLIGHT, v)
            apply()
        }

    var showHotel: Boolean
        get() = sharedPref.getBoolean(SHOW_HOTEL, true)
        set(v) = with(sharedPref.edit()) {
            putBoolean(SHOW_HOTEL, v)
            apply()
        }


    var showStandBy: Boolean
        get() = sharedPref.getBoolean(SHOW_STANDBY, true)
        set(v) = with(sharedPref.edit()) {
            putBoolean(SHOW_STANDBY, v)
            apply()
        }

    var showSim: Boolean
        get() = sharedPref.getBoolean(SHOW_SIM, true)
        set(v) = with(sharedPref.edit()) {
            putBoolean(SHOW_SIM, v)
            apply()
        }

    var showActualSim: Boolean
        get() = sharedPref.getBoolean(SHOW_ACTUALSIM, true)
        set(v) = with(sharedPref.edit()) {
            putBoolean(SHOW_ACTUALSIM, v)
            apply()
        }

    var showOther: Boolean
        get() = sharedPref.getBoolean(SHOW_OTHER_DUTY, true)
        set(v) = with(sharedPref.edit()) {
            putBoolean(SHOW_OTHER_DUTY, v)
            apply()
        }

    var preferedLayout: Int
        get() = sharedPref.getInt(PREFERED_LAYOUT, 0)
        set(v) = with(sharedPref.edit()) {
            putInt(PREFERED_LAYOUT, v)
            apply()
        }

    /**
     * Binds context to this class and fills fields with default values if not willed yet
     * @param context: Context of SharedPreferences to load
     */
    fun init(context: Context){
        ctx = context

        // Check if sharedPrefs are filled( ie if not first run), if not fill with default values //
        if (!filled) {
            with(sharedPref.edit()) {
                putBoolean(FILLED, true)
                // putBoolean(SharedPrefKeys.SHARE_NAME, true)
                putBoolean(SHOW_LEAVE, true)
                putBoolean(SHOW_TAXI, true)
                putBoolean(SHOW_STANDBY, true)
                putBoolean(SHOW_SIM, true)
                putBoolean(SHOW_ACTUALSIM, true)
                putBoolean(SHOW_OTHER_DUTY, true)
                putBoolean(SHOW_CHECKIN, true)
                putBoolean(SHOW_CHECKOUT, true)
                putBoolean(SHOW_FLIGHT, true)
                // putBoolean(SHOW_CLICK, true)
                putBoolean(SHOW_HOTEL, true)
                putInt(PREFERED_LAYOUT, JoozdlogLayoutOptions.FULL)

                apply()
            }
        }

    }

    fun checkVersion() = version == CURRENTVERSION

    fun updateVersion() {
        version = CURRENTVERSION
    }
}
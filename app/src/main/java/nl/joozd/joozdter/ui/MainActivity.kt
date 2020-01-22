package nl.joozd.joozdter.ui

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import nl.joozd.joozdter.R
import nl.joozd.joozdter.calendar.CalendarDescriptor
import nl.joozd.joozdter.calendar.CalendarHandler
import nl.joozd.joozdter.data.SharedPrefKeys
import nl.joozd.joozdter.ui.adapters.CalendarPickerAdapter
import nl.joozd.joozdter.ui.fragments.NewUserFragment
import org.jetbrains.anko.alert
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.okButton

class MainActivity : AppCompatActivity() {

    class ShowMenuListener(private val f: () -> Unit){
        fun go(){
            f()
        }
    }

    private lateinit var sharedPref: SharedPreferences
    private lateinit var calendarHandler: CalendarHandler
    private val calendarPickerAdapter = CalendarPickerAdapter(emptyList()) { getCalendar(it) }
    private var showHalpListener: ShowMenuListener? = null

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        /*R.id.menu_rebuild -> {
            doAsync {
                allFlights = Comms().rebuildFromServer()
                runOnUiThread {
                    flightDb.clearDB()
                    flightDb.saveFlights(allFlights)
                    toast("Database rebuilt. please restart ofzo")
                }
            }
            true
        } */
        R.id.menu_halp -> {
            showHalpListener?.go()
            true
        }
        else -> false
    }

    override fun onRequestPermissionsResult(requestCode : Int ,
                                            permissions: Array<String>,
                                            grantResults: IntArray){
        recreate()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPref = this.getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )
        val newUserFragment = NewUserFragment()

        showHalpListener = ShowMenuListener {
            supportFragmentManager.beginTransaction()
                .add(R.id.mainActivityLayout, newUserFragment)
                .addToBackStack(null)
                .commit()
        }


        // Check if sharedPrefs are filled( ie if not first run), if not fill with default values //
        val sharedPrefsNotFilled: Boolean =
            sharedPref.getBoolean(SharedPrefKeys.NOT_FILLED_YET, true)
        if (sharedPrefsNotFilled) {
            with(sharedPref.edit()) {
                putBoolean(SharedPrefKeys.NOT_FILLED_YET, false)
                putBoolean(SharedPrefKeys.SHARE_NAME, true)
                putBoolean(SharedPrefKeys.SHOW_FREE_TIME, true)
                putBoolean(SharedPrefKeys.SHOW_HOTEL, true)
                putBoolean(SharedPrefKeys.SHOW_TAXI, true)
                putBoolean(SharedPrefKeys.SHOW_FLIGHT_DAY, true)
                putBoolean(SharedPrefKeys.SHOW_FLIGHTS, true)
                putBoolean(SharedPrefKeys.SHOW_OTHER, true)
                putBoolean(SharedPrefKeys.SHOW_SIM_BRIEFING, true)
                putBoolean(SharedPrefKeys.SHOW_SIM_SESSION, true)
                apply()
            }
        }

        setContentView(R.layout.activity_main)

        if (sharedPref.getBoolean("firstTime", true)) {
            supportFragmentManager.beginTransaction()
                .add(R.id.mainActivityLayout, newUserFragment)
                .addToBackStack(null)
                .commit()
            with(sharedPref.edit()) {
                putBoolean("firstTime", false)
                apply()
            }
        } else {
            alert(getString(R.string.youCanClose)) {
                okButton {}
            }.show()
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR), 0
            )
        } else {


            calendarPickerAdapter.pickCalendar(
                sharedPref.getString(
                    SharedPrefKeys.PICKED_CALENDAR,
                    null
                )
            )
            calendarPicker.layoutManager = LinearLayoutManager(this)
            calendarPicker.adapter = calendarPickerAdapter

            calendarHandler = CalendarHandler(this@MainActivity)
            calendarHandler.onInit = CalendarHandler.OnInit {
                runOnUiThread {
                    calendarPickerAdapter.updateData(calendarHandler.calendarsList)
                    pickedCalendarText.text = calendarHandler.findCalendarByName(
                        sharedPref.getString(
                            SharedPrefKeys.PICKED_CALENDAR,
                            "NOTHINGTOSEEHEREDFSDFKAGFAKGAKFGNDKAFG"
                        )
                    )?.displayName ?: ""
                }
            }
            doAsync {
                calendarHandler.initialize()
            }
        }

        // set known values to switches, defvalue should not trigger as all set at onCreate() if not done yet
        shareNameSwitch.isChecked = sharedPref.getBoolean(SharedPrefKeys.SHARE_NAME, false)
        daysOffswitch.isChecked = sharedPref.getBoolean(SharedPrefKeys.SHOW_FREE_TIME, false)
        hotelSwitch.isChecked = sharedPref.getBoolean(SharedPrefKeys.SHOW_HOTEL, false)
        taxiSwitch.isChecked = sharedPref.getBoolean(SharedPrefKeys.SHOW_TAXI, false)
        flightDaySwitch.isChecked = sharedPref.getBoolean(SharedPrefKeys.SHOW_FLIGHT_DAY, false)
        flightsSwitch.isChecked = sharedPref.getBoolean(SharedPrefKeys.SHOW_FLIGHTS, false)
        otherSwitch.isChecked = sharedPref.getBoolean(SharedPrefKeys.SHOW_OTHER, false)
        simBriefingSwitch.isChecked = sharedPref.getBoolean(SharedPrefKeys.SHOW_SIM_BRIEFING, false)
        simActualSwitch.isChecked = sharedPref.getBoolean(SharedPrefKeys.SHOW_SIM_SESSION, false)

        shareNameSwitch.setOnCheckedChangeListener { _, isChecked ->
            with(sharedPref.edit()) {
                putBoolean(SharedPrefKeys.SHARE_NAME, isChecked)
                apply()
            }
        }

        daysOffswitch.setOnCheckedChangeListener { _, isChecked ->
            with(sharedPref.edit()) {
                putBoolean(SharedPrefKeys.SHOW_FREE_TIME, isChecked)
                apply()
            }
        }

        hotelSwitch.setOnCheckedChangeListener { _, isChecked ->
            with(sharedPref.edit()) {
                putBoolean(SharedPrefKeys.SHOW_HOTEL, isChecked)
                apply()
            }
        }

        taxiSwitch.setOnCheckedChangeListener { _, isChecked ->
            with(sharedPref.edit()) {
                putBoolean(SharedPrefKeys.SHOW_TAXI, isChecked)
                apply()
            }
        }

        flightDaySwitch.setOnCheckedChangeListener { _, isChecked ->

            with(sharedPref.edit()) {
                putBoolean(SharedPrefKeys.SHOW_FLIGHT_DAY, isChecked)
                apply()
            }
        }

        flightsSwitch.setOnCheckedChangeListener { _, isChecked ->
            with(sharedPref.edit()) {
                putBoolean(SharedPrefKeys.SHOW_FLIGHTS, isChecked)
                apply()
            }
        }

        otherSwitch.setOnCheckedChangeListener { _, isChecked ->
            with(sharedPref.edit()) {
                putBoolean(SharedPrefKeys.SHOW_OTHER, isChecked)
                apply()
            }
        }

        simBriefingSwitch.setOnCheckedChangeListener { _, isChecked ->
            with(sharedPref.edit()) {
                putBoolean(SharedPrefKeys.SHOW_SIM_BRIEFING, isChecked)
                apply()
            }
        }

        simActualSwitch.setOnCheckedChangeListener { _, isChecked ->
            with(sharedPref.edit()) {
                putBoolean(SharedPrefKeys.SHOW_SIM_SESSION, isChecked)
                apply()
            }
        }


    }

    private fun getCalendar(calendar: CalendarDescriptor){
        pickedCalendarText.text = calendar.displayName
        with (sharedPref.edit()) {
            putString(SharedPrefKeys.PICKED_CALENDAR, calendar.name)
            apply()
        }
        calendarPickerAdapter.pickCalendar(calendar.name)
    }
}

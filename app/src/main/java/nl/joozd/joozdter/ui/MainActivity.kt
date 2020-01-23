package nl.joozd.joozdter.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import nl.joozd.joozdter.R
import nl.joozd.joozdter.calendar.CalendarDescriptor
import nl.joozd.joozdter.calendar.CalendarHandler
import nl.joozd.joozdter.data.JoozdterPrefs
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

    private val prefs = JoozdterPrefs()
    private lateinit var calendarHandler: CalendarHandler
    private val calendarPickerAdapter = CalendarPickerAdapter(emptyList()) { getCalendar(it) }
    private var showHalpListener: ShowMenuListener? = null

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
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
        prefs.init(this)

        val newUserFragment = NewUserFragment()

        showHalpListener = ShowMenuListener {
            supportFragmentManager.beginTransaction()
                .add(R.id.mainActivityLayout, newUserFragment)
                .addToBackStack(null)
                .commit()
        }

        setContentView(R.layout.activity_main)

        // show splash screen on first run
        if (prefs.firstTime) {
            supportFragmentManager.beginTransaction()
                .add(R.id.mainActivityLayout, newUserFragment)
                .addToBackStack(null)
                .commit()
            prefs.firstTime = false
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


            calendarPickerAdapter.pickCalendar(prefs.pickedCalendar)
            calendarPicker.layoutManager = LinearLayoutManager(this)
            calendarPicker.adapter = calendarPickerAdapter

            calendarHandler = CalendarHandler(this@MainActivity)
            calendarHandler.onInit = CalendarHandler.OnInit {
                runOnUiThread {
                    calendarPickerAdapter.updateData(calendarHandler.calendarsList)
                    pickedCalendarText.text = calendarHandler.findCalendarByName(prefs.pickedCalendar)?.displayName ?: ""
                }
            }
            doAsync {
                calendarHandler.initialize()
            }
        }

        daysOffswitch.isChecked = prefs.showLeave
        taxiSwitch.isChecked = prefs.showTaxi
        checkInSwitch.isChecked = prefs.showCheckIn
        checkOutSwitch.isChecked = prefs.showCheckOut
        flightsSwitch.isChecked = prefs.showFlight
        hotelSwitch.isChecked = prefs.showHotel
        standbySwitch.isChecked = prefs.showStandBy
        simBriefingSwitch.isChecked = prefs.showSim
        simActualSwitch.isChecked = prefs.showActualSim
        otherSwitch.isChecked = prefs.showOther

        /*
        shareNameSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefss.shareName = isChecked
        }
        */

        daysOffswitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.showLeave = isChecked
        }

        hotelSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.showHotel = isChecked
        }

        taxiSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.showTaxi = isChecked
        }

        checkInSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) alert("Make sure to check emailed roster for notes, as they are usually added to CheckIn activity")
            prefs.showCheckIn = isChecked
        }

        checkOutSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.showCheckOut = isChecked
        }

        flightsSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.showFlight = isChecked
        }

        otherSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.showOther = isChecked
        }

        simBriefingSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.showSim = isChecked
        }

        simActualSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.showActualSim = isChecked
        }

        standbySwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.showStandBy = isChecked
        }

        preferedLayoutSpinner.onItemSelectedListener = object:  AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                prefs.preferedLayout = position + 1
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
        preferedLayoutSpinner.setSelection(prefs.preferedLayout-1)


    }

    private fun getCalendar(calendar: CalendarDescriptor){
        pickedCalendarText.text = calendar.displayName
        prefs.pickedCalendar = calendar.name
        calendarPickerAdapter.pickCalendar(calendar.name)
    }
}

package nl.joozd.joozdter.ui.mainActivity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.commit
import androidx.recyclerview.widget.LinearLayoutManager
import nl.joozd.joozdter.R
import nl.joozd.joozdter.calendar.CalendarHandler
import nl.joozd.joozdter.data.JoozdterPrefs
import nl.joozd.joozdter.databinding.ActivityMainBinding
import nl.joozd.joozdter.utils.extensions.bypassedIsChecked
import nl.joozd.joozdter.utils.extensions.setInterceptedOnCheckedChangedListener
import nl.joozd.joozdter.ui.adapters.CalendarPickerAdapter
import nl.joozd.joozdter.ui.fragments.NewUserFragment
import nl.joozd.joozdter.ui.utils.JoozdterActivity

class MainActivity : JoozdterActivity() {
    private val viewModel: MainActivityViewModel by viewModels()

    class ShowMenuListener(private val f: () -> Unit){
        fun go(){
            f()
        }
    }
    private lateinit var calendarHandler: CalendarHandler
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

        val newUserFragment = NewUserFragment()

        showHalpListener = ShowMenuListener {
            supportFragmentManager.beginTransaction()
                .add(R.id.mainActivityLayout, newUserFragment)
                .addToBackStack(null)
                .commit()
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR), 0
            )
        } else {

            with(ActivityMainBinding.inflate(layoutInflater)) {
                val calendarPickerAdapter = CalendarPickerAdapter(emptyList()) { viewModel.getCalendar(it) }
                calendarPicker.layoutManager = LinearLayoutManager(activity)
                calendarPicker.adapter = calendarPickerAdapter

                /**
                 * Observers:
                 */

                viewModel.calendarName.observe(activity){
                    calendarPickerAdapter.pickCalendar(it)
                }

                viewModel.foundCalendars.observe(activity){
                    calendarPickerAdapter.updateData(it)
                }

                viewModel.pickedCalendar.observe(activity){
                    it?.let { pickedCalendarText.text = it.displayName }
                }

                //toggle switches:
                viewModel.leave.observe(activity){
                    daysOffswitch.bypassedIsChecked = it
                }
                viewModel.taxi.observe(activity){
                    taxiSwitch.bypassedIsChecked = it
                }
                viewModel.checkIn.observe(activity){
                    checkInSwitch.bypassedIsChecked = it
                }
                viewModel.checkOut.observe(activity){
                    checkOutSwitch.bypassedIsChecked = it
                }
                viewModel.flight.observe(activity){
                    flightsSwitch.bypassedIsChecked = it
                }
                viewModel.hotel.observe(activity){
                    hotelSwitch.bypassedIsChecked = it
                }
                viewModel.standby.observe(activity){
                    standbySwitch.bypassedIsChecked = it
                }
                viewModel.simulator.observe(activity){
                    simBriefingSwitch.bypassedIsChecked = it
                }
                viewModel.actualSimulator.observe(activity){
                    simActualSwitch.bypassedIsChecked = it
                }
                viewModel.other.observe(activity){
                    otherSwitch.bypassedIsChecked = it
                }




                daysOffswitch.setInterceptedOnCheckedChangedListener { _, _ ->
                    viewModel.daysOffswitchClicked()
                }
                hotelSwitch.setInterceptedOnCheckedChangedListener { _, _ ->
                    viewModel.hotelSwitchClicked()
                }
                taxiSwitch.setInterceptedOnCheckedChangedListener { _, _ ->
                    viewModel.taxiSwitchClicked()
                }
                checkInSwitch.setInterceptedOnCheckedChangedListener { _, b ->
                    viewModel.checkInSwitchClicked()
                    if (!b) alert("Make sure to check emailed roster for notes, as they are usually added to CheckIn activity")
                }
                checkOutSwitch.setInterceptedOnCheckedChangedListener { _, _ ->
                    viewModel.checkOutSwitchClicked()
                }
                flightsSwitch.setInterceptedOnCheckedChangedListener { _, _ ->
                    viewModel.flightsSwitchClicked()
                }
                otherSwitch.setInterceptedOnCheckedChangedListener { _, _ ->
                    viewModel.otherSwitchClicked()
                }
                simBriefingSwitch.setInterceptedOnCheckedChangedListener { _, _ ->
                    viewModel.simBriefingSwitchClicked()
                }
                simActualSwitch.setInterceptedOnCheckedChangedListener { _, _ ->
                    viewModel.simActualSwitchClicked()
                }
                standbySwitch.setInterceptedOnCheckedChangedListener { _, _ ->
                    viewModel.standbySwitchClicked()
                }

                preferedLayoutSpinner.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            JoozdterPrefs.preferedLayout = position + 1
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {
                            // Do nothing
                        }
                    }
                preferedLayoutSpinner.setSelection(JoozdterPrefs.preferedLayout - 1)

                setContentView(root)

                // show splash screen on first run
                if (JoozdterPrefs.firstTime) {
                    supportFragmentManager.commit{
                        add(R.id.mainActivityLayout, newUserFragment)
                        addToBackStack(null)
                    }
                    JoozdterPrefs.firstTime = false
                } else {
                    alert(R.string.youCanClose)
                }
            }
        }
    }

}

package nl.joozd.joozdter.ui.mainActivity

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import nl.joozd.joozdter.R
import nl.joozd.joozdter.data.sharedPrefs.JoozdterPrefs
import nl.joozd.joozdter.databinding.ActivityMainBinding
import nl.joozd.joozdter.ui.adapters.CalendarPickerAdapter
import nl.joozd.joozdter.ui.fragments.NewUserFragment
import nl.joozd.joozdter.ui.utils.JoozdterActivity

class MainActivity : JoozdterActivity() {
    private val viewModel: MainActivityViewModel by viewModels()

    @SuppressLint("MissingPermission") // since the check is not missing...
    private val calendarPickerAdapter = CalendarPickerAdapter {
        if(checkReadWriteCalendarPermission()) {
            viewModel.setCalendar(it, this)
        }
    }

    override fun onRequestPermissionsResult(requestCode : Int ,
                                            permissions: Array<String>,
                                            grantResults: IntArray){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        recreate()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!checkReadWriteCalendarPermission()) requestReadCalendarPermission()
        else {
            with(ActivityMainBinding.inflate(layoutInflater)) {
                viewModel.fillCalendarsList()
                observeFlows()
                setOnClickListeners()
                initializeSpinner()

                calendarPicker.layoutManager = LinearLayoutManager(activity)
                calendarPicker.adapter = calendarPickerAdapter

                setContentView(root)

                // show splash screen on first run
                lifecycleScope.launch {
                    if (JoozdterPrefs.firstTime()) {
                        supportFragmentManager.commit {
                            add(R.id.mainActivityLayout, NewUserFragment())
                            addToBackStack(null)
                        }
                        JoozdterPrefs.firstTime(false)
                    } else {
                        alert(R.string.youCanClose)
                    }
                }
            }
        }
    }

    private fun ActivityMainBinding.initializeSpinner() {
        preferedLayoutSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    JoozdterPrefs.decodeCodes(position + 1)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Do nothing
                }
            }
        preferedLayoutSpinner.setSelection(JoozdterPrefs.decodeCodes.valueBlocking - 1)
    }

    private fun ActivityMainBinding.setOnClickListeners() {
        daysOffswitch.setOnClickListener {
            viewModel.daysOffswitchClicked()
        }
        hotelSwitch.setOnClickListener {
            viewModel.hotelSwitchClicked()
        }
        taxiSwitch.setOnClickListener {
            viewModel.taxiSwitchClicked()
        }
        checkInSwitch.setOnClickListener {
            viewModel.checkInSwitchClicked()
        }
        checkOutSwitch.setOnClickListener {
            viewModel.checkOutSwitchClicked()
        }
        flightsSwitch.setOnClickListener {
            viewModel.flightsSwitchClicked()
        }
        otherSwitch.setOnClickListener {
            viewModel.otherSwitchClicked()
        }
        simBriefingSwitch.setOnClickListener {
            viewModel.simBriefingSwitchClicked()
        }
        simActualSwitch.setOnClickListener {
            viewModel.simActualSwitchClicked()
        }
        standbySwitch.setOnClickListener {
            viewModel.standbySwitchClicked()
        }
    }


    private fun ActivityMainBinding.observeFlows(){
        viewModel.foundCalendarsFlow.launchCollectWhileLifecycleStateStarted{
            calendarPickerAdapter.submitList(it)
        }

        viewModel.pickedCalendarFlow.launchCollectWhileLifecycleStateStarted{ cal ->
            calendarPickerAdapter.pickCalendar(cal)
            cal?.let { pickedCalendarText.text = it.displayName }
        }

        viewModel.messagesFlow.launchCollectWhileLifecycleStateStarted{ message ->
            message?.let { alert(it){ viewModel.messageShown()} }
        }

        //toggle switches:
        viewModel.leaveFlow.launchCollectWhileLifecycleStateStarted{
            daysOffswitch.isChecked = it
        }
        viewModel.taxiFlow.launchCollectWhileLifecycleStateStarted{
            taxiSwitch.isChecked = it
        }
        viewModel.checkInFlow.launchCollectWhileLifecycleStateStarted{
            checkInSwitch.isChecked = it
        }
        viewModel.checkOutFlow.launchCollectWhileLifecycleStateStarted{
            checkOutSwitch.isChecked = it
        }
        viewModel.flightFlow.launchCollectWhileLifecycleStateStarted{
            flightsSwitch.isChecked = it
        }
        viewModel.hotelFlow.launchCollectWhileLifecycleStateStarted{
            hotelSwitch.isChecked = it
        }
        viewModel.standbyFlow.launchCollectWhileLifecycleStateStarted{
            standbySwitch.isChecked = it
        }
        viewModel.trainingFlow.launchCollectWhileLifecycleStateStarted{
            simBriefingSwitch.isChecked = it
        }
        viewModel.simulatorFlow.launchCollectWhileLifecycleStateStarted{
            simActualSwitch.isChecked = it
        }
        viewModel.otherDutyFlow.launchCollectWhileLifecycleStateStarted{
            otherSwitch.isChecked = it
        }
    }

    private fun checkReadWriteCalendarPermission() =
        ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED

    private fun requestReadCalendarPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR), 0
        )
    }

}

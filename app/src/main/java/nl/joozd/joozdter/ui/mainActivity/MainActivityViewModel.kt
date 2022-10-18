package nl.joozd.joozdter.ui.mainActivity

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.lifecycle.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import nl.joozd.joozdcalendarapi.CalendarDescriptor
import nl.joozd.joozdter.App
import nl.joozd.joozdter.calendar.CalendarRepository
import nl.joozd.joozdter.data.sharedPrefs.JoozdterPrefs
import nl.joozd.joozdter.data.sharedPrefs.SharedPreferenceDelegate
import nl.joozd.joozdter.ui.utils.JoozdterViewModel

class MainActivityViewModel: JoozdterViewModel() {
    /**
     * Observables:
     */

    private val calendarRepository = CalendarRepository()

    val foundCalendarsFlow get() = calendarRepository.calendarsFlow

    private val _messagesFlow = MutableStateFlow<String?>(null)
    val messagesFlow: StateFlow<String?> get() = _messagesFlow

    val pickedCalendarFlow = combine(foundCalendarsFlow, JoozdterPrefs.pickedCalendar.flow){ cals, name ->
        cals.firstOrNull{it.displayName == name}
    }

    //Checkboxes should observe these:
    val leaveFlow get() = JoozdterPrefs.leave.flow
    val taxiFlow get() = JoozdterPrefs.taxi.flow
    val checkInFlow get() = JoozdterPrefs.checkIn.flow
    val checkOutFlow get() = JoozdterPrefs.checkOut.flow
    val flightFlow get() = JoozdterPrefs.flight.flow
    val hotelFlow get() = JoozdterPrefs.hotel.flow
    val standbyFlow get() = JoozdterPrefs.standby.flow
    val trainingFlow get() = JoozdterPrefs.training.flow
    val simulatorFlow get() = JoozdterPrefs.simulator.flow
    val otherDutyFlow get() = JoozdterPrefs.otherDuty.flow

    /**
     * Functions to be called by onClickListeners
     */

    fun daysOffswitchClicked() {
        JoozdterPrefs.leave.toggle()
    }
    fun taxiSwitchClicked(){
        JoozdterPrefs.taxi.toggle()
    }
    fun checkInSwitchClicked(){
        viewModelScope.launch {
            val checkinWasChecked = JoozdterPrefs.checkIn()
            if (checkinWasChecked) _messagesFlow.value = "Make sure to check emailed roster for notes, as they are usually added to CheckIn activity"
            JoozdterPrefs.checkIn.toggle()
        }
    }
    fun checkOutSwitchClicked(){
        JoozdterPrefs.checkOut.toggle()
    }
    fun flightsSwitchClicked(){
        JoozdterPrefs.flight.toggle()
    }
    fun hotelSwitchClicked(){
        JoozdterPrefs.hotel.toggle()
    }
    fun standbySwitchClicked(){
        JoozdterPrefs.standby.toggle()
    }
    fun simBriefingSwitchClicked(){
        JoozdterPrefs.training.toggle()
    }
    fun simActualSwitchClicked(){
        JoozdterPrefs.simulator.toggle()
    }
    fun otherSwitchClicked(){
        JoozdterPrefs.otherDuty.toggle()
    }

    fun fillCalendarsList() = viewModelScope.launch {
        if (!App.instance.checkCalendarWritePermission()) return@launch
        else calendarRepository.updateCalendarsList()
    }

    @RequiresPermission(Manifest.permission.READ_CALENDAR)
    fun setCalendar(calendar: CalendarDescriptor, context: Context) {
        viewModelScope.launch {
            val oldCalendar = foundCalendarsFlow.value.firstOrNull { it.displayName == JoozdterPrefs.pickedCalendar() }
            // select picked calendar
            JoozdterPrefs.pickedCalendar(calendar.displayName)

            // If allowed (should be allowed), move flights from old to new calendar.
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED)
                calendarRepository.switchCalendars(oldCalendar, calendar)
        }
    }

    fun messageShown(){
        _messagesFlow.value = null
    }

    private fun SharedPreferenceDelegate.Pref<Boolean>.toggle(){
        val pref = this
        viewModelScope.launch {
            pref(!pref())
        }
    }
}
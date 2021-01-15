package nl.joozd.joozdter.ui.mainActivity

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import nl.joozd.joozdter.App
import nl.joozd.joozdter.calendar.CalendarDescriptor
import nl.joozd.joozdter.calendar.CalendarHandler
import nl.joozd.joozdter.data.JoozdterPrefs
import nl.joozd.joozdter.ui.utils.JoozdterViewModel

class MainActivityViewModel: JoozdterViewModel() {
    /**
     * Observables:
     */

    private val _pickedCalendar = MutableLiveData<CalendarDescriptor>()
    val pickedCalendar: LiveData<CalendarDescriptor>
        get() = _pickedCalendar

    private val _calendarName = MediatorLiveData<String>().apply{
        JoozdterPrefs.pickedCalendar?.let { value = it }
        addSource(pickedCalendar){
            it?.let{ cd ->
                value = cd.name
            }
        }
    }
    val calendarName: LiveData<String>
        get() = _calendarName

    private val _foundCalendars = MutableLiveData(emptyList<CalendarDescriptor>())
    val foundCalendars: LiveData<List<CalendarDescriptor>>
        get() = _foundCalendars

    //Checkboxes should observe these:
    private val _leave = MutableLiveData(JoozdterPrefs.leave)
    private val _taxi = MutableLiveData(JoozdterPrefs.taxi)
    private val _checkIn = MutableLiveData(JoozdterPrefs.checkIn)
    private val _checkOut = MutableLiveData(JoozdterPrefs.checkOut)
    private val _flight = MutableLiveData(JoozdterPrefs.flight)
    private val _hotel = MutableLiveData(JoozdterPrefs.hotel)
    private val _standby = MutableLiveData(JoozdterPrefs.standby)
    private val _simulator = MutableLiveData(JoozdterPrefs.simulator)
    private val _actualSimulator = MutableLiveData(JoozdterPrefs.actualSimulator)
    private val _other = MutableLiveData(JoozdterPrefs.other)

    /**
     * Functions to be called by onClickListeners
     */

    fun daysOffswitchClicked() {
        JoozdterPrefs.leave = !JoozdterPrefs.leave
    }
    fun taxiSwitchClicked(){
        JoozdterPrefs.taxi = !JoozdterPrefs.taxi
    }
    fun checkInSwitchClicked(){
        JoozdterPrefs.checkIn = !JoozdterPrefs.checkIn
    }
    fun checkOutSwitchClicked(){
        JoozdterPrefs.checkOut = !JoozdterPrefs.checkOut
    }
    fun flightsSwitchClicked(){
        JoozdterPrefs.flight = !JoozdterPrefs.flight
    }
    fun hotelSwitchClicked(){
        JoozdterPrefs.hotel = !JoozdterPrefs.hotel
    }
    fun standbySwitchClicked(){
        JoozdterPrefs.standby = !JoozdterPrefs.standby
    }
    fun simBriefingSwitchClicked(){
        JoozdterPrefs.simulator = !JoozdterPrefs.simulator
    }
    fun simActualSwitchClicked(){
        JoozdterPrefs.actualSimulator = !JoozdterPrefs.actualSimulator
    }
    fun otherSwitchClicked(){
        JoozdterPrefs.other = !JoozdterPrefs.other
    }

    val leave: LiveData<Boolean>
        get() = _leave
    val taxi: LiveData<Boolean>
        get() = _taxi
    val checkIn: LiveData<Boolean>
        get() = _checkIn
    val checkOut: LiveData<Boolean>
        get() = _checkOut
    val flight: LiveData<Boolean>
        get() = _flight
    val hotel: LiveData<Boolean>
        get() = _hotel
    val standby: LiveData<Boolean>
        get() = _standby
    val simulator: LiveData<Boolean>
        get() = _simulator
    val actualSimulator: LiveData<Boolean>
        get() = _actualSimulator
    val other: LiveData<Boolean>
        get() = _other





    private val calendarHandler = CalendarHandler(context).apply {
        viewModelScope.launch{
            initialize {
                    _foundCalendars.postValue(calendarsList)
                    _pickedCalendar.postValue(findCalendarByName(JoozdterPrefs.pickedCalendar))
                }
            }
        }








    private val onSharedPrefsChangedListener =  SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            JoozdterPrefs::pickedCalendar.name -> _calendarName.value = JoozdterPrefs.pickedCalendar

            JoozdterPrefs::leave.name -> _leave.value = JoozdterPrefs.leave
            JoozdterPrefs::taxi.name -> _taxi.value = JoozdterPrefs.taxi
            JoozdterPrefs::checkIn.name -> _checkIn.value = JoozdterPrefs.checkIn
            JoozdterPrefs::checkOut.name -> _checkOut.value = JoozdterPrefs.checkOut
            JoozdterPrefs::flight.name -> _flight.value = JoozdterPrefs.flight
            JoozdterPrefs::hotel.name -> _hotel.value = JoozdterPrefs.hotel
            JoozdterPrefs::standby.name -> _standby.value = JoozdterPrefs.standby
            JoozdterPrefs::simulator.name -> _simulator.value = JoozdterPrefs.simulator
            JoozdterPrefs::actualSimulator.name -> _actualSimulator.value = JoozdterPrefs.actualSimulator
            JoozdterPrefs::other.name -> _other.value = JoozdterPrefs.other
        }
    }
    init{
        JoozdterPrefs.sharedPrefs.registerOnSharedPreferenceChangeListener (onSharedPrefsChangedListener)
    }

    fun getCalendar(calendar: CalendarDescriptor){
        _pickedCalendar.value = calendar
        JoozdterPrefs.pickedCalendar = calendar.name
    }
}
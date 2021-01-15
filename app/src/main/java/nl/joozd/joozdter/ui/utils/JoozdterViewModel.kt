package nl.joozd.joozdter.ui.utils

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nl.joozd.joozdter.App

abstract class JoozdterViewModel: ViewModel() {
    private val _feedbackEvent = MutableLiveData<FeedbackEvent>()
    val feedbackEvent: LiveData<FeedbackEvent>
        get() = _feedbackEvent

    protected val context: Context
        get() = App.instance

    /**
     * Gives feedback to activity.
     * @param event: type of event
     * @return: The event that si being fed back
     * The [FeedbackEvent] that is being returned can be edited (ie. extraData can be filled)
     * with an [apply] statement. This is faster than the filling of the LiveData so it works.
     */
    protected fun feedback(event: FeedbackEvents.Event): FeedbackEvent =
        FeedbackEvent(event).also{
            viewModelScope.launch(Dispatchers.Main) {_feedbackEvent.value = it }
        }
}
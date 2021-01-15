package nl.joozd.joozdter.utils.extensions

import android.widget.CompoundButton
import java.util.*

/**
 * Use this if you want your compoundButton to do something when (un) checked,
 * but not change it's state by itself.
 *
 * It will block the actual changing of the button, but instead run [onCheckedChanged]
 * which should in turn eventually lead to setting `isChecked` to get proper setting of the switch
 * after its action has been performed
 * This might cause some trouble as it will trigger on programmatic sets outside of this listener.
 * To get around that, use [setIsCheckedWithoutBypassedListener] if you don't want that to happen.
 */
fun CompoundButton.setInterceptedOnCheckedChangedListener(onCheckedChanged: CompoundButton.OnCheckedChangeListener){
    compoundButtonListeners[this] = onCheckedChanged
    setOnCheckedChangeListener { compoundButton, b ->
        compoundButton.isChecked = !compoundButton.isChecked
        onCheckedChanged.onCheckedChanged(compoundButton, b)
    }
}

/**
 * Set value without triggering the listener added in [setInterceptedOnCheckedChangedListener]
 */
fun CompoundButton.setIsCheckedWithoutBypassedListener(isChecked: Boolean){
    compoundButtonListeners[this]?.let{ l ->
        setOnCheckedChangeListener(null)
        this.isChecked = isChecked
        setOnCheckedChangeListener(l)
    }
}

/**
 * Holds the OnCheckedChangeListeners for the compoundbuttons, as they are private and cannot
 * be retreived otherwise.
 */
private val compoundButtonListeners = WeakHashMap<CompoundButton, CompoundButton.OnCheckedChangeListener>()


var CompoundButton.bypassedIsChecked: Boolean
    get() = isChecked
    set(it: Boolean) { setIsCheckedWithoutBypassedListener(it)}

/*
/**
 * Same but with a [View.OnClickListener] for prettier code when not using the result of the switch
 * @see setInterceptedOnCheckedChangedListener
 */
fun CompoundButton.setInterceptedOnCheckedChangedListener(onClick: View.OnClickListener){
    setOnCheckedChangeListener { compoundButton, _ ->
        compoundButton.isChecked = !compoundButton.isChecked
        onClick.onClick(compoundButton)
    }
}

 */
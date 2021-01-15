package nl.joozd.joozdter.ui.adapters

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import nl.joozd.joozdter.R
import nl.joozd.joozdter.calendar.CalendarDescriptor
import nl.joozd.joozdter.databinding.ItemCalendarPickerBinding
import nl.joozd.joozdter.utils.extensions.ctx

class CalendarPickerAdapter(private var calendars: List<CalendarDescriptor>,
                            private val itemSelected: (CalendarDescriptor) -> Unit): RecyclerView.Adapter<CalendarPickerAdapter.ViewHolder>(){
    companion object {
        var pickedCalendar: String? = null // becomes CalendarDescriptor.name
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.ctx).inflate(R.layout.item_calendar_picker, parent, false)
        return ViewHolder(view, itemSelected)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindCalendar(calendars[position])
    }

    override fun getItemCount(): Int = calendars.size

    fun updateData(newCalendars: List<CalendarDescriptor>) {
        calendars = newCalendars
        this.notifyDataSetChanged()
    }
    fun pickCalendar(calendar: String?){
        pickedCalendar = calendar
        this.notifyDataSetChanged()
    }

    class ViewHolder(val containerView: View, private val itemSelected: (CalendarDescriptor) -> Unit) :
        RecyclerView.ViewHolder(containerView) {

        fun bindCalendar(calendar: CalendarDescriptor) {
            with(ItemCalendarPickerBinding.bind(containerView)) {
                with(calendar) {
                    calendarNameTextView.text = displayName
                    @RequiresApi(Build.VERSION_CODES.M)
                    if (name == pickedCalendar) {
                        itemBackground.setBackgroundColor(itemBackground.ctx.getColor(R.color.highlightPicked))
                    } else itemBackground.setBackgroundColor(itemBackground.ctx.getColor(R.color.none))
                    itemView.setOnClickListener {
                        itemSelected(this)
                    }
                }
            }
        }
    }

}
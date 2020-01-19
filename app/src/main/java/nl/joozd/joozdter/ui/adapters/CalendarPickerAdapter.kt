package nl.joozd.joozdter.ui.adapters

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.getColor
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_calendar_picker.*
import nl.joozd.joozdter.R
import nl.joozd.joozdter.calendar.CalendarDescriptor
import nl.joozd.joozdter.calendar.CalendarHandler
import nl.joozd.joozdter.extensions.ctx

class CalendarPickerAdapter(private var calendars: List<CalendarDescriptor>,
                            private val itemSelected: (CalendarDescriptor) -> Unit): RecyclerView.Adapter<CalendarPickerAdapter.ViewHolder>(){
    companion object {
        var pickedCalendar: String? = null // becomes CalendarDescriptor.name
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarPickerAdapter.ViewHolder {
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

    class ViewHolder(override val containerView: View, private val itemSelected: (CalendarDescriptor) -> Unit) :
        RecyclerView.ViewHolder(containerView),
        LayoutContainer {

        fun bindCalendar(calendar: CalendarDescriptor){
            with (calendar){
                calendarNameTextView.text = displayName
                @RequiresApi(Build.VERSION_CODES.M)
                    if (name == pickedCalendar) {
                        itemBackground.setBackgroundColor(itemBackground.ctx.getColor(R.color.highlightPicked))
                    }
                    else itemBackground.setBackgroundColor(itemBackground.ctx.getColor(R.color.none))
                itemView.setOnClickListener {
                    itemSelected(this)
                }
            }
        }
    }

}
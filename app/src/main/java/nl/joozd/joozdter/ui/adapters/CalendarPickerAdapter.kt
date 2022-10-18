package nl.joozd.joozdter.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import nl.joozd.joozdcalendarapi.CalendarDescriptor
import nl.joozd.joozdter.R
import nl.joozd.joozdter.databinding.ItemCalendarPickerBinding

class CalendarPickerAdapter(private val itemSelected: (CalendarDescriptor) -> Unit): ListAdapter<CalendarDescriptor, CalendarPickerAdapter.ViewHolder>(
    DIFF_CALLBACK){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_picker, parent, false)
        return ViewHolder(view, itemSelected)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindCalendar(getItem(position))
    }

    fun pickCalendar(calendar: CalendarDescriptor?){
        val oldPickedCalendar = pickedCalendar
        pickedCalendar = calendar
        currentList.indexOf(calendar).takeIf{it != -1}?.let {
            notifyItemChanged(it)
        }
        currentList.indexOf(oldPickedCalendar).takeIf{it != -1}?.let { notifyItemChanged(it) }
    }

    class ViewHolder(private val containerView: View, private val itemSelected: (CalendarDescriptor) -> Unit) :
        RecyclerView.ViewHolder(containerView) {

        fun bindCalendar(calendar: CalendarDescriptor) {
            with(ItemCalendarPickerBinding.bind(containerView)) {
                with(calendar) {
                    calendarNameTextView.text = displayName
                    if (calendar == pickedCalendar) {
                        itemBackground.setBackgroundColor(itemBackground.context.getColor(R.color.highlightPicked))
                    } else itemBackground.setBackgroundColor(itemBackground.context.getColor(R.color.none))
                    itemView.setOnClickListener {
                        itemSelected(this)
                    }
                }
            }
        }
    }

    companion object {
        var pickedCalendar: CalendarDescriptor? = null // becomes CalendarDescriptor.name

        private val DIFF_CALLBACK = object: DiffUtil.ItemCallback<CalendarDescriptor>() {
            override fun areItemsTheSame(oldItem: CalendarDescriptor, newItem: CalendarDescriptor): Boolean =
                oldItem.ID == newItem.ID

            override fun areContentsTheSame(oldItem: CalendarDescriptor, newItem: CalendarDescriptor): Boolean =
                oldItem == newItem
        }
    }
}
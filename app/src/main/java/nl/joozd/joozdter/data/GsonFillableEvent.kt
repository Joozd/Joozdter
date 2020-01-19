package nl.joozd.joozdter.data

import androidx.annotation.Keep

@Keep
data class GsonFillableEvent (val event_type: String, val description: String, val start_time: String, val end_time: String, val extra_data: String, val notes: String)
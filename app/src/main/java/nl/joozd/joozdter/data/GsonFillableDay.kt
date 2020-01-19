package nl.joozd.joozdter.data

import androidx.annotation.Keep

@Keep
data class GsonFillableDay(val date: String, val events: List<GsonFillableEvent>)

// date: "2019-07-05"

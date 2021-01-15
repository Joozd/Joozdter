package nl.joozd.joozdter.data.extensions

import androidx.work.Data
import nl.joozd.joozdter.model.Event
import nl.joozd.klcrosterparser.KlcRosterEvent
import java.time.Instant

fun KlcRosterEvent.toData(): Data = Data.Builder().apply{
    putString(TYPE_TAG, type)
    putLong(START_TAG, start.epochSecond)
    putLong(END_TAG, end.epochSecond)
    putString(DESCRIPTION_TAG, description)
    putString(EXTRA_MESSAGE_TAG, extraMessage)
}.build()

fun klcRosterEventFromData(data: Data): KlcRosterEvent = with(data){
    KlcRosterEvent(
        getString(TYPE_TAG)!!,
        Instant.ofEpochSecond(getLong(START_TAG, 0)),
        Instant.ofEpochSecond(getLong(END_TAG, 0)),
        getString(DESCRIPTION_TAG)!!,
        getString(EXTRA_MESSAGE_TAG)
    )
}

/**
 * Will convert a [KlcRosterEvent] to an [Event]
 * [KlcRosterEvent.extraMessage] will go to [Event.extraData] so it will end up in `location`
 * [Event.notes] starts empty, gives room for notes by other functions.
 */
fun KlcRosterEvent.toModel(): Event = Event(
    eventType = type,
    description = description,
    startTime = start,
    endTime = end,
    extraData = extraMessage ?: "",
    notes = "")


private const val TYPE_TAG = "a"
private const val START_TAG = "b"
private const val END_TAG = "c"
private const val DESCRIPTION_TAG = "d"
private const val EXTRA_MESSAGE_TAG = "e"





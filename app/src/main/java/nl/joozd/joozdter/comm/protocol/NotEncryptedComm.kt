package nl.joozd.joozdter.comm.protocol

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import nl.joozd.joozdter.comm.utils.addMetaData
import nl.joozd.joozdter.data.Day
import nl.joozd.joozdter.data.Event
import nl.joozd.joozdter.data.GsonFillableDay
import nl.joozd.joozdter.data.MetaData

// standard format: request|jsonmetadata|pdfdata


class NotEncryptedComm { // will request an AES key upon initializiation and use that for further Comms
    companion object {
        const val EOF = "EOF"
        const val ERROR_INVALID_ROSTER = "INVALID_ROSTER"
    }
    private val client = Client()

    fun sendRequest(requestString: String, metaData: MetaData, extraData: String?=null){ // for list of possible requests, see documentation
        var request = requestString.addMetaData(metaData)
        extraData?.let { request += "|$it" }
        client.sendToServer(
            Packet(request.toByteArray(Charsets.UTF_8), OUTBOUND_PACKET)
        )
    }
    fun receiveString(): String? {
        return client.readFromSocket()?.message?.toString(Charsets.UTF_8)
    }
    fun receiveDays(): List<Day>? {
        Log.d("receiveFlights!" ,"Receiving Days?")
        val listType = object : TypeToken<List<GsonFillableDay>>() { }.type
        val jsonData = (client.readFromSocket()!!.message).toString(Charsets.UTF_8)
        if (jsonData == ERROR_INVALID_ROSTER) return null
        val gsonDaysList =(Gson().fromJson<List<GsonFillableDay>>(jsonData, listType))
        val daysList = gsonDaysList.map{ Day(it.date, it.events.map {e -> Event(e.event_type, e.description, e.start_time, e.end_time, e.extra_data, e.notes)}) }
        client.sendOK()
        return daysList
    }

    fun close(){
        client.close()
    }
}
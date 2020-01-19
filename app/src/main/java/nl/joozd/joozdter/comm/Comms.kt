package nl.joozd.joozdter.comm

import android.util.Base64
import nl.joozd.joozdter.comm.protocol.Client
import nl.joozd.joozdter.data.MetaData
import nl.joozd.joozdter.comm.protocol.NotEncryptedComm
import nl.joozd.joozdter.data.Day


class Comms{
    private val handler: NotEncryptedComm = NotEncryptedComm()
            companion object{
        const val SEND_ROSTER       = "__SEND_PDF" // send a PDF file. Make the server turn it into flights. Or not.
    }

    fun sendPdfRoster(roster: ByteArray, metaData: MetaData): List<Day>? {
        synchronized(this) {
            handler.sendRequest(SEND_ROSTER, metaData, Base64.encodeToString(roster, Base64.DEFAULT))
            return handler.receiveDays()
        }
    }

    fun close() {
        synchronized(this) {
            handler.close()
        }
    }
}
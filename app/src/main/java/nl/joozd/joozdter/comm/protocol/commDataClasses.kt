package nl.joozd.joozdter.comm.protocol

const val OUTBOUND_PACKET = true
const val INBOUND_PACKET = false


class Packet (private val input: ByteArray, val outbound : Boolean = false) {
    val header="JOOZDTER"
    val message: ByteArray
    val output: ByteArray
    init{
        if (outbound)
        {
            message=input
            val length=input.size + 8
            output = length.toString().padStart(10,'0').toByteArray(Charsets.UTF_8) + header.toByteArray(Charsets.UTF_8)+input
        }
        else { // inbound package
            output=input
            if (input.size < 8) {
                throw IllegalArgumentException("Header is not $header")
            }
            if (input.take(8).toByteArray().toString(Charsets.UTF_8) != header) {
                throw IllegalArgumentException("Header is not $header")
            }
            message = input.drop(8).toByteArray()
        }
    }
}
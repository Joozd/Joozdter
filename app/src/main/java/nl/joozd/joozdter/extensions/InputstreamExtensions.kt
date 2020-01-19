package nl.joozd.joozdter.extensions

import java.io.InputStream

fun InputStream.toByteArray(): ByteArray {
    val bufferSize: Int = 2048
    var buffer = ByteArray(bufferSize)
    var output = ByteArray(0)
    var len = this.read(buffer)
    while (len != -1) {
        output += buffer
        len = this.read(buffer)
    }
    return output
}
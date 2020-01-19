package nl.joozd.joozdter.comm.protocol

import android.util.Log
import java.io.BufferedInputStream
import java.io.IOException
import java.lang.Exception
import java.net.ConnectException
import java.net.Socket
import java.net.SocketException
import java.net.UnknownHostException
import javax.net.ssl.SSLSocketFactory


const val SERVER_URL = "joozd.nl"
const val SERVER_PORT = 1338 // new port for ssl connection



class Client {
    class ExceptionListener(private val f:(exception: Exception) -> Unit){
        fun except(exception: Exception){
            f(exception)
        }
    }
    var exceptionListener: ExceptionListener = ExceptionListener{x ->throw(x)}
    var socket: Socket? = null
    init{
        try {
            socket = SSLSocketFactory.getDefault().createSocket(SERVER_URL, SERVER_PORT)
            //socket = Socket(SERVER_URL, SERVER_PORT)
        }catch (he: UnknownHostException){
            val exceptionString = "An exception 11 occurred:\n ${he.printStackTrace()}"
            Log.e(javaClass.simpleName, exceptionString, he)
            exceptionListener?.except(he)
        }catch (ioe: IOException){
            val exceptionString = "An exception 12 occurred:\n ${ioe.printStackTrace()}"
            Log.e(javaClass.simpleName, exceptionString, ioe)
            exceptionListener?.except(ioe)
        } catch (ce: ConnectException){
            val exceptionString = "An exception 13 occurred:\n ${ce.printStackTrace()}"
            Log.e(javaClass.simpleName, exceptionString, ce)
            exceptionListener?.except(ce)
        }catch (se: SocketException){
            val exceptionString = "An exception 14 occurred:\n ${se.printStackTrace()}"
            Log.e(javaClass.simpleName, exceptionString, se)
            exceptionListener?.except(se)
        }

    }
    fun sendToServer(packet: Packet): String{
        val message = packet.output
        Log.d("SendToServer:", message.take(40).toByteArray().toString(Charsets.UTF_8))
        try {
            socket?.let {
                it.getOutputStream().write(message)
                return "message sent: " + String(message)
            }
        }catch (he: UnknownHostException){
            val exceptionString = "An exception 21 occurred:\n ${he.printStackTrace()}"
            Log.e(javaClass.simpleName, exceptionString, he)
            exceptionListener?.except(he)
            return   exceptionString
        }catch (ioe: IOException){
            val exceptionString = "An exception 22 occurred:\n ${ioe.printStackTrace()}"
            Log.e(javaClass.simpleName, exceptionString, ioe)
            exceptionListener?.except(ioe)
            return   exceptionString
        } catch (ce: ConnectException){
            val exceptionString = "An exception 23 occurred:\n ${ce.printStackTrace()}"
            Log.e(javaClass.simpleName, exceptionString, ce)
            exceptionListener?.except(ce)
            return   exceptionString
        }catch (se: SocketException){
            val exceptionString = "An exception 24 occurred:\n ${se.printStackTrace()}"
            Log.e(javaClass.simpleName, exceptionString, se)
            exceptionListener?.except(se)
            return   exceptionString
        }
        return "no socket"
    }

    fun readFromSocket(): Packet?{
        var lengthString = ByteArray(0)
        var receivedData = ByteArray(0)
        var readBuffer=ByteArray(8192)
        //var readByte: Int

        try {
            socket?.let {
                val input=BufferedInputStream(it.getInputStream())
                while (lengthString.size < 10) lengthString += input.read().toByte()
                val messageLength = lengthString.take(10).toByteArray().toString(Charsets.UTF_8).toInt()
                Log.d("readFromSocket", "messageLength = $messageLength")
                do{
                    input.read(readBuffer)
                    var readBytes= readBuffer.indexOf(0.toByte())
                    if (readBytes == -1) readBytes = readBuffer.size
                    receivedData += readBuffer.slice(0 until readBytes)
                    readBuffer=ByteArray(8192)
                } while (receivedData.size < messageLength-10)
                while (receivedData.last() == 0.toByte()) receivedData = receivedData.dropLast(1).toByteArray()



                Log.d("readFromSocket", "done receiving ${receivedData.size} bytes of data! First and Last bytes are ${receivedData.first().toInt()} ${receivedData.last().toInt()}")
                Log.d("readFromSocket", receivedData.toString(Charsets.UTF_8))
            }
        }catch (he: UnknownHostException){
            val exceptionString = "An exception 31 occurred:\n ${he.printStackTrace()}"
            Log.e(javaClass.simpleName, exceptionString, he)
            exceptionListener?.except(he)
            return null
        }catch (ioe: IOException){
            val exceptionString = "An exception 32 occurred:\n ${ioe.printStackTrace()}"
            Log.e(javaClass.simpleName, exceptionString, ioe)
            exceptionListener?.except(ioe)
            return null
        } catch (ce: ConnectException){
            val exceptionString = "An exception 33 occurred:\n ${ce.printStackTrace()}"
            Log.e(javaClass.simpleName, exceptionString, ce)
            exceptionListener?.except(ce)
            return null
        }catch (se: SocketException){
            val exceptionString = "An exception 34 occurred:\n ${se.printStackTrace()}"
            Log.e(javaClass.simpleName, exceptionString, se)
            exceptionListener?.except(se)
            return null
        }catch (nfe:NumberFormatException ){
            val exceptionString = "An exception 2.1 occurred:\n ${nfe.printStackTrace()}"
            Log.e(javaClass.simpleName, exceptionString, nfe)
            return null

        }
        return Packet(receivedData, INBOUND_PACKET)
    }

    fun sendOK(): String{
        try {
            socket?.let {
                it.getOutputStream().write("OK".toByteArray(Charsets.UTF_8))
                return "OK Sent"
            }
            return "no socket"
        }catch (he: UnknownHostException){
            val exceptionString = "An exception 41 occurred:\n ${he.printStackTrace()}"
            Log.e(javaClass.simpleName, exceptionString, he)
            exceptionListener?.except(he)
            return   exceptionString
        }catch (ioe: IOException){
            val exceptionString = "An exception 42 occurred:\n ${ioe.printStackTrace()}"
            Log.e(javaClass.simpleName, exceptionString, ioe)
            exceptionListener?.except(ioe)
            return   exceptionString
        } catch (ce: ConnectException){
            val exceptionString = "An exception 43 occurred:\n ${ce.printStackTrace()}"
            Log.e(javaClass.simpleName, exceptionString, ce)
            exceptionListener?.except(ce)
            return   exceptionString
        }catch (se: SocketException){
            val exceptionString = "An exception 44 occurred:\n ${se.printStackTrace()}"
            Log.e(javaClass.simpleName, exceptionString, se)
            exceptionListener?.except(se)
            return   exceptionString
        }
    }

    fun receiveOK(): Boolean{
        try {
            socket?.let {
                val shouldBeOK: ByteArray = byteArrayOf(it.getInputStream().read().toByte(), it.getInputStream().read().toByte())
                Log.d("receiveOK", shouldBeOK.toString(Charsets.UTF_8))
                return shouldBeOK.toString(Charsets.UTF_8) == "OK"
            }
            return false
        }catch (he: UnknownHostException){
            val exceptionString = "An exception 1 occurred:\n ${he.printStackTrace()}"
            Log.e(javaClass.simpleName, exceptionString, he)
            return   false
        }catch (ioe: IOException){
            val exceptionString = "An exception 1 occurred:\n ${ioe.printStackTrace()}"
            Log.e(javaClass.simpleName, exceptionString, ioe)
            return   false
        } catch (ce: ConnectException){
            val exceptionString = "An exception 1 occurred:\n ${ce.printStackTrace()}"
            Log.e(javaClass.simpleName, exceptionString, ce)
            return   false
        }catch (se: SocketException) {
            val exceptionString = "An exception 1 occurred:\n ${se.printStackTrace()}"
            Log.e(javaClass.simpleName, exceptionString, se)
            return false
        }
    }

    fun close(){
        socket?.close()
    }

}


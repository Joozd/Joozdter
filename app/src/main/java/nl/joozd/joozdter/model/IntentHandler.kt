package nl.joozd.joozdter.model

import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.joozd.joozdter.App
import nl.joozd.joozdter.exceptions.BadFileTypeException
import nl.joozd.joozdter.exceptions.NotARosterException
import nl.joozd.joozdter.parser.RosterParser
import nl.joozd.joozdter.workers.JoozdterWorkersHub
import java.io.FileNotFoundException
import java.io.InputStream

class IntentHandler(intent: Intent, private val onProgressMade: OnProgressMadeListener? = null) {
    private val context get() = App.instance
    private val uri = intent.getUri() ?: throw (FileNotFoundException("Uri not found in Intent $intent"))


    private var progress: Int = INITIALIZED
        set(it){
            onProgressMade?.onProgressMade(it)
            field = it
        }

    init{
        //initial trigger of onProgressMade counter because initialization worked.
        onProgressMade?.onProgressMade(progress)
    }


    /**
     * Parse file into a roster
     */
    suspend fun sendFileToWorker(){
        val parser = RosterParser.ofUri(uri, context)
            ?:  throw (BadFileTypeException("Did not receive a valid PDF file"))
        updateProgress(FILE_READ)
        if (!parser.checkIfValid()) throw (NotARosterException("Unable to parse file"))
        updateProgress(FILE_VALID)
        JoozdterWorkersHub.processUri(uri)
        updateProgress(ROSTER_PASSED_TO_WORKER)
    }


    private fun Intent.getUri(): Uri? =
        getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri

    /**
     * Update progress to furthest progress we made thus far
     */
    private fun updateProgress(p: Int){
        progress = minOf(progress, p)
    }




    fun interface OnProgressMadeListener{
        fun onProgressMade(progress: Int)
    }

    companion object{
        const val INITIALIZED = 4
        // const val FILE_OPENED = 3
        const val FILE_READ = 2
        const val FILE_VALID = 1
        const val ROSTER_PASSED_TO_WORKER = 0
    }
}
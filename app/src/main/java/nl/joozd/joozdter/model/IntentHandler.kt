package nl.joozd.joozdter.model

import android.content.Intent
import android.net.Uri
import nl.joozd.joozdter.App
import nl.joozd.joozdter.utils.extensions.getParcelableExtraUri
import java.io.FileNotFoundException

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


    private fun Intent.getUri(): Uri? =
        getParcelableExtraUri()

    /**
     * Update progress to furthest progress we made thus far
     */
    private fun updateProgress(p: Int){
        progress = minOf(progress, p)
    }

    private fun debugPrint(debugMessage: String){
        println("IntentWorker Debug says: $debugMessage")
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
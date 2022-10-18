package nl.joozd.joozdter.ui.utils

import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch

open class JoozdterActivity: AppCompatActivity(), CoroutineScope by MainScope() {
    protected val activity: AppCompatActivity
        get() = this

    protected fun alert(res: Int): AlertDialog =
        AlertDialog.Builder(this).apply {
            setMessage(res)
            setPositiveButton(android.R.string.ok) { _, _ -> }
        }.show()


    /*
    protected fun alert(message: CharSequence) =
        AlertDialog.Builder(this).apply {
            setMessage(message)
            setPositiveButton(android.R.string.ok) { _, _ -> }
        }.show()
     */


    protected fun alert(message: CharSequence, onClose: () -> Unit) =
        AlertDialog.Builder(this).apply {
            setMessage(message)
            setPositiveButton(android.R.string.ok) { _, _ ->
                onClose()
            }
        }.show()

    protected fun <T> Flow<T>.launchCollectWhileLifecycleStateStarted(collector: FlowCollector<T>){
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                collect(collector)
            }
        }
    }

}
package nl.joozd.joozdter.ui.utils

import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import nl.joozd.joozdter.App

open class JoozdterActivity: AppCompatActivity(), CoroutineScope by MainScope() {
    protected val activity: AppCompatActivity
        get() = this

    protected fun alert(res: Int) =
        AlertDialog.Builder(this).apply {
            setMessage(res)
            setPositiveButton(android.R.string.ok) { _, _ -> }
        }.show()


    protected fun alert(message: CharSequence) =
        AlertDialog.Builder(this).apply {
            setMessage(message)
            setPositiveButton(android.R.string.ok) { _, _ -> }
        }.show()


    protected fun alert(message: CharSequence, onClose: () -> Unit) =
        AlertDialog.Builder(this).apply {
            setMessage(message)
            setPositiveButton(android.R.string.ok) { _, _ ->
                onClose()
            }
        }.show()

}
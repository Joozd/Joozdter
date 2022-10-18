package nl.joozd.joozdter.ui.pdfParserActivity

import android.Manifest
import android.animation.ValueAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import nl.joozd.joozdter.R
import nl.joozd.joozdter.databinding.ActivityPdfParserBinding
import nl.joozd.joozdter.ui.mainActivity.MainActivity
import nl.joozd.joozdter.ui.utils.JoozdterActivity
import nl.joozd.joozdter.utils.enums.Progress

class JoozdterPdfParserActivity : JoozdterActivity() {
    private val viewModel: PdfParserActivityViewModel2 by viewModels()

    //Setting this will end previous animation
    private var bigNumberAnimator: ValueAnimator? = null
        set(it){
            bigNumberAnimator?.end()
            field = it
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ActivityPdfParserBinding.inflate(layoutInflater).apply {
            //check if permissions are OK, ask if they aren't:
            if (!checkCalendarWritePermission())
                requestCalendarWritePermission()
            else
                viewModel.parseIntent(intent)

            observeFlows()

            /**
             * OnClickisteners
             */
            closePdfParserActivityButton.setOnClickListener { finish() }

            setContentView(root)
        }
    }

    private fun ActivityPdfParserBinding.observeFlows(){
        viewModel.progressFlow.launchCollectWhileLifecycleStateStarted{
            showProgress(it)
        }
        viewModel.messageFlow.launchCollectWhileLifecycleStateStarted{ message ->
            message?.let {
                if (it == PdfParserActivityViewModel2.NO_CALENDAR_SELECTED_ERROR)
                    showNoCalendarPickedDialog()
                else
                    alert(it){ viewModel.messageShown() }
            }
        }
    }

    private fun ActivityPdfParserBinding.showProgress(progress: Progress){
        updateBigNumber(when(progress){
            Progress.STARTED -> 5
            Progress.GOT_FILE -> 4
            Progress.READING_FILE -> 3
            Progress.PARSING_ROSTER -> 2
            Progress.SAVING_ROSTER -> 1

            Progress.DONE -> 0.also{ done("done", false) }
            Progress.ERROR -> (-1).also{ errorLayout() }
        })
    }

    private fun requestCalendarWritePermission() {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR),
            0
        )
    }

    private fun checkCalendarWritePermission() =
        (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_CALENDAR)
                == PackageManager.PERMISSION_GRANTED)

    private fun ActivityPdfParserBinding.updateBigNumber(number: Int){
        countDownCounter.text = when(number) {
            0 -> getString(R.string.checkMark)
            -1 -> getString(R.string.errorMark)
            else -> number.toString()
        }
        bigNumberAnimator = ValueAnimator.ofFloat(BIG_NUMBER_SCALE_FACTOR, 1.0f).apply {
            addUpdateListener {
                countDownCounter.scaleX = it.animatedValue as Float
                countDownCounter.scaleY = it.animatedValue as Float
            }
            duration = 250
            start()
        }

    }

    private fun ActivityPdfParserBinding.done(text: String, isError: Boolean){
        progressTextView.text = text
        closePdfParserActivityButton.visibility = View.VISIBLE
        backgroundLayout.setOnClickListener { finish() }
        if (isError) errorLayout()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        recreate()
    }

    private fun showNoCalendarPickedDialog() =
        alert("No valid calendar picked.\nPlease pick one in the main screen and try again." ){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

    private fun ActivityPdfParserBinding.errorLayout(){
        updateBigNumber(-1) // -1 means ERROR
        TextViewCompat.setTextAppearance(countDownCounter, R.style.bigNumberErrorStyle)
        TextViewCompat.setTextAppearance(progressTextView, R.style.statusErrorStyle)
    }


    companion object{
        private const val BIG_NUMBER_SCALE_FACTOR = 2.0f
    }
}


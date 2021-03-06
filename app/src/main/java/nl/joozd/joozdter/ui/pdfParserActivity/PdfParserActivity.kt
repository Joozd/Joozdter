package nl.joozd.joozdter.ui.pdfParserActivity

import android.Manifest
import android.animation.ValueAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import nl.joozd.joozdter.R
import nl.joozd.joozdter.databinding.ActivityPdfParserBinding
import nl.joozd.joozdter.ui.mainActivity.MainActivity
import nl.joozd.joozdter.ui.utils.FeedbackEvents.PdfParserActivityEvents
import nl.joozd.joozdter.ui.utils.JoozdterActivity

class PdfParserActivity : JoozdterActivity() {
    private val viewModel: PdfParserActivityViewModel by viewModels()
    private var mDialogShown: AlertDialog? = null // needs to be dismissed or dialog will be leaked

    private var bigNumberAnimator: ValueAnimator? = null
        set(it){
            bigNumberAnimator?.end()
            field = it
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ActivityPdfParserBinding.inflate(layoutInflater).apply {

            //check if permissions are OK, ask if they aren't:
            while (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_CALENDAR)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR),
                    0
                )
            }

            /**
             * OnClickisteners
             */
            closePdfParserActivityButton.setOnClickListener { finish() }

            /**
             * Observers
             */

            // Wait for calendarWorker to be ready in viewModel, then parse the file given through [intent]
            viewModel.calendarWorkerReady.observe(activity) {
                if(savedInstanceState?.getBoolean(RECREATING_TAG) == null)
                    viewModel.parseIntent(intent)
            }

            viewModel.fileReceived.observe(activity){
                progressTextView.text = getString(R.string.receivedFile)
            }

            viewModel.seemsToBeARoster.observe(activity){
                progressTextView.text = getString(R.string.itSeemsToBeARoster)
            }

            viewModel.progressCountDown.observe(activity){
                updateBigNumber(it)
            }


            viewModel.feedbackEvent.observe(activity){
                when (it.getEvent()){
                    PdfParserActivityEvents.NO_VALID_CALENDAR_PICKED -> mDialogShown = showNoCalendarPickedDialog()
                    PdfParserActivityEvents.NOT_A_KLC_ROSTER -> done("Not a valid roster", true)
                    PdfParserActivityEvents.FILE_ERROR -> done ("Weird error FILE_ERROR. maybe try again?", true)
                    PdfParserActivityEvents.FILE_NOT_FOUND -> done ("Weird error FILE_NOT_FOUND. maybe try again?", true)
                    PdfParserActivityEvents.DONE -> done ("Done! Your roster should appear in your calendar shortly!", false)
                }
            }
            setContentView(root)
        }
    }

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
        recreate()
    }

    private fun showNoCalendarPickedDialog() =
        alert("No valid calendar picked.\nPlease pick one in the main screen and try again." ){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

    private fun ActivityPdfParserBinding.errorLayout(){
        updateBigNumber(-1) // -1 means ERROR
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) {
            countDownCounter.setTextAppearance(activity, R.style.bigNumberErrorStyle) // deprecated in API M
            progressTextView.setTextAppearance(activity, R.style.statusErrorStyle) // deprecated in API M
        } else {
            countDownCounter.setTextAppearance(R.style.bigNumberErrorStyle)  // Needs API M or higher
            progressTextView.setTextAppearance(R.style.statusErrorStyle)   // Needs API M or higher
        }
    }

    override fun onStop() {
        mDialogShown?.dismiss()
        super.onStop()
    }

    companion object{
        private const val BIG_NUMBER_SCALE_FACTOR = 2.0f
        private const val RECREATING_TAG = "RECREATING_TAG"
    }
}


package nl.joozd.joozdter.ui.pdfParserActivity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import nl.joozd.joozdter.databinding.ActivityPdfParserBinding
import nl.joozd.joozdter.ui.mainActivity.MainActivity
import nl.joozd.joozdter.ui.utils.FeedbackEvents.PdfParserActivityEvents
import nl.joozd.joozdter.ui.utils.JoozdterActivity

class PdfParserActivity : JoozdterActivity() {
    private val viewModel: PdfParserActivityViewModel by viewModels()
    private var mDialogShown: AlertDialog? = null // needs to be dismissed or dialog will be leaked

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
             * Onclicklisteners
             */
            closePdfParserActivityButton.setOnClickListener { finish() }

            /**
             * Observers
             */

            // Wait for calendarWorker to be ready in viewModel, then parse the file given through [intent]
            viewModel.calendarWorkerReady.observe(activity) {
                viewModel.parseIntent(intent)
            }

            viewModel.fileReceived.observe(activity){
                receivedFileText.visibility = if (it) View.VISIBLE else View.INVISIBLE
            }

            viewModel.seemsToBeARoster.observe(activity){
                itSeemsToBeARosterText.visibility = if (it) View.VISIBLE else View.INVISIBLE
            }


            viewModel.feedbackEvent.observe(activity){
                when (it.getEvent()){
                    PdfParserActivityEvents.NO_VALID_CALENDAR_PICKED -> mDialogShown = showNoCalendarPickedDialog()
                    PdfParserActivityEvents.NOT_A_KLC_ROSTER -> done("Not a valid roster")
                    PdfParserActivityEvents.FILE_ERROR -> done ("Weird error FILE_ERROR. maybe try again?")
                    PdfParserActivityEvents.FILE_NOT_FOUND -> done ("Weird error FILE_NOT_FOUND. maybe try again?")
                    PdfParserActivityEvents.DONE -> done ("Done! Your roster should appear in your calendar shortly!")
                }
            }
            setContentView(root)
        }
    }

    private fun ActivityPdfParserBinding.done(text: String){
        resultTextView.text = text
        resultTextView.visibility = View.VISIBLE
        closePdfParserActivityButton.visibility = View.VISIBLE
        println("Done!XXXXXXXXAUBGRGR")
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        recreate()
    }

    private fun showNoCalendarPickedDialog() =
        alert("No valid calendar picked.\nPlease pick one in the main screen and try again." ){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

    override fun onStop() {
        mDialogShown?.dismiss()
        super.onStop()
    }
}


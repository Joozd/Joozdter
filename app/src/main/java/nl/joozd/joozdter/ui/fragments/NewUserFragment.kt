package nl.joozd.joozdter.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import nl.joozd.joozdter.R




class NewUserFragment : Fragment() {
    private lateinit var thisView: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        thisView = inflater.inflate(R.layout.new_user_fragment, container, false)

        val closeButton: Button = thisView.findViewById(R.id.closePdfParserActivityButton)

        closeButton.setOnClickListener {
            fragmentManager?.popBackStack()
        }

        return thisView
    }
}
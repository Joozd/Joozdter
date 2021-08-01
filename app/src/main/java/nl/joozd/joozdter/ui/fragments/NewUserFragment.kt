package nl.joozd.joozdter.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import nl.joozd.joozdter.R
import nl.joozd.joozdter.databinding.NewUserFragmentBinding


class NewUserFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = NewUserFragmentBinding.bind(inflater.inflate(R.layout.new_user_fragment, container, false)).apply{

        closePdfParserActivityButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

    }.root
}
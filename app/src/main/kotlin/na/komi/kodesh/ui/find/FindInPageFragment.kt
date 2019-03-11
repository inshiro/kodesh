package na.komi.kodesh.ui.find

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.Fragment
import na.komi.kodesh.R
import na.komi.kodesh.ui.main.MainComponents
import na.komi.kodesh.util.Knavigator

class FindInPageFragment : Fragment() {

    private val knavigator: Knavigator  by MainComponents.navComponent.inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_find_in_page, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        view?.let { view ->
            val closeButton = view.findViewById<AppCompatImageButton>(R.id.close_botton)
            val editText = view.findViewById<AppCompatEditText>(R.id.find_edit_text)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                editText.showSoftInputOnFocus = true
            editText.requestFocus()

            knavigator.fragmentManager = requireActivity().supportFragmentManager

            closeButton.setOnClickListener {
                knavigator.hide(this)
            }


        }
    }
}
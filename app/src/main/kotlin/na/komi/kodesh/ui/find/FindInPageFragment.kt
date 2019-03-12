package na.komi.kodesh.ui.find

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.Fragment
import na.komi.kodesh.R
import na.komi.kodesh.ui.main.MainActivity
import na.komi.kodesh.ui.main.MainComponents
import na.komi.kodesh.util.knavigator.Knavigator

class FindInPageFragment : Fragment() {

    private val knavigator: Knavigator  by MainComponents.navComponent.inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_find_in_page, container, false)
    }

    fun InputMethodManager.showKeyboard() {
        toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    fun InputMethodManager.hideKeyboard(view:View) {
        hideSoftInputFromWindow(view.windowToken, 0);
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        view?.let { view ->
            val closeButton = view.findViewById<AppCompatImageButton>(R.id.close_botton)
            val editText = view.findViewById<AppCompatEditText>(R.id.find_edit_text)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                editText.showSoftInputOnFocus = true
            val imm by lazy { requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager }
            editText.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) imm.showKeyboard()
                else imm.hideKeyboard(v)//.v.hideKeyboard()
            }
            editText.requestFocus()

            knavigator setFragmentManager  requireActivity().supportFragmentManager

            closeButton.setOnClickListener {
                knavigator.hide(this)
            }


        }
    }

    override fun onDetach() {
        super.onDetach()
        (activity as? MainActivity)?.getNavigationView()?.menu?.findItem(R.id.action_find_in_page)?.isEnabled = true
    }
}
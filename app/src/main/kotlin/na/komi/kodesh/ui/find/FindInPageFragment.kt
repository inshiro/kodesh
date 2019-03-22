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
import na.komi.kodesh.R
import na.komi.kodesh.ui.internal.BaseFragment2
import na.komi.skate.core.extension.hide
import na.komi.skate.core.extension.startSkating

class FindInPageFragment : BaseFragment2() {
    override val layout: Int = R.layout.fragment_find_in_page

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_find_in_page, container, false)
    }

    fun InputMethodManager.showKeyboard() {
        toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    fun InputMethodManager.hideKeyboard(view: View) {
        hideSoftInputFromWindow(view.windowToken, 0);
    }

    private val listener by lazy { OnClick }

    fun setOnHideListener(init: OnClick.() -> Unit) {
        listener.init() // Calls the site functions to set the values
    }

    object OnClick {
        var onHide = {}
        fun onHide(action: () -> Unit) {
            onHide = action
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        view?.let { view ->
            val skate by startSkating(savedInstanceState)
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

            skate.fragmentManager = requireActivity().supportFragmentManager

            closeButton.setOnClickListener {
                resetBottomSheetContainer()
                this.hide()
            }
        }
    }

    fun resetBottomSheetContainer(){
        getBottomSheetContainer()?.post {
            getBottomSheetContainer()?.visibility = View.VISIBLE
        }
        listener.onHide()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        resetBottomSheetContainer()
    }

    override fun onDetach() {
        super.onDetach()
        resetBottomSheetContainer()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden)
            resetBottomSheetContainer()
    }

}
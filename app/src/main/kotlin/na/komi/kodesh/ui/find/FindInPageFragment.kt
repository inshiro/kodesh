package na.komi.kodesh.ui.find

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageButton
import na.komi.kodesh.R
import na.komi.kodesh.ui.internal.BaseFragment2
import na.komi.skate.core.extension.hide

class FindInPageFragment : BaseFragment2() {
    override val layout: Int = R.layout.fragment_find_in_page

    fun InputMethodManager.showKeyboard() {
        toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    fun InputMethodManager.hideKeyboard(view: View) {
        hideSoftInputFromWindow(view.windowToken, 0);
    }

    @Volatile
    var listener: Listeners? = null

    @Synchronized
    inline fun setListener(init: Listeners.() -> Unit) {
        listener = null
        listener = Listeners()
        listener!!.init()

    }

    inner class Listeners {

        var onHide = {}

        @Synchronized
        fun onHide(action: () -> Unit) {
            onHide = action
        }

        var onShow = {}

        @Synchronized
        fun onShow(action: () -> Unit) {
            onShow = action
        }
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
                else imm.hideKeyboard(v)
            }
            editText.requestFocus()

            val textWatcher = object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {}

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    //listener.onTextChanged(s)
                }

            }

            editText.post {
                editText.addTextChangedListener(textWatcher)
            }


            closeButton.setOnClickListener {
                //resetBottomSheetContainer()
                this.hide()
            }
            listener?.onShow?.invoke()
        }
    }

    fun resetBottomSheetContainer() {
        getBottomSheetContainer()?.post {
            getBottomSheetContainer()?.visibility = View.VISIBLE
        }
        if (!requireActivity().isChangingConfigurations)
            listener?.onHide?.invoke()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        resetBottomSheetContainer()
        listener = null
    }

}
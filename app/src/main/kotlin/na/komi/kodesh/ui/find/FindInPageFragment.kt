package na.komi.kodesh.ui.find

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.ArrayMap
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import na.komi.kodesh.Prefs
import na.komi.kodesh.R
import na.komi.kodesh.model.Bible
import na.komi.kodesh.ui.internal.BaseFragment2
import na.komi.kodesh.ui.main.MainChildAdapter
import na.komi.kodesh.ui.main.MainPageAdapter
import na.komi.kodesh.ui.widget.BaselineGridTextView
import na.komi.kodesh.ui.widget.ViewPager3
import na.komi.kodesh.util.log
import na.komi.kodesh.util.page.Formatting
import na.komi.kodesh.util.text.count
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
        var onHide: (() -> Unit)? = {}
        var onShow: (() -> Unit)? = {}

        @Synchronized
        fun onHide(action: () -> Unit) {
            onHide = action
        }


        @Synchronized
        fun onShow(action: () -> Unit) {
            onShow = action
        }
    }

    fun resetBottomSheetContainer() {
        getBottomSheetContainer()?.post {
            getBottomSheetContainer()?.visibility = View.VISIBLE
        }
        if (!requireActivity().isChangingConfigurations) {
            listener?.onHide?.invoke()
            mainChildAdapter?.search("")
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        resetBottomSheetContainer()
        listener = null
        mainChildAdapter = null
        mainList = null
    }

    data class SearchListResult (val positions: ArrayMap<Int,IntArray>, val total:Int)
    fun onDown(list: MutableList<Bible>, str: CharSequence): Int {
        var c = 0
        val kjvPref = Prefs.kjvStylingPref
        list.forEach {
            val vt = it.verseText!!.replace("[","").replace("]","").replace("<","").replace(">","")
            val newVt = if (kjvPref) Formatting.diffText(vt, Formatting.kjvList[it.id - 1]) else vt
            c += newVt.count(str.toString(),true)
        }
        return c
    }

    private val defaultTextColor by lazy {
        val typedValue = TypedValue()
        requireContext().theme.resolveAttribute(R.attr.textColor, typedValue, true)
        typedValue.data
    }
    val disabledColorStateList by lazy {
        ContextCompat.getColorStateList(requireContext(), R.color.switch_thumb_disabled_material_light)
    }
    val defaultButtonColorStateList by lazy {
        val typedValue = TypedValue()
        requireContext().theme.resolveAttribute(R.attr.textColor, typedValue, true)
        ContextCompat.getColorStateList(requireContext(), typedValue.resourceId)
    }

    var mainList : MutableList<Bible>?=null
    var mainChildAdapter: MainChildAdapter? =null
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        view?.let { view ->
            val upButton = view.findViewById<AppCompatImageButton>(R.id.find_up_botton)
            val downButton = view.findViewById<AppCompatImageButton>(R.id.find_down_botton)
            val closeButton = view.findViewById<AppCompatImageButton>(R.id.close_botton)
            val editText = view.findViewById<AppCompatEditText>(R.id.find_edit_text)
            val resultsText = view.findViewById<BaselineGridTextView>(R.id.find_results_text_view)

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

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    performSearch(s,  upButton, downButton, resultsText)
                    //listener.onTextChanged(s)
                }

            }

            editText.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (resultsText.text.isNullOrEmpty()) {
                        val s = editText.text.toString()
                        performSearch(s, upButton, downButton, resultsText)
                    }
                    imm.hideKeyboard(v)
                    return@setOnEditorActionListener true
                }
                false
            }

            editText.post {
                registerVars()
                editText.addTextChangedListener(textWatcher)
                if (editText.text.toString().isEmpty()) {
                    ImageViewCompat.setImageTintList(upButton, disabledColorStateList)
                    ImageViewCompat.setImageTintList(downButton, disabledColorStateList)
                } else {
                    if (ImageViewCompat.getImageTintList(upButton) != defaultButtonColorStateList) {
                        ImageViewCompat.setImageTintList(upButton, defaultButtonColorStateList)
                        ImageViewCompat.setImageTintList(downButton, defaultButtonColorStateList)
                    }
                    editText.selectAll()

                }
            }


            closeButton.setOnClickListener {
                //resetBottomSheetContainer()
                this.hide()
            }
            listener?.onShow?.invoke()
        }
    }

    private fun performSearch(text: CharSequence, upButton: AppCompatImageButton, downButton: AppCompatImageButton, resultsText: BaselineGridTextView) {

        if (mainList==null || mainChildAdapter == null)  {
            registerVars()
        }
        if (mainList==null || mainChildAdapter == null) return

        if (text.isEmpty()) {
            ImageViewCompat.setImageTintList(upButton, disabledColorStateList)
            ImageViewCompat.setImageTintList(downButton, disabledColorStateList)
            resultsText.text = ""
            mainChildAdapter!!.search("")
        } else {
            if (ImageViewCompat.getImageTintList(upButton) != defaultButtonColorStateList) {
                ImageViewCompat.setImageTintList(upButton, defaultButtonColorStateList)
                ImageViewCompat.setImageTintList(downButton, defaultButtonColorStateList)
            }
            val result = onDown(mainList!!, text)
            if (result == 0) {
                resultsText.text = "0/0"
                resultsText.setTextColor(Formatting.SearchNotFoundColor)
                mainChildAdapter!!.search("")
            } else {
                if (resultsText.currentTextColor != defaultTextColor)
                    resultsText.setTextColor(defaultTextColor)
                val a = "1/$result"
                resultsText.text = a
                mainChildAdapter!!.search(text)
            }
        }
    }

    fun registerVars(){
        val mainFragmentView =
            requireActivity().supportFragmentManager.fragments.find { it.tag?.contains("MainFragment") ?: false }
                ?.view
        mainList = (mainFragmentView!!.findViewById<ViewPager3>(R.id.pager_main).findViewHolderForAdapterPosition(Prefs.VP_Position) as? MainPageAdapter.ViewHolder)?.childRecyclerView?.let { crv->
            (crv.adapter as MainChildAdapter).also { mainChildAdapter = it  }.list
        }
    }

}
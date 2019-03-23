package na.komi.kodesh.ui.find

import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import na.komi.kodesh.ui.widget.NestedRecyclerView
import na.komi.kodesh.ui.widget.ViewPager3
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
            mainChildAdapter?.highlight(null)
            mainChildAdapter?.notifyDataSetChanged()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        resetBottomSheetContainer()
        listener = null
        mainChildAdapter = null
        mainList = null
        childRecyclerView = null
        resultsText = null
        _defaultTextColor = null
        _defaultButtonColorStateList = null
    }

    data class SearchListResult(val positions: MutableMap<Int, MutableList<Pair<Int, Int>>>, var total: Int)

    fun getSearchListResult(list: MutableList<Bible>, str: CharSequence): SearchListResult {
        var c = 0
        val kjvPref = Prefs.kjvStylingPref
        val searchListResult = SearchListResult(mutableMapOf(), c)
        list.forEach {
            val vt = it.verseText!!.replace("[", "").replace("]", "").replace("<", "").replace(">", "")
            val newVt = if (kjvPref) Formatting.diffText(vt, Formatting.kjvList[it.id - 1]) else vt
            c += newVt.count(str.toString(), true) { s, e, c ->
                if (searchListResult.positions[it.verseId!!] == null)
                    searchListResult.positions[it.verseId!!] = mutableListOf()
                searchListResult.positions[it.verseId!!]!!.add(Pair(s, e))
            }

        }
        searchListResult.total = c
        return searchListResult
    }

    var resultList: SearchListResult? = null

    fun onUp() {
        onButton(false)
    }

    fun onDown() {
        onButton(true)
    }

    fun onButton(down: Boolean, fromSearch: Boolean = false) {
        if (resultList == null) return
        val keys = resultList!!.positions.keys
        val values = resultList!!.positions.values
        if (down) {
            counter++
            if (counter > resultList!!.total) counter = 1
        } else {
            counter--
            if (counter <= 0) counter = resultList!!.total
        }
        var c = 0
        var idxBefore = 0
        var idxAfter = 0
        var idx = 0
        var lidx = 0
        // log d resultList!!
        //log d values
        run {
            for (index in 0 until values.size) {
                for (lindex in 0 until values.elementAt(index).size) {
                    c++
                    if (c == counter) {
                        if (index - 1 >= 0)
                            idxBefore = keys.elementAt(index - 1)
                        if (index + 1 < keys.size)
                            idxAfter = keys.elementAt(index + 1)
                        idx = keys.elementAt(index)
                        lidx = lindex + 1
                        return@run
                    }
                }
            }
        }

        //log d "$idx | $lidx"

        childRecyclerView?.scrollToPosition(idx - 1)
        resultsText?.text = "$counter/${resultList!!.total}"
        mainChildAdapter?.highlight(Pair(idx, lidx))
        if (!fromSearch) {
            mainChildAdapter?.notifyItemChanged(idxAfter - 1)
            mainChildAdapter?.notifyItemChanged(idxBefore - 1)
            mainChildAdapter?.notifyItemChanged(idx - 1)
        }
        /*val element = keys.elementAt(counter - 1)
        val key = element.first
        if (key - 1 >= 0) {
            childRecyclerView?.scrollToPosition(key - 1)
            log d "onDown Scroll to position ${key - 1}"
            resultsText?.text = "$counter/${resultList!!.total}"

            mainChildAdapter?.highlight(element)

        }*/
    }


    private var counter = 0
    private val disabledColorStateList by lazy {
        ContextCompat.getColorStateList(requireContext(), R.color.switch_thumb_disabled_material_light)
    }
    private var _defaultTextColor: Int? = null
    private val defaultTextColor
        get() = _defaultTextColor ?: _defaultTextColor.let {
            val typedValue = TypedValue()
            requireContext().theme.resolveAttribute(R.attr.textColor, typedValue, true)
            typedValue.data.also { _defaultTextColor = it }
        }
    private var _defaultButtonColorStateList: ColorStateList? = null
    private val defaultButtonColorStateList
        get() = _defaultButtonColorStateList ?: _defaultButtonColorStateList.let {
            val typedValue = TypedValue()
            requireContext().theme.resolveAttribute(R.attr.textColor, typedValue, true)
            ContextCompat.getColorStateList(requireContext(), typedValue.resourceId)
                .also { _defaultButtonColorStateList = it }
        }

    var mainList: MutableList<Bible>? = null
    var mainChildAdapter: MainChildAdapter? = null
    var childRecyclerView: NestedRecyclerView? = null
    var resultsText: BaselineGridTextView? = null
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        view?.let { view ->
            val upButton = view.findViewById<AppCompatImageButton>(R.id.find_up_botton)
            val downButton = view.findViewById<AppCompatImageButton>(R.id.find_down_botton)
            val closeButton = view.findViewById<AppCompatImageButton>(R.id.close_botton)
            val editText = view.findViewById<AppCompatEditText>(R.id.find_edit_text)
            resultsText = view.findViewById(R.id.find_results_text_view)

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
                    performSearch(s, upButton, downButton, resultsText!!)
                    //listener.onTextChanged(s)
                }

            }

            editText.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (resultsText!!.text.isNullOrEmpty()) {
                        val s = editText.text.toString()
                        performSearch(s, upButton, downButton, resultsText!!)
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


            upButton.setOnClickListener {
                if (resultsText!!.text.isNullOrEmpty()) {
                    counter = 0
                    performSearch(editText.text.toString(), upButton, downButton, resultsText!!)
                }
                onUp()
            }
            downButton.setOnClickListener {
                if (resultsText!!.text.isNullOrEmpty()) {
                    counter = 0
                    performSearch(editText.text.toString(), upButton, downButton, resultsText!!)
                }
                onDown()
            }

            closeButton.setOnClickListener {
                //resetBottomSheetContainer()
                this.hide()
            }
            listener?.onShow?.invoke()
        }
    }

    private fun performSearch(
        text: CharSequence,
        upButton: AppCompatImageButton,
        downButton: AppCompatImageButton,
        resultsText: BaselineGridTextView
    ) {

        if (mainList == null || mainChildAdapter == null) {
            registerVars()
        }
        if (mainList == null || mainChildAdapter == null) return

        if (text.isEmpty()) {
            ImageViewCompat.setImageTintList(upButton, disabledColorStateList)
            ImageViewCompat.setImageTintList(downButton, disabledColorStateList)
            resultsText.text = ""
            mainChildAdapter!!.search("")
            mainChildAdapter!!.notifyDataSetChanged()
        } else {
            if (ImageViewCompat.getImageTintList(upButton) != defaultButtonColorStateList) {
                ImageViewCompat.setImageTintList(upButton, defaultButtonColorStateList)
                ImageViewCompat.setImageTintList(downButton, defaultButtonColorStateList)
            }
            resultList = getSearchListResult(mainList!!, text)
            if (resultList!!.total == 0) {
                resultsText.text = "0/0"
                resultsText.setTextColor(Formatting.SearchNotFoundColor)
                mainChildAdapter!!.search("")
                mainChildAdapter!!.notifyDataSetChanged()
            } else {
                if (resultsText.currentTextColor != defaultTextColor)
                    resultsText.setTextColor(defaultTextColor)
                val a = "1/${resultList!!.total}"
                resultsText.text = a
                mainChildAdapter!!.search(text)
                mainChildAdapter!!.notifyDataSetChanged()
                counter = 0
                onButton(true, true)
            }
        }
    }

    fun registerVars() {
        val mainFragmentView =
            requireActivity().supportFragmentManager.fragments.find { it.tag?.contains("MainFragment") ?: false }
                ?.view
        mainList =
            (mainFragmentView!!.findViewById<ViewPager3>(R.id.pager_main).findViewHolderForAdapterPosition(Prefs.VP_Position) as? MainPageAdapter.ViewHolder)?.childRecyclerView?.let { crv ->
                childRecyclerView = crv
                (crv.adapter as MainChildAdapter).also { mainChildAdapter = it }.list
            }
    }

}
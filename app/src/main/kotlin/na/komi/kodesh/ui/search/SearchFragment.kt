package na.komi.kodesh.ui.search

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.children
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.fragment_search.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import na.komi.kodesh.R
import na.komi.kodesh.ui.internal.BaseFragment2
import na.komi.kodesh.ui.internal.BottomSheetBehavior2
import na.komi.kodesh.ui.main.MainViewModel
import na.komi.kodesh.util.closestKatana
import na.komi.kodesh.util.snackbar
import org.rewedigital.katana.Component
import org.rewedigital.katana.KatanaTrait
import org.rewedigital.katana.androidx.viewmodel.activityViewModel

class SearchFragment : BaseFragment2(), KatanaTrait {
    override val layout: Int = R.layout.fragment_search

    override val component: Component by closestKatana()

    private val viewModel: MainViewModel by activityViewModel()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupRecyclerView()
        /*view?.findViewById<AppBarLayout>(R.id.search_app_bar_layout)?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                it.outlineProvider = null
            }
        }*/
        view?.findViewById<RecyclerView>(R.id.recycler_view_search)?.post {
            getToolbar()?.let {
                it.title = getString(R.string.kod_search_title)
                for (a in it.menu.children)
                    a.isVisible = false
            }
        }
        val bh = getBottomSheetBehavior()
        launch(Dispatchers.Main) {
            while (bh?.state != BottomSheetBehavior2.STATE_COLLAPSED) delay(10)
            val editText = view?.text_input_edit_text
            view?.findViewById<RecyclerView>(R.id.recycler_view_search)?.post { editText?.requestFocus() }
        }
    }


    fun InputMethodManager.showKeyboard() {
        toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    fun InputMethodManager.hideKeyboard(view: View) {
        hideSoftInputFromWindow(view.windowToken, 0);
    }

    private var listSize:Int? =null
    fun setupRecyclerView() {
        val rv: RecyclerView = view!!.recycler_view_search
        val adapter = SearchAdapter()
        val editText: TextInputEditText = view!!.text_input_edit_text as TextInputEditText
        val imm by lazy { requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager }

        rv.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        /*val searchVerseObserver = Observer<PagedList<Bible>> { list ->
            list?.let {
                adapter.submitList(it)
                Snackbar.make(
                        editText,
                        "${it.size} result${if (it.size > 1 || it.size == 0) "s" else ""}",
                        Snackbar.LENGTH_SHORT
                ).show()
            }
        }*/

        /*editText.setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                v.clearFocus()
                return@setOnKeyListener true
            }
            false
        }*/

        val coordinatorLayout = requireActivity().findViewById<CoordinatorLayout>(R.id.container_main)
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    if (s.isNotBlank() || s.isNotEmpty()) {
                        performSearch(s.toString(), adapter, coordinatorLayout)
                    }


                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        }

        editText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (listSize==null) {
                    SEARCH_DEBOUNCE_MS = 0
                    performSearch(editText.text.toString(), adapter, coordinatorLayout)
                    //imm.hideKeyboard(editText)
                }
                editText.clearFocus()
                return@setOnEditorActionListener true
            }
            false
        }
        editText.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) imm.showKeyboard()
            else imm.hideKeyboard(v)//.v.hideKeyboard()
        }


        editText.post {
            editText.addTextChangedListener(textWatcher)
            editText.selectAll()
        }

        rv.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        snackBar?.dismiss()
        listSize = null
        snackBar = null
    }

    private var SEARCH_DEBOUNCE_MS = 370.toLong()
    private var snackBar: Snackbar?=null
    fun performSearch(s: String, adapter: SearchAdapter, coordinatorLayout: CoordinatorLayout) {
        coroutineContext.cancelChildren()
        launch {
            delay(SEARCH_DEBOUNCE_MS)
            if (SEARCH_DEBOUNCE_MS == 0L)
                SEARCH_DEBOUNCE_MS = 370.toLong()
            viewModel.searchVerse(s).observe(viewLifecycleOwner, Observer { list ->
                list?.let {
                    // When we receive the list, use it.
                    adapter.submitList(it)
                    //editText.snackbar("${it.size} result${if (it.size == 1) "" else "s"}")
                    snackBar = coordinatorLayout.snackbar("${it.size} result${if (it.size == 1) "" else "s"}",Snackbar.LENGTH_SHORT)
                    listSize = it.size
                    //Snackbar.make(coordinatorLayout, "${it.size} result${if (it.size == 1) "" else "s"}", Snackbar.LENGTH_SHORT).apply { view.layoutParams = (view.layoutParams as CoordinatorLayout.LayoutParams).apply {setMargins(leftMargin, topMargin, rightMargin, 267)}}.show()
                    //editText.toast("${it.size} result${if (it.size == 1) "" else "s"}")
                }
            })
        }
    }

}
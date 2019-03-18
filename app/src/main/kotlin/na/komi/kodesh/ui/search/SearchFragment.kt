package na.komi.kodesh.ui.search

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.view.children
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.fragment_search.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import na.komi.kodesh.Application
import na.komi.kodesh.R
import na.komi.kodesh.model.Bible
import na.komi.kodesh.ui.internal.BaseFragment2
import na.komi.kodesh.ui.internal.BaseKatanaFragment
import na.komi.kodesh.ui.internal.BottomSheetBehavior2
import na.komi.kodesh.ui.internal.FragmentToolbar
import na.komi.kodesh.ui.main.MainViewModel
import na.komi.kodesh.util.closestKatana
import na.komi.kodesh.util.viewModel
import org.rewedigital.katana.Component
import org.rewedigital.katana.KatanaTrait

class SearchFragment : BaseFragment2() , KatanaTrait{
    override val layout: Int = R.layout.fragment_search

    override val component: Component by closestKatana()

    private val viewModel: MainViewModel by viewModel()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        getToolbar()?.let {
            it.title = getString(R.string.kod_search_title)
            for (a in it.menu.children)
                a.isVisible = false
        }
        setupRecyclerView()
        val bh = getBottomSheetBehavior()
        launch(Dispatchers.Main) {
            while (bh?.state != BottomSheetBehavior2.STATE_COLLAPSED) delay(10)
            val editText = view?.text_input_edit_text
            editText?.requestFocus()
        }
    }


    fun InputMethodManager.showKeyboard() {
        toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    fun InputMethodManager.hideKeyboard(view:View) {
        hideSoftInputFromWindow(view.windowToken, 0);
    }
    fun setupRecyclerView() {
        val rv: RecyclerView = view!!.recycler_view_search
        val adapter = SearchAdapter()
        val editText: TextInputEditText = view!!.text_input_edit_text as TextInputEditText
        var job = Job()
        val SEARCH_DEBOUNCE_MS = 300.toLong()
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

        editText.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) imm.showKeyboard()
            else imm.hideKeyboard(v)//.v.hideKeyboard()
        }

        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    if (s.isNotBlank() || s.isNotEmpty()) {
                        job.cancel()
                        job = launch {
                            delay(SEARCH_DEBOUNCE_MS)
                            viewModel.searchVerse(s.toString()).observe(viewLifecycleOwner, Observer { list ->
                                list?.let {
                                    // When we receive the list, use it.
                                    adapter.submitList(it)
                                    Snackbar.make(
                                            editText,
                                            "${it.size} result${if (it.size == 1) "" else "s"}",
                                            Snackbar.LENGTH_SHORT
                                    ).show()
                                }
                            })
                        }
                    }


                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })


        rv.adapter = adapter
    }


}
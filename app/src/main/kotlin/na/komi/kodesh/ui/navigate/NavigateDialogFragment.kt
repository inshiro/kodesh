package na.komi.kodesh.ui.navigate

import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.NumberPicker
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.button.MaterialButton
import na.komi.kodesh.Prefs
import na.komi.kodesh.R
import na.komi.kodesh.model.Bible
import na.komi.kodesh.ui.internal.ExtendedBottomSheetDialogFragment
import na.komi.kodesh.ui.main.MainViewModel
import na.komi.kodesh.util.closestKatana
import na.komi.kodesh.util.tryy
import na.komi.kodesh.ui.widget.NumberPicker2
import org.rewedigital.katana.Component
import org.rewedigital.katana.KatanaTrait
import org.rewedigital.katana.androidx.viewmodel.activityViewModel

class NavigateDialogFragment : ExtendedBottomSheetDialogFragment(), KatanaTrait {
    override val component: Component by closestKatana()

    private val viewModel:MainViewModel by activityViewModel()

    override val initialState: Int
        get() = BottomSheetBehavior.STATE_EXPANDED

    init {
        retainInstance = true
    }

    interface OnPositiveClick {
        fun onPositiveClick()
    }

    private var _listener: OnPositiveClick? = null
    fun setPositiveClickListener(listener: OnPositiveClick) {
        _listener = listener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dialog_navigate, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bookPicker = view.findViewById<NumberPicker2>(R.id.bookNumberPicker)
        val chapterPicker = view.findViewById<NumberPicker2>(R.id.chapterNumberPicker)
        val versePicker = view.findViewById<NumberPicker2>(R.id.verseNumberPicker)
        val negativeButton = view.findViewById<MaterialButton>(R.id.navigate_cancel_button)
        val positiveButton = view.findViewById<MaterialButton>(R.id.navigate_positive_button)
        val books = viewModel.getBooks()
        val bible: Bible = viewModel.getRowAtPagePositon(Prefs.VP_Position + 1)

        bookPicker.apply {
            scaleX = 1.1f
            scaleY = 1.1f
        }
        chapterPicker.apply {
            scaleX = 1.1f
            scaleY = 1.1f
        }
        versePicker.apply {
            scaleX = 1.1f
            scaleY = 1.1f
        }

        savedInstanceState?.getIntArray("BookPicker")?.let {
            bookPicker.apply {
                maxValue = it[0]
                minValue = it[1]
                displayedValues = books
                value = it[2]
            }
        } ?: bookPicker.apply {
            maxValue = books.size
            minValue = 1
            displayedValues = books
            value = bible.bookId!!
        }
        savedInstanceState?.getIntArray("ChapterPicker")?.let {
            chapterPicker.apply {
                maxValue = it[0]
                minValue = it[1]
                value = it[2]
            }
        } ?: chapterPicker.apply {
            maxValue = viewModel.getChapterAmount(bookPicker.value)
            minValue = 1
            value = bible.chapterId!!
        }


        savedInstanceState?.getIntArray("VersePicker")?.let {
            versePicker.apply {
                maxValue = it[0]
                minValue = it[1]
                value = it[2]
            }
        } ?: versePicker.apply {
            maxValue = viewModel.getVerseAmount(bookPicker.value, chapterPicker.value)//bible.verseId!!
            minValue = 1
            value = 1
        }
        bookPicker.setOnValueChangedListener { _, _, _ ->
            chapterPicker.maxValue = viewModel.getChapterAmount(bookPicker.value)
            chapterPicker.value = 1
        }
        chapterPicker.setOnValueChangedListener { _, _, _ ->
            versePicker.maxValue = viewModel.getVerseAmount(bookPicker.value, chapterPicker.value)
            versePicker.value = 1
        }

        tryy {
            val f = NumberPicker::class.java.getDeclaredField("mInputText")
            f.isAccessible = true
            val bookInputText = f.get(bookPicker) as EditText
            val chapterInputText = f.get(chapterPicker) as EditText
            val verseInputText = f.get(versePicker) as EditText
            bookInputText.filters = arrayOfNulls<InputFilter>(0)
            chapterInputText.filters = arrayOfNulls<InputFilter>(0)
            verseInputText.filters = arrayOfNulls<InputFilter>(0)
        }
        //.setPositiveButton("OK") { dialog, which ->

        negativeButton.setOnClickListener {
            dismiss()
        }
        positiveButton.setOnClickListener {
            val bp = viewModel.getPagePosition(bookPicker.value)
            val ca = chapterPicker.maxValue
            Prefs.NavigateToPosition = bp - ca + chapterPicker.value - 1
            viewModel.pagePosition.value = bp - ca + chapterPicker.value - 1
            //_listener?.onPositiveClick()
            dismiss()
        }
    }
    /*override fun onDestroyView() {
        dialog?.let {
            if (retainInstance) it.setDismissMessage(null)
        }
        super.onDestroyView()
    }*/


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        dialog?.let {
            val bookPicker = it.findViewById<NumberPicker2>(R.id.bookNumberPicker)
            val chapterPicker = it.findViewById<NumberPicker2>(R.id.chapterNumberPicker)
            val versePicker = it.findViewById<NumberPicker2>(R.id.verseNumberPicker)
            outState.putIntArray("BookPicker", intArrayOf(bookPicker.maxValue, bookPicker.minValue, bookPicker.value))
            outState.putIntArray(
                "ChapterPicker",
                intArrayOf(chapterPicker.maxValue, chapterPicker.minValue, chapterPicker.value)
            )
            outState.putIntArray(
                "VersePicker",
                intArrayOf(versePicker.maxValue, versePicker.minValue, versePicker.value)
            )
        }
    }

    /*override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val typedValue = TypedValue()
        activity!!.theme.resolveAttribute(R.attr.alertDialogTheme, typedValue, true)
        @ColorInt val id = typedValue.resourceId
        val alertBuilder = AlertDialog.Builder(activity!!, id)
            .setTitle(requireContext().getString(R.string.navigate_dialog_title))
            .setNegativeButton("CANCEL") { dialog, which -> dismiss() }
        val v = requireActivity().layoutInflater.inflate(R.layout.fragment_dialog_navigate, null)
        v?.let {
            val bookPicker = it.findViewById<NumberPicker2>(R.id.bookNumberPicker)
            val chapterPicker = it.findViewById<NumberPicker2>(R.id.chapterNumberPicker)
            val versePicker = it.findViewById<NumberPicker2>(R.id.verseNumberPicker)
            val books = viewModel.getBooks()
            val bible: Bible = viewModel.getRowAtPagePositon(Prefs.VP_Position + 1)

            bookPicker.apply {
                scaleX = 1.1f
                scaleY = 1.1f
            }
            chapterPicker.apply {
                scaleX = 1.1f
                scaleY = 1.1f
            }
            versePicker.apply {
                scaleX = 1.1f
                scaleY = 1.1f
            }

            savedInstanceState?.getIntArray("BookPicker")?.let {
                bookPicker.apply {
                    maxValue = it[0]
                    minValue = it[1]
                    displayedValues = books
                    value = it[2]
                }
            } ?: bookPicker.apply {
                maxValue = books.size
                minValue = 1
                displayedValues = books
                value = bible.bookId!!
            }
            savedInstanceState?.getIntArray("ChapterPicker")?.let {
                chapterPicker.apply {
                    maxValue = it[0]
                    minValue = it[1]
                    value = it[2]
                }
            } ?: chapterPicker.apply {
                maxValue = viewModel.getChapterAmount(bookPicker.value)
                minValue = 1
                value = bible.chapterId!!
            }


            savedInstanceState?.getIntArray("VersePicker")?.let {
                versePicker.apply {
                    maxValue = it[0]
                    minValue = it[1]
                    value = it[2]
                }
            } ?: versePicker.apply {
                maxValue = viewModel.getVerseAmount(bookPicker.value, chapterPicker.value)//bible.verseId!!
                minValue = 1
                value = 1
            }
            bookPicker.setOnValueChangedListener { _, _, _ ->
                chapterPicker.maxValue = viewModel.getChapterAmount(bookPicker.value)
                chapterPicker.value = 1
            }
            chapterPicker.setOnValueChangedListener { _, _, _ ->
                versePicker.maxValue = viewModel.getVerseAmount(bookPicker.value, chapterPicker.value)
                versePicker.value = 1
            }

            tryy {
                val f = NumberPicker::class.java.getDeclaredField("mInputText")
                f.isAccessible = true
                val bookInputText = f.get(bookPicker) as EditText
                val chapterInputText = f.get(chapterPicker) as EditText
                val verseInputText = f.get(versePicker) as EditText
                bookInputText.filters = arrayOfNulls<InputFilter>(0)
                chapterInputText.filters = arrayOfNulls<InputFilter>(0)
                verseInputText.filters = arrayOfNulls<InputFilter>(0)
            }
            alertBuilder.setView(it)
                .setPositiveButton("OK") { dialog, which ->
                    val bp = viewModel.getPagePosition(bookPicker.value)
                    val ca = chapterPicker.maxValue
                    Prefs.NavigateToPosition = bp - ca + chapterPicker.value - 1
                    _listener?.onPositiveClick()
                }
        }
        return alertBuilder.create()
    }*/
}
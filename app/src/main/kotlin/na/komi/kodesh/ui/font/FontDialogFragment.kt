package na.komi.kodesh.ui.font

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.SeekBar
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.appcompat.widget.AppCompatTextView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import na.komi.kodesh.Prefs
import na.komi.kodesh.R
import na.komi.kodesh.ui.internal.ExtendedBottomSheetDialogFragment
import na.komi.kodesh.ui.main.MainActivity
import na.komi.kodesh.ui.main.MainFragment
import na.komi.kodesh.ui.main.MainPageAdapter
import na.komi.kodesh.util.onClick
import na.komi.kodesh.util.setLowProfileStatusBar
import na.komi.kodesh.util.text.futureSet
import na.komi.kodesh.ui.widget.ViewPager3

class FontDialogFragment : ExtendedBottomSheetDialogFragment() {

    override val initialState: Int
        get() = BottomSheetBehavior.STATE_EXPANDED

    interface OnDismissListener {
        fun onDismiss()
    }

    private var _listener: OnDismissListener? = null
    fun setOnDismissListener(listener: OnDismissListener) {
        _listener = listener
    }

    /*
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val typedValue = TypedValue()
        activity!!.theme.resolveAttribute(R.attr.bottomSheetDialogTheme, typedValue, true)
        @LayoutRes val id = typedValue.resourceId
        setStyle(DialogFragment.STYLE_NO_TITLE, id)
    }*/

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dialog_font, container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = super.onCreateDialog(savedInstanceState)
        view.setOnShowListener {
            val bottomSheet = view.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            val behavior = BottomSheetBehavior.from(bottomSheet)
            behavior.skipCollapsed = true
            behavior.state = initialState// BottomSheetBehavior.STATE_COLLAPSED
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val step = 10
        val max = 200
        val min = 50
        val default = min / step
        var fromButton = false
        var person: Boolean


        val fontSizeTitle = view.findViewById<AppCompatTextView>(R.id.font_size_text_view)
        val themeTitle = view.findViewById<AppCompatTextView>(R.id.theme_title)

        val fontPercentText = view.findViewById<AppCompatTextView>(R.id.percent_text_view)
        val seekBar = view.findViewById<AppCompatSeekBar>(R.id.font_seek_bar)
        val increaseButton = view.findViewById<AppCompatTextView>(R.id.increase_button)
        val decreaseButton = view.findViewById<AppCompatTextView>(R.id.decrease_button)

        val lightThemeButton = view.findViewById<AppCompatButton>(R.id.light_theme_button)
        val darkThemeButton = view.findViewById<AppCompatButton>(R.id.dark_theme_button)
        val blackThemeButton = view.findViewById<AppCompatButton>(R.id.black_theme_button)

        val f = parentFragment as? MainFragment
        val rv = f?.view?.findViewById<ViewPager3>(R.id.pager_main)
       // var currentTextView =
       //     (f?.view?.findViewById<ViewPager3>(R.id.pager_main)?.findViewHolderForAdapterPosition(Prefs.VP_Position) as? MainPageAdapter.ViewHolder)?.textView


        // Ex :
        // If you want values from 3 to 5 with a step of 0.1 (3, 3.1, 3.2, ..., 5)
        // this means that you have 21 possible values in the seekbar.
        // So the range of the seek bar will be [0 ; (5-3)/0.1 = 20].
        seekBar.max = (max - min) / step
        seekBar.progress = Prefs.mainFontSize.toInt() - default
        val defaultValue = min + seekBar.progress * step
        fontPercentText.futureSet("$defaultValue%${if (defaultValue == 100) " (Default)" else ""}")
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {

                // Ex :
                // And finally when you want to retrieve the value in the range you
                // wanted in the first place -> [3-5]
                //
                // if progress = 13 -> value = 3 + (13 * 0.1) = 4.3

                person = fromUser
                if (fromButton) person = true
                if (!person) return
                /*if (currentTextView == null)
                    currentTextView =
                        (f?.view?.findViewById<ViewPager3>(R.id.pager_main)?.findViewHolderForAdapterPosition(Prefs.VP_Position) as? MainPageAdapter.ViewHolder)?.textView
                currentTextView?.let {
                    val value = min + progress * step
                    fontPercentText.futureSet("$value%${if (value == 100) " (Default)" else ""}")
                    currentFontSize = value / 10f
                    //adapter?.updateTextSize(currentFontSize)
                    viewModel.textSize.value = currentFontSize
                    //setFontSize(value)
                }*/

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

        })
        increaseButton.setOnClickListener {
            fromButton = true
            seekBar.progress++
            fromButton = false
            //setFontSize(computeFontValue(seekBar.progress,min,step))
        }
        decreaseButton.setOnClickListener {
            fromButton = true
            seekBar.progress--
            fromButton = false
            //setFontSize(computeFontValue(seekBar.progress,min,step))
        }
        lightThemeButton.onClick {
            Prefs.themeId = 0
            restartActivity()

        }
        darkThemeButton.onClick {
            Prefs.themeId = 1
            restartActivity()
        }
        blackThemeButton.onClick {
            Prefs.themeId = 2
            restartActivity()
        }
        val fontSizeSpannable = fontSizeTitle.text as Spannable
        val themeTitleSpannable = themeTitle.text as Spannable
        fontSizeSpannable.withSpan(StyleSpan(Typeface.BOLD))
        themeTitleSpannable.withSpan(StyleSpan(Typeface.BOLD))

    }


    inline fun Spannable.withSpan(span: Any, action: Spannable.() -> Unit = {}): Spannable {
        val from = length
        action()
        setSpan(span, if (from == length) 0 else from, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        return this
    }
    private var currentFontSize = -1f

    private fun restartActivity() {
        activity?.let {
            val intent = Intent(it, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK and Intent.FLAG_ACTIVITY_NO_ANIMATION
            startActivity(intent);
            it.finish();
            it.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }
    override fun onDismiss(dialog: DialogInterface) {

/*        if (currentFontSize != -1f && currentFontSize != Prefs.mainFontSize) {

            val f = parentFragment as MainFragment
            val rv = f.view?.findViewById<ViewPager3>(R.id.pager_main)
            val adapter = (rv?.adapter as? MainPageAdapter)
            val vh = rv?.findViewHolderForAdapterPosition(Prefs.VP_Position) as MainPageAdapter.ViewHolder
            // int childCount = recyclerView.getChildCount(), i = 0; i < childCount; ++i
            if (rv!=null)
            for (i in 0  until rv.childCount) {
                val vh = rv.getChildViewHolder(rv.getChildAt(i)) as MainPageAdapter.ViewHolder
                vh.textView.setTextSize(TypedValue.COMPLEX_UNIT_PT, currentFontSize)
                log d "Inside for: $i"
            }


            //vh.updateTextSize(currentFontSize)
            Prefs.mainFontSize = currentFontSize
            //adapter?.updateTextSize(currentFontSize)
            adapter?.notifyDataSetChanged()
        }*/
        super.onDismiss(dialog)
        (activity as? MainActivity)?.setLowProfileStatusBar()
        //log d "currentFontSize: $currentFontSize"
        if (currentFontSize != -1f && currentFontSize != Prefs.mainFontSize) {
            _listener?.onDismiss()
            Prefs.mainFontSize = currentFontSize
            currentFontSize = -1f
        }
    }

    // For config change
    override fun onStop() {
        super.onStop()
        if (currentFontSize != -1f && currentFontSize != Prefs.mainFontSize) {
            _listener?.onDismiss()
            Prefs.mainFontSize = currentFontSize
            currentFontSize = -1f
        }
    }

    /*
    private var _viewModel: MainViewModel?=null
    private val viewModel
      get()=  _viewModel ?: ViewModelProviders.of(activity!!, InjectorUtils.provideMainViewModelFactory(activity!!))
            .get(MainViewModel::class.java)*/



}
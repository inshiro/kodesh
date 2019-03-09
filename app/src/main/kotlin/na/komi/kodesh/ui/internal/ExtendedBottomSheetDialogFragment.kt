package na.komi.kodesh.ui.internal

import android.app.Dialog
import android.content.res.Configuration
import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import na.komi.kodesh.R


/**
 * Descendant of BottomSheetDialogFragment that adds a few features and conveniences.
 */
abstract class ExtendedBottomSheetDialogFragment : BottomSheetDialogFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @BottomSheetBehavior.State
    abstract val initialState:Int

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = super.onCreateDialog(savedInstanceState)
        view.setOnShowListener {
            val bottomSheet = view.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            val behavior = BottomSheetBehavior.from(bottomSheet)
            //behavior.skipCollapsed = true
            behavior.state = initialState// BottomSheetBehavior.STATE_COLLAPSED
        }
        return view
    }

    override fun onStart() {
        super.onStart()
        setWindowLayout()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setWindowLayout()
    }

    protected fun disableBackgroundDim() {
        dialog!!.window!!.setDimAmount(0f)
    }

    private fun setWindowLayout() {
        if (dialog != null) {
            val width = dialogWidthPx()
            dialog!!.window!!.setLayout(
                if (width > 0) width else ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    private fun dialogWidthPx(): Int {
        return context!!.resources.getDimensionPixelSize(R.dimen.bottom_sheet_width)
    }
}
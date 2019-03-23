package na.komi.kodesh.util

import android.content.Context
import android.os.Build
import android.os.SystemClock
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.view.View
import com.google.android.material.snackbar.Snackbar
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import na.komi.kodesh.Application
import na.komi.kodesh.ui.internal.BottomSheetBehavior2
import na.komi.kodesh.ui.internal.LinearLayoutManager2
import na.komi.kodesh.ui.widget.ViewPager3
import kotlin.math.absoluteValue


fun View.onClick(debounceTime: Long = 1000L, action: () -> Unit) {
    this.setOnClickListener(object : View.OnClickListener {
        private var lastClickTime: Long = 0

        override fun onClick(v: View) {
            if (SystemClock.elapsedRealtime() - lastClickTime < debounceTime) return
            else action()

            lastClickTime = SystemClock.elapsedRealtime()
        }
    })
}

fun View.snackbar(text: String, length: Int = Snackbar.LENGTH_LONG):Snackbar {
    return Snackbar.make(this, text, length).also { it.show() }
}
fun View.toast(text: String, length: Int = Toast.LENGTH_LONG) {
    Toast.makeText(context, text, length).show()
}

fun BottomSheetBehavior2<ConstraintLayout>.close() {
    if (state == BottomSheetBehavior2.STATE_EXPANDED)
        state = BottomSheetBehavior2.STATE_COLLAPSED
}

fun BottomSheetBehavior2<ConstraintLayout>.toggle(): Boolean {
    if (state == BottomSheetBehavior2.STATE_COLLAPSED)
        setState(BottomSheetBehavior2.STATE_EXPANDED)
    else if (state == BottomSheetBehavior2.STATE_EXPANDED)
        state = BottomSheetBehavior2.STATE_COLLAPSED
    return true
}

fun createStaticLayout(str: CharSequence, width: Int, tp: TextPaint, textSize: Float? = null): StaticLayout {
    if (textSize != null)
        tp.textSize = textSize
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        //log d "Create StaticLayout"
        val builder = StaticLayout.Builder.obtain(str, 0, str.length, tp, width)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1f)
            .setIncludePad(false)
            .setHyphenationFrequency(Layout.HYPHENATION_FREQUENCY_NONE)
        return builder.build()
    } else {
        @Suppress("DEPRECATION")
        return StaticLayout(
            str,
            tp,
            width,
            Layout.Alignment.ALIGN_NORMAL,
            1f,
            0f,
            false
        )
    }
}

fun RecyclerView.betterSmoothScrollToPosition(targetItem: Int) {
    layoutManager?.apply {
        val maxScroll = 10
        when (this) {
            is LinearLayoutManager -> {
                val topItem = findFirstVisibleItemPosition()
                val distance = topItem - targetItem
                val anchorItem = when {
                    distance > maxScroll -> targetItem + maxScroll
                    distance < -maxScroll -> targetItem - maxScroll
                    else -> topItem
                }
                var scrollTo = false
                if (anchorItem != topItem) scrollToPosition(anchorItem).also { scrollTo = true }
                post {
                    if (!scrollTo) {
                        if (anchorItem in 0..targetItem) {
                            scrollToPosition(anchorItem)
                        } else if (targetItem-5 >= 0)  scrollToPosition(targetItem-5)
                        scrollTo = true
                    }
                    smoothScrollToPosition(targetItem)
                }
            }
            else -> smoothScrollToPosition(targetItem).also { log d "Ran here $targetItem" }
        }
    }
}

/**
 * Inflate a [View] with given layoutId and attach it to the calling [ViewGroup].
 * @param layout Id of the layout to inflate.
 */
fun ViewGroup.inflateView(@LayoutRes layout: Int): View {
    return LayoutInflater.from(this.context).inflate(layout, this, false)
}

/**
 * This method converts dp unit to equivalent pixels, depending on device density.
 *
 * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
 * @param context Context to get resources and device specific display metrics
 * @return A float value to represent px equivalent to dp depending on device density
 */
fun Context.dpToPx(dp: Float): Float {
    return dp * (resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}

/**
 * This method converts device specific pixels to density independent pixels.
 *
 * @param px A value in px (pixels) unit. Which we need to convert into db
 * @param context Context to get resources and device specific display metrics
 * @return A float value to represent dp equivalent to px value
 */
fun Context.pxToDp(px: Float): Float {
    return px / (resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}

fun Context.pxToPt(px: Float): Float {
    return px * 72 / (resources.displayMetrics.densityDpi.toFloat())
}

fun Context.ptToPx(pt: Float): Float {
    return pt / 72 * (resources.displayMetrics.densityDpi.toFloat())//resources.displayMetrics.density + 0.5f
}

fun sp(size: Float) = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_SP,
    size,
    Application.instance.applicationContext.resources.displayMetrics
)
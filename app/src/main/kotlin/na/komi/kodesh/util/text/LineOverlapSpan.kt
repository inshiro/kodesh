package na.komi.kodesh.util.text

import android.graphics.Paint
import android.text.style.LineHeightSpan

/**
 * Diagram: is.gd/uhKKGO
 * Android span shift
 * https://is.gd/coGGJ9
 */
class LineOverlapSpan(private val adjust: Int = 0) : LineHeightSpan {
    override fun chooseHeight(text: CharSequence, start: Int, end: Int, spanstartv: Int, v: Int, fm: Paint.FontMetricsInt) {
        fm.bottom += fm.top //- (fm.descent + fm.bottom)
        fm.descent += fm.top + adjust*3
    }
}